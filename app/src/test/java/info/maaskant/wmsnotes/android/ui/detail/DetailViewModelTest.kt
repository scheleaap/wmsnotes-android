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
    private val note1Id = "note-1"
    private val note2Id = "note-2"
    private val title = "Title"
    private val text = "Text"
    private val note1v1 = Note()
        .apply(NoteCreatedEvent(eventId = 1, noteId = note1Id, revision = 1, title = title)).component1()
        .apply(ContentChangedEvent(eventId = 2, noteId = note1Id, revision = 2, content = text)).component1()
    private val note1v2 = note1v1
        .apply(
            ContentChangedEvent(
                eventId = 3,
                noteId = note1Id,
                revision = 3,
                content = "Different text"
            )
        ).component1()

    private val noteProjector: NoteProjector = mockk()
    //    private val renderer: Renderer = mockk()
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
        val textObserver = model.getText().test()
        val textUpdatesForEditorObserver = model.getTextUpdatesForEditor().test()
        val titleObserver = model.getTitle().test()

        // When

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(emptyList<Boolean>())
        assertThat(noteObserver.values().toList()).isEqualTo(emptyList<info.maaskant.wmsnotes.model.projection.Note>())
        assertThat(textObserver.values().toList()).isEqualTo(emptyList<String>())
        assertThat(textUpdatesForEditorObserver.values().toList()).isEqualTo(emptyList<String>())
        assertThat(titleObserver.values().toList()).isEqualTo(emptyList<String>())
    }

    @Test
    fun initialize() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(note1Id, Observable.just(note1v1))

        // When
        model.setNote(note1Id)

        // Then
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textObserver = model.getText().test()
        val textUpdatesForEditorObserver = model.getTextUpdatesForEditor().test()
        val titleObserver = model.getTitle().test()
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false))
        assertThat(noteObserver.values().toList()).isEqualTo(listOf(note1v1))
        assertThat(textObserver.values().toList()).isEqualTo(listOf(note1v1.content))
        assertThat(textUpdatesForEditorObserver.values().toList()).isEqualTo(listOf(note1v1.content))
        assertThat(titleObserver.values().toList()).isEqualTo(listOf(note1v1.title))
    }

    @Test
    fun `set note id twice`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val noteObserver = model.getNote().test()
        givenAProjectedNoteWithUpdates(note1Id, Observable.just(note1v1))

        // When
        model.setNote(note1Id)
        model.setNote(note1Id)

        // Then
        assertThat(noteObserver.values().toList()).hasSize(1)
    }

    @Test
    fun `note update`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textObserver = model.getText().test()
        val textUpdatesForEditorObserver = model.getTextUpdatesForEditor().test()
        val titleObserver = model.getTitle().test()
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(note1Id, Observable.just(note1v1))
        model.setNote(note1Id)

        // When
        projectedNoteWithUpdates.onNext(note1v2)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false))
        assertThat(noteObserver.values().toList()).isEqualTo(
            listOf(
                note1v1,
                note1v2
            )
        )
        assertThat(textObserver.values().toList()).isEqualTo(listOf(note1v1.content, note1v2.content))
        assertThat(textUpdatesForEditorObserver.values().toList()).isEqualTo(listOf(note1v1.content, note1v2.content))
        assertThat(note1v1.title).isEqualTo(note1v2.title)
        assertThat(titleObserver.values().toList()).isEqualTo(listOf(note1v1.title))
    }

    @Test
    fun `note update when dirty`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(note1Id, Observable.just(note1v1))
        model.setNote(note1Id)
        model.setText("changed")
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textObserver = model.getText().test()
        val textUpdatesForEditorObserver = model.getTextUpdatesForEditor().test()
        val titleObserver = model.getTitle().test()

        // When
        projectedNoteWithUpdates.onNext(note1v2)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true))
        assertThat(noteObserver.values().toList()).isEqualTo(
            listOf(
                note1v1,
                note1v2
            )
        )
        assertThat(textObserver.values().toList()).isEqualTo(listOf("changed"))
        assertThat(textUpdatesForEditorObserver.values().toList()).isEqualTo(listOf(note1v1.content))
        assertThat(titleObserver.values().toList()).isEqualTo(listOf(note1v1.title))
    }

    @Test
    fun `note update when dirty, resolving the dirty state`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(note1Id, Observable.just(note1v1))
        model.setNote(note1Id)
        model.setText(note1v2.content)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val textObserver = model.getText().test()
        val textUpdatesForEditorObserver = model.getTextUpdatesForEditor().test()

        // When
        projectedNoteWithUpdates.onNext(note1v2)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true, false))
        assertThat(noteObserver.values().toList()).isEqualTo(
            listOf(
                note1v1,
                note1v2
            )
        )
        assertThat(textObserver.values().toList()).isEqualTo(listOf(note1v2.content))
        assertThat(textUpdatesForEditorObserver.values().toList()).isEqualTo(listOf(note1v1.content))
    }

    @Test
    fun `text and isDirty, normal`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(note1Id, Observable.just(note1v1))
        model.setNote(note1Id)
        val dirtyObserver = model.isDirty().test()
        val textObserver = model.getText().test()
        val textUpdatesForEditorObserver = model.getTextUpdatesForEditor().test()

        // When
        model.setText("changed")

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false, true))
        assertThat(textObserver.values().toList()).isEqualTo(listOf(note1v1.content, "changed"))
        assertThat(textUpdatesForEditorObserver.values().toList()).isEqualTo(listOf(note1v1.content))
    }

    @Test
    fun `text and isDirty, the same text twice`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(note1Id, Observable.just(note1v1))
        model.setNote(note1Id)
        model.setText("changed")
        val dirtyObserver = model.isDirty().test()
        val textObserver = model.getText().test()
        val textUpdatesForEditorObserver = model.getTextUpdatesForEditor().test()

        // When
        model.setText("changed")

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true))
        assertThat(textObserver.values().toList()).isEqualTo(listOf("changed"))
        assertThat(textUpdatesForEditorObserver.values().toList()).isEqualTo(listOf(note1v1.content))
    }

    @Test
    fun `text and isDirty, resolving the dirty state`() {
        // Given
        val model = DetailViewModel(noteProjector, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(note1Id, Observable.just(note1v1))
        model.setNote(note1Id)
        model.setText("changed")
        val dirtyObserver = model.isDirty().test()
        val textObserver = model.getText().test()
        val textUpdatesForEditorObserver = model.getTextUpdatesForEditor().test()

        // When
        model.setText(note1v1.content)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true, false))
        assertThat(textObserver.values().toList()).isEqualTo(listOf("changed", note1v1.content))
        assertThat(textUpdatesForEditorObserver.values().toList()).isEqualTo(listOf(note1v1.content))
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
}
