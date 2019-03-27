package info.maaskant.wmsnotes.android.client.indexing

import android.annotation.SuppressLint
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.note.NoteCreatedEvent
import info.maaskant.wmsnotes.model.note.NoteDeletedEvent
import info.maaskant.wmsnotes.utilities.logger
import info.maaskant.wmsnotes.utilities.persistence.StateProducer
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@SuppressLint("CheckResult")
class FolderIndex @Inject constructor(
    private val eventStore: EventStore,
    initialState: FolderIndexState?,
    scheduler: Scheduler
) : StateProducer<FolderIndexState> {

    private val logger by logger()

    private var state = initialState ?: FolderIndexState()
    private val stateSubject: BehaviorSubject<FolderIndexState> = BehaviorSubject.create()

    init {
        stateSubject.onNext(state)

        Observable.concat(
            eventStore.getEvents(afterEventId = state.lastEventId),
            eventStore.getEventUpdates()
        )
            .subscribeOn(scheduler)
            .subscribe({
                when (it) {
                    is NoteCreatedEvent -> {
                        logger.debug("Adding note ${it.aggId} to index")
                        updateState(state.addNode(Note(it.aggId, it.title), it.eventId))
                    }
                    is NoteDeletedEvent -> {
                        logger.debug("Removing note ${it.aggId} from index")
                        updateState(state.removeNode(it.aggId, it.eventId))
                    }
                    else -> {
                    }
                }
            }, { logger.warn("Error", it) })
    }

    fun getNodes(folder: Folder): Observable<List<Node>> {
        return stateSubject
            .map { it.getNodes(folder) }
            .scan(true to null) { previous: Pair<Boolean, List<Node>?>, new: List<Node> -> (new == previous.second) to new }
            .filter { !it.first }
            .map { it.second }
    }

    private fun updateState(state: FolderIndexState) {
        this.state = state
        stateSubject.onNext(state)
    }

    override fun getStateUpdates(): Observable<FolderIndexState> = stateSubject.skip(1)

    companion object {
        val rootNode = Folder("/", "Root")
    }
}

