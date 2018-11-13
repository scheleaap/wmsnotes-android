package info.maaskant.wmsnotes.android.model

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.android.app.di.Configuration.cache
import info.maaskant.wmsnotes.android.app.di.Configuration.delay
import info.maaskant.wmsnotes.android.app.di.Configuration.storeInMemory
import info.maaskant.wmsnotes.model.Event
import info.maaskant.wmsnotes.model.KryoEventSerializer
import info.maaskant.wmsnotes.model.eventstore.DelayingEventStore
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.eventstore.FileEventStore
import info.maaskant.wmsnotes.model.eventstore.InMemoryEventStore
import info.maaskant.wmsnotes.model.projection.NoteProjector
import info.maaskant.wmsnotes.model.projection.cache.*
import info.maaskant.wmsnotes.utilities.serialization.Serializer
import java.io.File
import javax.inject.Singleton

@Suppress("ConstantConditionIf")
@Module
class ModelModule {

    @Provides
    fun eventSerializer(kryoPool: Pool<Kryo>): Serializer<Event> = KryoEventSerializer(kryoPool)

    @Provides
    @Singleton
    fun eventStore(@OtherModule.AppDirectory appDirectory: File, eventSerializer: Serializer<Event>): EventStore {
        val realStore = if (storeInMemory) {
            InMemoryEventStore()
        } else {
            FileEventStore(appDirectory.resolve("events"), eventSerializer)
        }
        return if (delay) {
            DelayingEventStore(realStore)
        } else {
            realStore
        }
    }

    @Singleton
    @Provides
    fun noteCache(@OtherModule.AppDirectory appDirectory: File, noteSerializer: Serializer<info.maaskant.wmsnotes.model.projection.Note>): NoteCache =
        if (cache) {
            FileNoteCache(appDirectory.resolve("cache").resolve("projected_notes"), noteSerializer)
        } else {
            NoopNoteCache
        }

    @Singleton
    @Provides
    fun noteProjector(cachingNoteProjector: CachingNoteProjector): NoteProjector = cachingNoteProjector

    @Provides
    fun noteSerializer(kryoPool: Pool<Kryo>): Serializer<info.maaskant.wmsnotes.model.projection.Note> =
        KryoNoteSerializer(kryoPool)

}
