package info.maaskant.wmsnotes.android.ui.navigation

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.StringsModule.StringId
import info.maaskant.wmsnotes.client.indexing.Node
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.model.CommandProcessor
import info.maaskant.wmsnotes.model.Path
import info.maaskant.wmsnotes.model.note.CreateNoteCommand
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class NavigationViewModel @Inject constructor(
    private val commandProcessor: CommandProcessor,
    private val treeIndex: TreeIndex,
    @StringId(R.string.new_note_title) private val newNoteTitle: String
) : ViewModel() {

    // TODO:
    // Implement onCleared to release subscriptions?

    private val stackSubject: BehaviorSubject<ImmutableStack<Path>> = BehaviorSubject.create()
    private lateinit var stackValue: ImmutableStack<Path>

    init {
        setStack(ImmutableStack.from(initialPath))
    }

    fun createNote() {
        commandProcessor.commands.onNext(
            CreateNoteCommand(
                aggId = null,
                path = stackValue.peek()!!,
                title = newNoteTitle,
                content = ""
            )
        )
    }

    fun getNotes(path: Path): Observable<List<Node>> {
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

    fun navigateTo(path: Path) {
        val currentPath = stackValue.peek()
        if (path.parent() == currentPath) {
            Timber.d("Navigating to %s", path)
            setStack(stackValue.push(path))
        } else {
            throw IllegalArgumentException("'$path' is not a child of '$currentPath'")
        }
    }

    fun navigateUp(): Boolean {
        val currentPathValue = stackValue.peek()!!
        if (currentPathValue != rootPath) {
            setStack(stackValue.pop().first)
            return true
        } else {
            return false
        }
    }

    /**
     * @return The state of the view model that needs to be saved.
     */
    // Source: https://github.com/googlesamples/android-architecture/blob/dev-todo-mvvm-rxjava/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksFragment.java
    fun getStateToSave(): Bundle {
        val bundle = Bundle()
        bundle.putSerializable(CURRENT_PATH_KEY, stackValue.peek().toString())
        return bundle
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

    private fun createNavigationStack(stack: ImmutableStack<Path>, path: Path): ImmutableStack<Path> {
        return if (path != rootPath) {
            createNavigationStack(stack, path.parent()).push(path)
        } else {
            stack.push(path)
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
}
