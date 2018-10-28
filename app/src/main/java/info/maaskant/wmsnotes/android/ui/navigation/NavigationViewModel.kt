package info.maaskant.wmsnotes.android.ui.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.android.model.Node
import info.maaskant.wmsnotes.android.model.Note
import info.maaskant.wmsnotes.model.eventstore.EventStore
import io.reactivex.Flowable
import timber.log.Timber

class NavigationViewModel(private val eventStore: EventStore) : ViewModel() {
    private lateinit var liveData: LiveData<List<Node>>

    fun getNotes(): LiveData<List<Node>> {
        if (!::liveData.isInitialized) {
            liveData = getReactiveStuff().toLiveData()
        }
        return liveData
    }

    fun getReactiveStuff(): Flowable<List<Node>> {
        Timber.i("%s", eventStore)
        val events = eventStore.getEvents().toList().blockingGet()
        Timber.i("%s", events)
        return Flowable.just(listOf(Note("Wout"), Note("Henk"), Note("Jan")))
    }

    fun navigateTo(node: Node) {
        Timber.d("Navigating to %s", node.title)
    }
}
