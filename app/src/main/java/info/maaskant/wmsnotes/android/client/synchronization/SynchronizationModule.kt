package info.maaskant.wmsnotes.android.client.synchronization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import com.f2prateek.rx.preferences2.Preference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.android.app.PreferencesModule.ServerHostname
import info.maaskant.wmsnotes.android.app.Configuration.storeInMemory
import info.maaskant.wmsnotes.client.api.GrpcCommandMapper
import info.maaskant.wmsnotes.client.api.GrpcEventMapper
import info.maaskant.wmsnotes.client.synchronization.*
import info.maaskant.wmsnotes.client.synchronization.commandexecutor.LocalCommandExecutor
import info.maaskant.wmsnotes.client.synchronization.commandexecutor.RemoteCommandExecutor
import info.maaskant.wmsnotes.client.synchronization.eventrepository.FileModifiableEventRepository
import info.maaskant.wmsnotes.client.synchronization.eventrepository.InMemoryModifiableEventRepository
import info.maaskant.wmsnotes.client.synchronization.eventrepository.ModifiableEventRepository
import info.maaskant.wmsnotes.client.synchronization.strategy.*
import info.maaskant.wmsnotes.client.synchronization.strategy.merge.EqualsMergeStrategy
import info.maaskant.wmsnotes.client.synchronization.strategy.merge.MergeStrategy
import info.maaskant.wmsnotes.client.synchronization.strategy.merge.MultipleMergeStrategy
import info.maaskant.wmsnotes.client.synchronization.strategy.merge.folder.FolderMergingSynchronizationStrategy
import info.maaskant.wmsnotes.client.synchronization.strategy.merge.note.DifferenceAnalyzer
import info.maaskant.wmsnotes.client.synchronization.strategy.merge.note.DifferenceCompensator
import info.maaskant.wmsnotes.client.synchronization.strategy.merge.note.KeepBothMergeStrategy
import info.maaskant.wmsnotes.client.synchronization.strategy.merge.note.NoteMergingSynchronizationStrategy
import info.maaskant.wmsnotes.model.CommandBus
import info.maaskant.wmsnotes.model.CommandExecution
import info.maaskant.wmsnotes.model.Event
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.folder.Folder
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.server.command.grpc.CommandServiceGrpc
import info.maaskant.wmsnotes.server.command.grpc.EventServiceGrpc
import info.maaskant.wmsnotes.utilities.persistence.FileStateRepository
import info.maaskant.wmsnotes.utilities.persistence.StateRepository
import info.maaskant.wmsnotes.utilities.serialization.Serializer
import io.grpc.Deadline
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SynchronizationModule {
    @Provides
    @Singleton
    fun grpcDeadline(): Deadline = Deadline.after(1, TimeUnit.SECONDS)

    @Provides
    @Singleton
    fun grpcCommandService(managedChannel: ManagedChannel) =
        CommandServiceGrpc.newBlockingStub(managedChannel)!!

    @Provides
    @Singleton
    fun grpcEventService(managedChannel: ManagedChannel) =
        EventServiceGrpc.newBlockingStub(managedChannel)!!

    @Provides
    @Singleton
    fun managedChannel(@ServerHostname hostnamePreference: Preference<String>): ManagedChannel {
        val hostname = if (hostnamePreference.get().isBlank()) "localhost" else hostnamePreference.get()
        return ManagedChannelBuilder.forAddress(hostname, 6565)
            .usePlaintext()
            .build()
    }

    @Provides
    @Singleton
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

    @Provides
    @Singleton
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

    @Provides
    @Singleton
    fun eventImporterStateSerializer(kryoPool: Pool<Kryo>): Serializer<EventImporterState> =
        KryoEventImporterStateSerializer(kryoPool)

    @Provides
    @Singleton
    @ForLocalEvents
    fun localEventImporterStateRepository(@OtherModule.AppDirectory appDirectory: File, serializer: Serializer<EventImporterState>): StateRepository<EventImporterState> =
        FileStateRepository(
            serializer = serializer,
            file = appDirectory.resolve("synchronization").resolve("local_events").resolve(".state"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Provides
    @Singleton
    @ForRemoteEvents
    fun remoteEventImporterStateRepository(@OtherModule.AppDirectory appDirectory: File, serializer: Serializer<EventImporterState>): StateRepository<EventImporterState> =
        FileStateRepository(
            serializer = serializer,
            file = appDirectory.resolve("synchronization").resolve("remote_events").resolve(".state"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Provides
    @Singleton
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

    @Provides
    @Singleton
    fun remoteEventImporter(
        grpcEventService: EventServiceGrpc.EventServiceBlockingStub,
        grpcDeadline: Deadline,
        grpcEventMapper: GrpcEventMapper,
        @ForRemoteEvents eventRepository: ModifiableEventRepository,
        @ForRemoteEvents stateRepository: StateRepository<EventImporterState>
    ) =
        RemoteEventImporter(
            grpcEventService,
            grpcDeadline,
            eventRepository,
            grpcEventMapper,
            stateRepository.load()
        ).apply {
            stateRepository.connect(this)
        }

    @Provides
    @Singleton
    fun synchronizationStrategy(
        folderMergeStrategy: MergeStrategy<Folder>,
        folderRepository: AggregateRepository<Folder>,
        noteMergeStrategy: MergeStrategy<Note>,
        noteRepository: AggregateRepository<Note>
    ): SynchronizationStrategy =
        SkippingIdenticalDelegatingSynchronizationStrategy(
            MultipleSynchronizationStrategy(
                LocalOnlySynchronizationStrategy(),
                RemoteOnlySynchronizationStrategy(),
                NoteMergingSynchronizationStrategy(
                    mergeStrategy = noteMergeStrategy,
                    aggregateRepository = noteRepository
                ),
                FolderMergingSynchronizationStrategy(
                    mergeStrategy = folderMergeStrategy,
                    aggregateRepository = folderRepository
                )
            )
        )

    @Provides
    @Singleton
    fun noteMergeStrategy(
        differenceAnalyzer: DifferenceAnalyzer,
        differenceCompensator: DifferenceCompensator
    ): MergeStrategy<Note> =
        MultipleMergeStrategy(
            EqualsMergeStrategy(),
            KeepBothMergeStrategy(
                differenceAnalyzer = differenceAnalyzer,
                differenceCompensator = differenceCompensator,
                aggregateIdGenerator = { "n-" + UUID.randomUUID().toString() },
                conflictedNoteTitleSuffix = " (conflict on Android)"
            )
        )

    @Provides
    @Singleton
    fun folderMergeStrategy(): MergeStrategy<Folder> =
        EqualsMergeStrategy()

    @Singleton
    @Provides
    fun differenceAnalyzer() = DifferenceAnalyzer()

    @Singleton
    @Provides
    fun differenceCompensator() = DifferenceCompensator()

    @Singleton
    @Provides
    fun eventToCommandMapper() = EventToCommandMapper()

    @Provides
    @Singleton
    fun commandToCommandRequestMapper() = CommandToCommandRequestMapper()

    @Provides
    @Singleton
    fun localCommandExecutor(
        commandToCommandRequestMapper: CommandToCommandRequestMapper,
        commandBus: CommandBus,
        commandExecutionTimeout: CommandExecution.Duration
    ) =
        LocalCommandExecutor(commandToCommandRequestMapper, commandBus, commandExecutionTimeout)

    @Provides
    @Singleton
    fun remoteCommandExecutor(
        grpcCommandMapper: GrpcCommandMapper,
        grpcCommandService: CommandServiceGrpc.CommandServiceBlockingStub,
        grpcDeadline: Deadline
    ) =
        RemoteCommandExecutor(grpcCommandMapper, grpcCommandService, grpcDeadline)

    @Provides
    @Singleton
    fun synchronizerStateRepository(@OtherModule.AppDirectory appDirectory: File, kryoPool: Pool<Kryo>): StateRepository<SynchronizerState> =
        FileStateRepository(
            serializer = KryoSynchronizerStateSerializer(kryoPool),
            file = appDirectory.resolve("synchronization").resolve("synchronizer.state"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Provides
    @Singleton
    fun synchronizer(
        @ForLocalEvents localEvents: ModifiableEventRepository,
        @ForRemoteEvents remoteEvents: ModifiableEventRepository,
        synchronizationStrategy: SynchronizationStrategy,
        eventToCommandMapper: EventToCommandMapper,
        localCommandExecutor: LocalCommandExecutor,
        remoteCommandExecutor: RemoteCommandExecutor,
        stateRepository: StateRepository<SynchronizerState>
    ) = Synchronizer(
        localEvents,
        remoteEvents,
        synchronizationStrategy,
        eventToCommandMapper,
        localCommandExecutor,
        remoteCommandExecutor,
        stateRepository.load()
    ).apply {
        stateRepository.connect(this)
    }

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ForLocalEvents

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ForRemoteEvents

}
