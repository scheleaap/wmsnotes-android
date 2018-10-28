package info.maaskant.wmsnotes.android.model

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.model.KryoEventSerializer
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.eventstore.FileEventStore
import java.io.File
import javax.inject.Singleton

@Module
class ModelModule {

    @Provides
    @Singleton
    fun provideEventStore(@OtherModule.AppDirectory appDirectory: File, kryoPool: Pool<Kryo>): EventStore =
        FileEventStore(appDirectory.resolve("events"), KryoEventSerializer(kryoPool))

}
