package info.maaskant.wmsnotes.android.ui.navigation

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.client.indexing.Node
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.model.Path
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject


class NavigationViewModel @Inject constructor(
    private val treeIndex: TreeIndex
) : ViewModel() {

    private val CURRENT_PATH_KEY = "currentPath"

    // TODO:
    // Implement onCleared to release subscriptions?

    private val initialPath = Path()

    private val currentPath: BehaviorSubject<Path> = BehaviorSubject.createDefault(initialPath)

    fun getCurrentPath(): LiveData<Path> =
        currentPath
            .toFlowable(BackpressureStrategy.ERROR)
            .toLiveData()

    fun getNotes(path: Path): LiveData<List<Node>> = getNotesInternal(path).toLiveData()

    private fun getNotesInternal(path: Path): Flowable<List<Node>> {
        return Observable.concat(
            Observable.just(Unit),
            treeIndex.getEvents(filterByFolder = path)
                .map { Unit }
        )
            .map {
                treeIndex.getNodes(filterByFolder = path)
                    .map { it.value }.toList().blockingGet()
            }
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun navigateTo(path: Path) {
        Timber.d("Navigating to %s", path)
        currentPath.onNext(path)
    }

    /**
     * @return The state of the view model that needs to be saved.
     */
    // Source: https://github.com/googlesamples/android-architecture/blob/dev-todo-mvvm-rxjava/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksFragment.java
    fun getStateToSave(): Bundle {
        val bundle = Bundle()
        bundle.putSerializable(CURRENT_PATH_KEY, currentPath.value?.toString() ?: "")
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
            currentPath.onNext(path)
        }
    }

}
