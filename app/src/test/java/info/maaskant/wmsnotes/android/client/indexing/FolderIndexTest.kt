package info.maaskant.wmsnotes.android.client.indexing

import info.maaskant.wmsnotes.model.Event
import info.maaskant.wmsnotes.model.Path
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.model.note.NoteCreatedEvent
import info.maaskant.wmsnotes.model.note.NoteDeletedEvent
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
    private val aggId = "note"
    private val rootPath = Path()
    private val title = "Title"
    private val content = "Text"
    private val noteCreatedEvent =
        NoteCreatedEvent(eventId = 0, aggId = aggId, revision = 1, path = rootPath, title = title, content = content)

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
        eventUpdatesSubject.onNext(noteCreatedEvent)

        // Then
        testObserver.assertNotComplete()
        testObserver.assertNoErrors()
        assertThat(testObserver.values()).isEqualTo(
            listOf(
                emptyList(),
                listOf(Note(aggId, title))
            )
        )
    }

    @Test
    fun `note deleted`() {
        // Given
        val index = FolderIndex(eventStore, state, scheduler)

        // When
        val observer = index.getNodes(FolderIndex.rootNode).test()
        eventUpdatesSubject.onNext(noteCreatedEvent)
        eventUpdatesSubject.onNext(NoteDeletedEvent(eventId = 0, aggId = aggId, revision = 1))

        // Then
        observer.assertNotComplete()
        observer.assertNoErrors()
        assertThat(observer.values()).isEqualTo(
            listOf(
                emptyList(),
                listOf(Note(aggId, title)),
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
                    eventId = 2,
                    aggId = "note-2",
                    path = rootPath,
                    revision = 1,
                    title = title,
                    content = content
                )
            )
        )
        val index = FolderIndex(eventStore, state, scheduler)

        // When
        val observer = index.getNodes(FolderIndex.rootNode).test()
        eventUpdatesSubject.onNext(
            NoteCreatedEvent(
                eventId = 3,
                aggId = "note-3",
                revision = 1,
                path = rootPath,
                title = title,
                content = content
            )
        )

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
        eventUpdatesSubject.onNext(noteCreatedEvent)

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
