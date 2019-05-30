package info.maaskant.wmsnotes.android.model

import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.model.CommandBus
import info.maaskant.wmsnotes.model.CommandExecution
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.folder.Folder
import info.maaskant.wmsnotes.model.folder.FolderCommandExecutor
import info.maaskant.wmsnotes.model.folder.FolderCommandToEventMapper
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.model.note.NoteCommandExecutor
import info.maaskant.wmsnotes.model.note.NoteCommandToEventMapper
import info.maaskant.wmsnotes.model.note.policy.NoteTitlePolicy
import info.maaskant.wmsnotes.model.note.policy.extractTitleFromContent
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class CommandModule {
    @Provides
    @Singleton
    fun commandExecutionTimeout() = CommandExecution.Duration(500, TimeUnit.MILLISECONDS)

    @Provides
    @Singleton
    fun commandBus() = CommandBus()

    @Provides
    @Singleton
    fun folderCommandExecutor(
        commandBus: CommandBus,
        eventStore: EventStore,
        repository: AggregateRepository<Folder>
    ) = FolderCommandExecutor(
        commandBus,
        eventStore,
        repository,
        FolderCommandToEventMapper(),
        Schedulers.io()
    )

    @Provides
    @Singleton
    fun noteCommandExecutor(
        commandBus: CommandBus,
        eventStore: EventStore,
        repository: AggregateRepository<Note>
    ) = NoteCommandExecutor(
        commandBus,
        eventStore,
        repository,
        NoteCommandToEventMapper(),
        Schedulers.io()
    )

    @Provides
    @Singleton
    fun noteTitlePolicy(commandBus: CommandBus) =
        NoteTitlePolicy(
            commandBus,
            Schedulers.computation(),
            titleExtractor = ::extractTitleFromContent
        )
}
