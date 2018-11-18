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
import javax.inject.Inject

class NavigationViewModel @Inject constructor(
    private val folderIndex: FolderIndex
) : ViewModel() {

    // TODO:
    // Implement onCleared to release subscriptions

    private lateinit var liveData: LiveData<List<Node>>

    fun getNotes(): LiveData<List<Node>> {
        if (!::liveData.isInitialized) {
            liveData = getNotesInternal().toLiveData()
        }
        return liveData
    }

    private fun getNotesInternal(): Flowable<List<Node>> {
        return folderIndex.getNodes(FolderIndex.rootNode)
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun navigateTo(node: Node) {
        Timber.d("Navigating to %s", node.title)
    }
}
