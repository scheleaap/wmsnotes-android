package info.maaskant.wmsnotes.android.ui.navigation

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.android.model.Node
import info.maaskant.wmsnotes.android.model.Note
import io.reactivex.Flowable
import io.reactivex.Observable
import timber.log.Timber

class NavigationViewModel : ViewModel() {
    private lateinit var liveData : LiveData<List<Node>>

    fun getNotes(): LiveData<List<Node>> {
        if (!::liveData.isInitialized) {
            liveData = getReactiveStuff().toLiveData()
        }
        return liveData
    }

    fun getReactiveStuff() : Flowable<List<Node>> {
        return Flowable.just(listOf(Note("Wout"), Note("Henk"), Note("Jan")))
    }

    fun navigateTo(node: Node) {
        Timber.d("Navigating to %s", node.title)
    }
}
