package info.maaskant.wmsnotes.android.ui.detail

import info.maaskant.wmsnotes.model.ContentChangedEvent
import info.maaskant.wmsnotes.model.NoteCreatedEvent
import info.maaskant.wmsnotes.model.projection.Note
import info.maaskant.wmsnotes.model.projection.NoteProjector
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DetailViewModelTest {
    private val noteId = "note"
    private val title = "Title"
    private val text = "Text"
    private val noteV1 = Note()
        .apply(NoteCreatedEvent(eventId = 1, noteId = noteId, revision = 1, title = title)).component1()
        .apply(ContentChangedEvent(eventId = 2, noteId = noteId, revision = 2, content = text)).component1()
    private val noteV2 = noteV1
        .apply(
            ContentChangedEvent(
                eventId = 3,
                noteId = noteId,
                revision = 3,
                content = "Different text"
            )
        ).component1()

    private val noteProjector: NoteProjector = mockk()
    private val scheduler = Schedulers.trampoline()

    @BeforeEach
    fun init() {
        clearMocks(
            noteProjector
        )
    }

    @Test
    fun `default values`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textUpdatesObserver = model.getTextUpdates().test()
        val titleObserver = model.getTitle().test()

        // When

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(emptyList<Boolean>())
        assertThat(noteObserver.values().toList()).isEqualTo(emptyList<info.maaskant.wmsnotes.model.projection.Note>())
        assertThat(textUpdatesObserver.values().toList()).isEqualTo(emptyList<String>())
        assertThat(titleObserver.values().toList()).isEqualTo(emptyList<String>())
    }

    @Test
    fun initialize() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(noteId, Observable.just(noteV1))

        // When
        model.setNote(noteId)

        // Then
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textUpdatesObserver = model.getTextUpdates().test()
        val titleObserver = model.getTitle().test()
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false))
        assertThat(noteObserver.values().toList()).isEqualTo(listOf(noteV1))
        assertThat(textUpdatesObserver.values().toList()).isEqualTo(listOf(systemUpdate(noteV1.content)))
        assertThat(titleObserver.values().toList()).isEqualTo(listOf(noteV1.title))
    }

    @Test
    fun `set note id twice`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val noteObserver = model.getNote().test()
        givenAProjectedNoteWithUpdates(noteId, Observable.just(noteV1))

        // When
        model.setNote(noteId)
        model.setNote(noteId)

        // Then
        assertThat(noteObserver.values().toList()).hasSize(1)
    }

    @Test
    fun `note update`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textUpdatesObserver = model.getTextUpdates().test()
        val titleObserver = model.getTitle().test()
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(noteId, Observable.just(noteV1))
        model.setNote(noteId)

        // When
        projectedNoteWithUpdates.onNext(noteV2)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false))
        assertThat(noteObserver.values().toList()).isEqualTo(
            listOf(
                noteV1,
                noteV2
            )
        )
        assertThat(textUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                systemUpdate(noteV1.content),
                systemUpdate(noteV2.content)
            )
        )
        assertThat(noteV1.title).isEqualTo(noteV2.title)
        assertThat(titleObserver.values().toList()).isEqualTo(listOf(noteV1.title))
    }

    @Test
    fun `note update when dirty`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(noteId, Observable.just(noteV1))
        model.setNote(noteId)
        model.setTextFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textUpdatesObserver = model.getTextUpdates().test()
        val titleObserver = model.getTitle().test()

        // When
        projectedNoteWithUpdates.onNext(noteV2)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true))
        assertThat(noteObserver.values().toList()).isEqualTo(
            listOf(
                noteV1,
                noteV2
            )
        )
        assertThat(textUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                userUpdate("changed")
            )
        )
        assertThat(titleObserver.values().toList()).isEqualTo(listOf(noteV1.title))
    }

    @Test
    fun `note update when dirty, resolving the dirty state`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(noteId, Observable.just(noteV1))
        model.setNote(noteId)
        model.setTextFromUser(noteV2.content)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textUpdatesObserver = model.getTextUpdates().test()

        // When
        projectedNoteWithUpdates.onNext(noteV2)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true, false))
        assertThat(noteObserver.values().toList()).isEqualTo(
            listOf(
                noteV1,
                noteV2
            )
        )
        assertThat(textUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                userUpdate(noteV2.content)
            )
        )
    }

    @Test
    fun `text and isDirty, normal`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(noteId, Observable.just(noteV1))
        model.setNote(noteId)
        val dirtyObserver = model.isDirty().test()
        val textUpdatesObserver = model.getTextUpdates().test()

        // When
        model.setTextFromUser("changed")

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false, true))
        assertThat(textUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                systemUpdate(noteV1.content),
                userUpdate("changed")
            )
        )
    }

    @Test
    fun `text and isDirty, the same text twice`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(noteId, Observable.just(noteV1))
        model.setNote(noteId)
        model.setTextFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val textUpdatesObserver = model.getTextUpdates().test()

        // When
        model.setTextFromUser("changed")

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true))
        assertThat(textUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                userUpdate("changed")
            )
        )
    }

    @Test
    fun `text and isDirty, resolving the dirty state`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(noteId, Observable.just(noteV1))
        model.setNote(noteId)
        model.setTextFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val textUpdatesObserver = model.getTextUpdates().test()

        // When
        model.setTextFromUser(noteV1.content)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true, false))
        assertThat(textUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                userUpdate("changed"),
                userUpdate(noteV1.content)
            )
        )
    }

    private fun givenAProjectedNoteWithUpdates(
        noteId: String,
        immediateResponse: Observable<Note>
    ): PublishSubject<Note> {
        val projectedNoteWithUpdates: PublishSubject<Note> = PublishSubject.create()
        every { noteProjector.projectAndUpdate(noteId) }.returns(Observable.defer {
            Observable.concat(
                immediateResponse,
                projectedNoteWithUpdates
            )
        })
        return projectedNoteWithUpdates
    }

    private fun systemUpdate(text: String) = TextUpdate(text, TextUpdate.Source.SYSTEM)
    private fun userUpdate(text: String) = TextUpdate(text, TextUpdate.Source.USER)
}
