package info.maaskant.wmsnotes.android.client.synchronization

import android.content.Context
import android.preference.PreferenceManager
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.android.app.di.Configuration.storeInMemory
import info.maaskant.wmsnotes.android.ui.settings.SettingsFragment
import info.maaskant.wmsnotes.client.api.GrpcCommandMapper
import info.maaskant.wmsnotes.client.api.GrpcEventMapper
import info.maaskant.wmsnotes.client.synchronization.*
import info.maaskant.wmsnotes.client.synchronization.eventrepository.FileModifiableEventRepository
import info.maaskant.wmsnotes.client.synchronization.eventrepository.InMemoryModifiableEventRepository
import info.maaskant.wmsnotes.client.synchronization.eventrepository.ModifiableEventRepository
import info.maaskant.wmsnotes.model.CommandProcessor
import info.maaskant.wmsnotes.model.Event
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.projection.NoteProjector
import info.maaskant.wmsnotes.server.command.grpc.CommandServiceGrpc
import info.maaskant.wmsnotes.server.command.grpc.EventServiceGrpc
import info.maaskant.wmsnotes.utilities.persistence.FileStateRepository
import info.maaskant.wmsnotes.utilities.persistence.StateRepository
import info.maaskant.wmsnotes.utilities.serialization.Serializer
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Suppress("ConstantConditionIf")
@Module(includes = [SynchronizationWorkerModule::class])
class SynchronizationModule {

    @Singleton
    @Provides
    @ServerHostname
    fun serverHostname(context: Context): String = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getString(
            SettingsFragment.SERVER_HOSTNAME_KEY,
            context.resources.getString(R.string.pref_default_server_hostname)
        )!!

    @Singleton
    @Provides
    fun grpcCommandService(managedChannel: ManagedChannel) =
        CommandServiceGrpc.newBlockingStub(managedChannel)!!
            .withDeadlineAfter(1000, TimeUnit.MILLISECONDS)

    @Singleton
    @Provides
    fun grpcEventService(managedChannel: ManagedChannel) =
        EventServiceGrpc.newBlockingStub(managedChannel)!!
            .withDeadlineAfter(1000, TimeUnit.MILLISECONDS)

    @Singleton
    @Provides
    fun managedChannel(@ServerHostname hostname: String): ManagedChannel =
        ManagedChannelBuilder.forAddress(hostname, 6565)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build()

    @Singleton
    @Provides
    @ForLocalEvents
    fun localEventRepository(@OtherModule.AppDirectory appDirectory: File, eventSerializer: Serializer<Event>) =
        if (storeInMemory) {
            InMemoryModifiableEventRepository()
        } else {
            FileModifiableEventRepository(
                appDirectory.resolve("synchronization").resolve("local_events"),
                eventSerializer
            )
        }

    @Singleton
    @Provides
    @ForRemoteEvents
    fun remoteEventRepository(@OtherModule.AppDirectory appDirectory: File, eventSerializer: Serializer<Event>) =
        if (storeInMemory) {
            InMemoryModifiableEventRepository()
        } else {
            FileModifiableEventRepository(
                appDirectory.resolve("synchronization").resolve("remote_events"),
                eventSerializer
            )
        }

    @Singleton
    @Provides
    fun eventImporterStateSerializer(kryoPool: Pool<Kryo>): Serializer<EventImporterState> =
        KryoEventImporterStateSerializer(kryoPool)

    @Singleton
    @Provides
    @ForLocalEvents
    fun localEventImporterStateRepository(@OtherModule.AppDirectory appDirectory: File, serializer: Serializer<EventImporterState>): StateRepository<EventImporterState> =
        FileStateRepository(
            serializer = serializer,
            file = appDirectory.resolve("synchronization").resolve("local_events").resolve(".state"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Singleton
    @Provides
    @ForRemoteEvents
    fun remoteEventImporterStateRepository(@OtherModule.AppDirectory appDirectory: File, serializer: Serializer<EventImporterState>): StateRepository<EventImporterState> =
        FileStateRepository(
            serializer = serializer,
            file = appDirectory.resolve("synchronization").resolve("remote_events").resolve(".state"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Singleton
    @Provides
    fun localEventImporter(
        eventStore: EventStore,
        @ForLocalEvents eventRepository: ModifiableEventRepository,
        @ForLocalEvents stateRepository: StateRepository<EventImporterState>
    ) =
        LocalEventImporter(
            eventStore,
            eventRepository,
            stateRepository.load()
        ).apply {
            stateRepository.connect(this)
        }

    @Singleton
    @Provides
    fun remoteEventImporter(
        grpcEventService: EventServiceGrpc.EventServiceBlockingStub,
        grpcEventMapper: GrpcEventMapper,
        @ForRemoteEvents eventRepository: ModifiableEventRepository,
        @ForLocalEvents stateRepository: StateRepository<EventImporterState>
    ) =
        RemoteEventImporter(
            grpcEventService,
            eventRepository,
            grpcEventMapper,
            stateRepository.load()
        ).apply {
            stateRepository.connect(this)
        }

    @Singleton
    @Provides
    fun synchronizerStateRepository(@OtherModule.AppDirectory appDirectory: File, kryoPool: Pool<Kryo>): StateRepository<SynchronizerState> =
        FileStateRepository(
            serializer = KryoSynchronizerStateSerializer(kryoPool),
            file = appDirectory.resolve("synchronization").resolve("synchronizer.state"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Singleton
    @Provides
    fun synchronizer(
        @ForLocalEvents localEvents: ModifiableEventRepository,
        @ForRemoteEvents remoteEvents: ModifiableEventRepository,
        remoteCommandService: CommandServiceGrpc.CommandServiceBlockingStub,
        eventToCommandMapper: EventToCommandMapper,
        grpcCommandMapper: GrpcCommandMapper,
        commandProcessor: CommandProcessor,
        noteProjector: NoteProjector,
        stateRepository: StateRepository<SynchronizerState>
    ) = Synchronizer(
        localEvents,
        remoteEvents,
        remoteCommandService,
        eventToCommandMapper,
        grpcCommandMapper,
        commandProcessor,
        noteProjector,
        stateRepository.load()
    ).apply {
        stateRepository.connect(this)
    }

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ServerHostname

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ForLocalEvents

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ForRemoteEvents

}
