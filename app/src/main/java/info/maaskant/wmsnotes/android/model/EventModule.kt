package info.maaskant.wmsnotes.android.model

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.android.app.Configuration.delay
import info.maaskant.wmsnotes.android.app.Configuration.storeInMemory
import info.maaskant.wmsnotes.model.Event
import info.maaskant.wmsnotes.model.KryoEventSerializer
import info.maaskant.wmsnotes.model.eventstore.DelayingEventStore
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.eventstore.FileEventStore
import info.maaskant.wmsnotes.model.eventstore.InMemoryEventStore
import info.maaskant.wmsnotes.utilities.serialization.Serializer
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class EventModule {
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
}
