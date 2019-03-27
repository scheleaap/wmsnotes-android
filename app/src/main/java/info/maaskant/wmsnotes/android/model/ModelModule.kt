package info.maaskant.wmsnotes.android.model

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.android.app.di.Configuration.cache
import info.maaskant.wmsnotes.android.app.di.Configuration.delay
import info.maaskant.wmsnotes.android.app.di.Configuration.storeInMemory
import info.maaskant.wmsnotes.model.AggregateCommandHandler
import info.maaskant.wmsnotes.model.CommandProcessor
import info.maaskant.wmsnotes.model.Event
import info.maaskant.wmsnotes.model.KryoEventSerializer
import info.maaskant.wmsnotes.model.aggregaterepository.*
import info.maaskant.wmsnotes.model.eventstore.DelayingEventStore
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.eventstore.FileEventStore
import info.maaskant.wmsnotes.model.eventstore.InMemoryEventStore
import info.maaskant.wmsnotes.model.folder.Folder
import info.maaskant.wmsnotes.model.folder.FolderCommand
import info.maaskant.wmsnotes.model.folder.FolderCommandToEventMapper
import info.maaskant.wmsnotes.model.folder.KryoFolderSerializer
import info.maaskant.wmsnotes.model.note.KryoNoteSerializer
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.model.note.NoteCommand
import info.maaskant.wmsnotes.model.note.NoteCommandToEventMapper
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

    @Provides
    @Singleton
    fun folderCommandHandler(repository: AggregateRepository<Folder>): AggregateCommandHandler<Folder> =
        AggregateCommandHandler(
            FolderCommand::class,
            repository,
            FolderCommandToEventMapper()
        )

    @Provides
    @Singleton
    fun noteCommandHandler(repository: AggregateRepository<Note>): AggregateCommandHandler<Note> =
        AggregateCommandHandler(
            NoteCommand::class,
            repository,
            NoteCommandToEventMapper()
        )

    @Provides
    @Singleton
    fun commandProcessor(
        eventStore: EventStore,
        folderCommandHandler: AggregateCommandHandler<Folder>,
        noteCommandHandler: AggregateCommandHandler<Note>
    ): CommandProcessor =
        CommandProcessor(
            eventStore,
            folderCommandHandler,
            noteCommandHandler
        )

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
