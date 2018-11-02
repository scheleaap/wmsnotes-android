package info.maaskant.wmsnotes.android.client.indexing

import info.maaskant.wmsnotes.model.Event
import info.maaskant.wmsnotes.model.NoteCreatedEvent
import info.maaskant.wmsnotes.model.NoteDeletedEvent
import info.maaskant.wmsnotes.model.eventstore.EventStore
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FolderIndexTest {

    private val noteId = "note"
    private val title = "Title"

    private val scheduler = Schedulers.trampoline()

    private val eventStore: EventStore = mockk()

    private lateinit var state: FolderIndexState

    private lateinit var eventUpdatesSubject: PublishSubject<Event>

    @BeforeEach
    fun init() {
        eventUpdatesSubject = PublishSubject.create<Event>()
        state = FolderIndexState()
        clearMocks(
            eventStore
        )
        every { eventStore.getEvents(any()) }.returns(Observable.empty())
        every { eventStore.getEventUpdates() }.returns(eventUpdatesSubject as Observable<Event>)
    }

    @Test
    fun `note created`() {
        // Given
        val index = FolderIndex(eventStore, state, scheduler)

        // When
        val testObserver = index.getNodes(FolderIndex.rootNode).test()
        eventUpdatesSubject.onNext(NoteCreatedEvent(eventId = 0, noteId = noteId, revision = 1, title = title))

        // Then
        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
        assertThat(testObserver.values()).isEqualTo(
            listOf(
                emptyList(),
                listOf(Note(noteId, title))
            )
        )
    }

    @Test
    fun `note deleted`() {
        // Given
        val index = FolderIndex(eventStore, state, scheduler)

        // When
        val observer = index.getNodes(FolderIndex.rootNode).test()
        eventUpdatesSubject.onNext(NoteCreatedEvent(eventId = 0, noteId = noteId, revision = 1, title = title))
        eventUpdatesSubject.onNext(NoteDeletedEvent(eventId = 0, noteId = noteId, revision = 1))

        // Then
        observer.assertNotComplete()
        observer.assertNoErrors()
        assertThat(observer.values()).isEqualTo(
            listOf(
                emptyList(),
                listOf(Note(noteId, title)),
                emptyList<Node>()
            )
        )
    }

    @Test
    fun `only state`() {
        // Given
        state = FolderIndexState().addNode(Note("note-1", title), 1)
        val index = FolderIndex(eventStore, state, scheduler)

        // When
        val observer = index.getNodes(FolderIndex.rootNode).test()

        // Then
        observer.assertNotComplete()
        observer.assertNoErrors()
        assertThat(observer.values()).isEqualTo(
            listOf(
                listOf(
                    Note("note-1", title)
                )
            )
        )
    }

    @Test
    fun `combine state, getEvents() and getEventUpdates()`() {
        // Given
        state = FolderIndexState().addNode(Note("note-1", title), 1)
        every { eventStore.getEvents(afterEventId = 1) }.returns(
            Observable.just(
                NoteCreatedEvent(
                    2,
                    "note-2",
                    1,
                    title
                )
            )
        )
        val index = FolderIndex(eventStore, state, scheduler)

        // When
        val observer = index.getNodes(FolderIndex.rootNode).test()
        eventUpdatesSubject.onNext(NoteCreatedEvent(eventId = 3, noteId = "note-3", revision = 1, title = title))

        // Then
        observer.assertNotComplete()
        observer.assertNoErrors()
        assertThat(observer.values()).isEqualTo(
            listOf(
                /*listOf(
                    Note("note-1", title)
                ),*/ listOf(
                    Note("note-1", title),
                    Note("note-2", title)
                ),
                listOf(
                    Note("note-1", title),
                    Note("note-2", title),
                    Note("note-3", title)
                )
            )
        )
    }

    @Test
    fun `different folders`() {
        // Given
        val index = FolderIndex(eventStore, state, scheduler)

        // When
        val observer = index.getNodes(Folder("/bla", "")).test()
        eventUpdatesSubject.onNext(NoteCreatedEvent(eventId = 0, noteId = noteId, revision = 1, title = title))

        // Then
        observer.assertNotComplete()
        observer.assertNoErrors()
        assertThat(observer.values()).isEqualTo(
            listOf(
                emptyList<Node>()
            )
        )
    }

}
