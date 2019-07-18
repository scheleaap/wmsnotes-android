package info.maaskant.wmsnotes.android.ui.navigation

import android.os.Bundle
import androidx.lifecycle.ViewModel
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.StringsModule.StringId
import info.maaskant.wmsnotes.android.ui.navigation.NavigationViewModel.FolderTitleValidity.Invalid
import info.maaskant.wmsnotes.android.ui.navigation.NavigationViewModel.FolderTitleValidity.Valid
import info.maaskant.wmsnotes.client.indexing.Node
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.model.CommandBus
import info.maaskant.wmsnotes.model.CommandExecution
import info.maaskant.wmsnotes.model.Path
import info.maaskant.wmsnotes.model.folder.CreateFolderCommand
import info.maaskant.wmsnotes.model.folder.FolderCommandRequest
import info.maaskant.wmsnotes.model.note.CreateNoteCommand
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.model.note.NoteCommandRequest
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NavigationViewModel @Inject constructor(
    private val commandBus: CommandBus,
    private val treeIndex: TreeIndex,
    @StringId(R.string.new_note_title) private val newNoteTitle: String,
    @StringId(R.string.create_folder_dialog_error_title_must_not_be_empty) private val titleMustNotBeEmptyText: String,
    @StringId(R.string.create_folder_dialog_error_title_must_not_contain_slash) private val titleMustNotContainSlashText: String
) : ViewModel() {
    private val logger by logger()

    // TODO:
    // Implement onCleared to release subscriptions?

    private val stackSubject: BehaviorSubject<ImmutableStack<Path>> = BehaviorSubject.create()
    private lateinit var stackValue: ImmutableStack<Path>

    init {
        setStack(ImmutableStack.from(initialPath))
    }

    fun createFolder(title: String) {
        commandBus.requests.onNext(
            FolderCommandRequest.of(
                CreateFolderCommand(
                    path = stackValue.peek()!!.child(title)
                )
            )
        )
    }

    private fun createNavigationStack(stack: ImmutableStack<Path>, path: Path): ImmutableStack<Path> {
        return if (path != rootPath) {
            createNavigationStack(stack, path.parent()).push(path)
        } else {
            stack.push(path)
        }
    }

    fun createNote(): String? {
        return try {
            val commandResult = CommandExecution.executeBlocking(
                commandBus = commandBus,
                commandRequest = NoteCommandRequest.of(
                    CreateNoteCommand(
                        aggId = Note.randomAggId(),
                        path = stackValue.peek()!!,
                        title = newNoteTitle,
                        content = ""
                    )
                ),
                timeout = CommandExecution.Duration(500, TimeUnit.MILLISECONDS)
            )
            commandResult.newEvents.first().aggId
        } catch (e: RuntimeException) {
            logger.warn("Could not create new note", e)
            null
        }
    }

    fun getNodes(path: Path): Observable<List<Node>> {
        return Observable.concat(
            Observable.just(Unit),
            treeIndex.getEvents(filterByFolder = path)
                .map { Unit }
        )
            .map {
                treeIndex.getNodes(filterByFolder = path)
                    .map { it.value }.toList().blockingGet()
            }
    }

    fun getStack(): Observable<ImmutableStack<Path>> = stackSubject

    /**
     * @return The state of the view model that needs to be saved.
     */
    // Source: https://github.com/googlesamples/android-architecture/blob/dev-todo-mvvm-rxjava/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksFragment.java
    fun getStateToSave(): Bundle {
        val bundle = Bundle()
        bundle.putSerializable(CURRENT_PATH_KEY, stackValue.peek().toString())
        return bundle
    }

    fun isValidFolderTitle(title: String): FolderTitleValidity =
        when {
            title.isBlank() -> Invalid(titleMustNotBeEmptyText)
            title.contains('/') -> Invalid(titleMustNotContainSlashText)
            else -> Valid
        }

    fun navigateTo(path: Path) {
        val currentPath = stackValue.peek()
        if (path.parent() == currentPath) {
            logger.debug("Navigating to {}", path)
            setStack(stackValue.push(path))
        } else {
            throw IllegalArgumentException("'$path' is not a child of '$currentPath'")
        }
    }

    fun navigateUp(): Boolean {
        val currentPathValue = stackValue.peek()!!
        return if (currentPathValue != rootPath) {
            setStack(stackValue.pop().first)
            true
        } else {
            false
        }
    }

    /**
     * Restore the state of the view model based on a bundle.
     *
     * @param bundle The bundle containing the state.
     */
    // Source: https://github.com/googlesamples/android-architecture/blob/dev-todo-mvvm-rxjava/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksFragment.java
    fun restoreState(bundle: Bundle?) {
        if (bundle != null && bundle.containsKey(CURRENT_PATH_KEY)) {
            val path = Path.from(bundle.getString(CURRENT_PATH_KEY)!!)
            setStack(createNavigationStack(ImmutableStack.empty(), path))
        }
    }

    @Synchronized
    private fun setStack(stack: ImmutableStack<Path>) {
        this.stackValue = stack
        this.stackSubject.onNext(stack)
    }

    companion object {
        private const val CURRENT_PATH_KEY = "currentPath"
        private val rootPath = Path()
        private val initialPath = rootPath
    }

    sealed class FolderTitleValidity {
        object Valid : FolderTitleValidity()
        data class Invalid(val reason: String) : FolderTitleValidity()
    }
}
