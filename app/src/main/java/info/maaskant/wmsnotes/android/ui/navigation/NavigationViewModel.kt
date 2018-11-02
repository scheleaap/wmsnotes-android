package info.maaskant.wmsnotes.android.ui.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.android.client.indexing.FolderIndex
import info.maaskant.wmsnotes.android.client.indexing.Node
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class NavigationViewModel(
    private val folderIndex: FolderIndex
) : ViewModel() {

    // TODO:
    // Implement onCleared to release subscriptions

    private lateinit var liveData: LiveData<List<Node>>

    fun getNotes(): LiveData<List<Node>> {
        if (!::liveData.isInitialized) {
            liveData = getReactiveStuff().toLiveData()
        }
        return liveData
    }

    fun getReactiveStuff(): Flowable<List<Node>> {
        return folderIndex.getNodes(FolderIndex.rootNode)
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
//            .doOnSubscribe {
//                eventStore.appendEvent(NoteCreatedEvent(0, "n1", 1, "Note 1"))
//                eventStore.appendEvent(NoteCreatedEvent(0, "n2", 1, "Note 2"))
//                eventStore.appendEvent(NoteDeletedEvent(0, "n1", 2))
//            }
    }

    fun navigateTo(node: Node) {
        Timber.d("Navigating to %s", node.title)
    }
}
