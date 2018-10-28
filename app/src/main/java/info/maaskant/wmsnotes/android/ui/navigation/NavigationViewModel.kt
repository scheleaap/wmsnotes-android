package info.maaskant.wmsnotes.android.ui.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.android.model.Node
import info.maaskant.wmsnotes.android.model.Note
import info.maaskant.wmsnotes.client.indexing.NoteIndex
import info.maaskant.wmsnotes.model.Event
import info.maaskant.wmsnotes.model.NoteCreatedEvent
import info.maaskant.wmsnotes.model.NoteDeletedEvent
import info.maaskant.wmsnotes.model.eventstore.EventStore
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observables.ConnectableObservable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class NavigationViewModel(
    private val eventStore: EventStore,
    private val noteIndex: NoteIndex
) : ViewModel() {

    // TODO:
    // Implement onCleared to release subscriptions

    private lateinit var liveData: LiveData<List<Node>>

    val allEventsWithUpdates: Observable<Event> =
//        noteIndex.getNotes()
    eventStore.getEvents()
        .subscribeOn(Schedulers.io())
//        .map { NoteCreatedEvent(0, it.noteId, 0, it.title) as Event }
        .mergeWith(eventStore.getEventUpdates())

    fun getNotes(): LiveData<List<Node>> {
        if (!::liveData.isInitialized) {
            liveData = getReactiveStuff().toLiveData()
        }
        return liveData
    }

    fun getReactiveStuff(): Flowable<List<Node>> {
//        return Flowable.just(listOf(Note("Wout"), Note("Henk"), Note("Jan")))
        return allEventsWithUpdates
            .toFlowable(BackpressureStrategy.ERROR)
            .scan(emptyMap<String, Node>()) { nodes: Map<String, Node>, event: Event ->
                when (event) {
                    is NoteCreatedEvent -> nodes + (event.noteId to Note(title = event.title))
                    is NoteDeletedEvent -> nodes - event.noteId
                    else -> nodes
                }
            }
            .map { it.values }
            .map { it.toList() }
            .observeOn(AndroidSchedulers.mainThread())
//            .doOnSubscribe {
//                eventStore.appendEvent(NoteCreatedEvent(0, "n2", 1, "Note 2"))
//                eventStore.appendEvent(NoteDeletedEvent(0, "n1", 2))
//            }

    }

    fun navigateTo(node: Node) {
        Timber.d("Navigating to %s", node.title)
    }
}
