package info.maaskant.wmsnotes.android.ui.navigation

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
import timber.log.Timber
import javax.inject.Inject

class NavigationViewModel @Inject constructor(
    private val treeIndex: TreeIndex
) : ViewModel() {

    // TODO:
    // Implement onCleared to release subscriptions?

    val notes: LiveData<List<Node>> by lazy {
        getNotesInternal().toLiveData()
    }

    private fun getNotesInternal(): Flowable<List<Node>> {
        val path = Path()
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

    fun navigateTo(node: Node) {
        Timber.d("Navigating to %s", node.title)
    }
}
