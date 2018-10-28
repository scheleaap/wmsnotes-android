package info.maaskant.wmsnotes.android.app.di2

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.model.KryoEventSerializer
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.eventstore.FileEventStore
import java.io.File
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun provideEventStore(kryoPool: Pool<Kryo>): EventStore =
        FileEventStore(File("wmsnotes_data/events"), KryoEventSerializer(kryoPool))

}
