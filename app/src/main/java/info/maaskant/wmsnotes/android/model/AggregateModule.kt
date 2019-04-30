package info.maaskant.wmsnotes.android.model

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.android.app.di.Configuration.cache
import info.maaskant.wmsnotes.android.app.di.Configuration.storeInMemory
import info.maaskant.wmsnotes.model.aggregaterepository.*
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.folder.Folder
import info.maaskant.wmsnotes.model.folder.KryoFolderSerializer
import info.maaskant.wmsnotes.model.note.KryoNoteSerializer
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.utilities.serialization.Serializer
import java.io.File
import javax.inject.Singleton

@Suppress("ConstantConditionIf")
@Module
class AggregateModule {
    @Provides
    @Singleton
    fun folderCache(@OtherModule.AppDirectory appDirectory: File, serializer: Serializer<Folder>): AggregateCache<Folder> =
        if (cache && !storeInMemory) {
            FileAggregateCache(appDirectory.resolve("cache").resolve("projected_folders"), serializer)
        } else {
            NoopAggregateCache()
        }

    @Provides
    @Singleton
    fun noteCache(@OtherModule.AppDirectory appDirectory: File, serializer: Serializer<Note>): AggregateCache<Note> =
        if (cache && !storeInMemory) {
            FileAggregateCache(appDirectory.resolve("cache").resolve("projected_notes"), serializer)
        } else {
            NoopAggregateCache()
        }

    @Provides
    @Singleton
    fun folderRepository(eventStore: EventStore, cache: AggregateCache<Folder>): AggregateRepository<Folder> =
        CachingAggregateRepository(eventStore, cache, Folder())

    @Provides
    @Singleton
    fun noteRepository(eventStore: EventStore, cache: AggregateCache<Note>): AggregateRepository<Note> =
        CachingAggregateRepository(eventStore, cache, Note())

    @Provides
    @Singleton
    fun folderSerializer(kryoPool: Pool<Kryo>): Serializer<Folder> = KryoFolderSerializer(kryoPool)

    @Provides
    @Singleton
    fun noteSerializer(kryoPool: Pool<Kryo>): Serializer<Note> = KryoNoteSerializer(kryoPool)
}
