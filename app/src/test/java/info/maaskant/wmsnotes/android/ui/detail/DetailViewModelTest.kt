package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import info.maaskant.wmsnotes.android.ui.detail.DetailViewModel.Update
import info.maaskant.wmsnotes.model.Path
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.note.ContentChangedEvent
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.model.note.NoteCreatedEvent
import info.maaskant.wmsnotes.model.note.TitleChangedEvent
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
    private val aggId = "n-10000000-0000-0000-0000-000000000000"
    private val path = Path()
    private val title = "Title"
    private val content = "Content"
    private val noteV1 = Note()
        .apply(
            NoteCreatedEvent(
                eventId = 1,
                aggId = aggId,
                revision = 1,
                path = path,
                title = title,
                content = content
            )
        )
        .component1()
    private val noteV2 = noteV1
        .apply(
            ContentChangedEvent(
                eventId = 2,
                aggId = aggId,
                revision = 2,
                content = "Different content"
            )
        ).component1()
        .apply(
            TitleChangedEvent(
                eventId = 3,
                aggId = aggId,
                revision = 3,
                title = "Different title"
            )
        ).component1()

    private val noteRepository: AggregateRepository<Note> = mockk()
    private val scheduler = Schedulers.trampoline()

    @BeforeEach
    fun init() {
        clearMocks(
            noteRepository
        )
    }

    @Test
    fun `default values`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()

        // When

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(emptyList<Boolean>())
        assertThat(noteObserver.values().toList()).isEqualTo(emptyList<Note>())
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(emptyList<String>())
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(emptyList<String>())
    }

    @Test
    fun initialize() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))

        // When
        model.setNote(aggId)

        // Then
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false))
        assertThat(noteObserver.values().toList()).isEqualTo(listOf(noteV1))
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromViewModel(noteV1.content)))
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromViewModel(noteV1.title)))
    }

    @Test
    fun `set note id twice`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val noteObserver = model.getNote().test()
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))

        // When
        model.setNote(aggId)
        model.setNote(aggId)

        // Then
        assertThat(noteObserver.values().toList()).hasSize(1)
    }

    @Test
    fun `note update`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)

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
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromViewModel(noteV1.content),
                updateFromViewModel(noteV2.content)
            )
        )
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromViewModel(noteV1.title),
                updateFromViewModel(noteV2.title)
            )
        )
    }

    @Test
    fun `note update when dirty, content`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setContentFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()

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
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromView("changed")))
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromViewModel(noteV1.title)))
    }

    @Test
    fun `note update when dirty, title`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setTitleFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()

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
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromViewModel(noteV1.content)))
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromView("changed")))
    }

    @Test
    fun `note update when dirty, resolving the dirty state, both`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setContentFromUser(noteV2.content)
        model.setTitleFromUser(noteV2.title)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()

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
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromView(noteV2.content)))
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromView(noteV2.title)))
    }

    @Test
    fun `note update when dirty, resolving the dirty state, content`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setContentFromUser(noteV2.content)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()

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
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromView(noteV2.content)))
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromViewModel(noteV1.title),
                updateFromViewModel(noteV2.title)
            )
        )
    }

    @Test
    fun `note update when dirty, resolving the dirty state, title`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setTitleFromUser(noteV2.title)
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()

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
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromViewModel(noteV1.content),
                updateFromViewModel(noteV2.content)
            )
        )
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromView(noteV2.title)))
    }

    @Test
    fun `note update when dirty, resolves content, title still dirty`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        val projectedNoteWithUpdates = givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setContentFromUser(noteV2.content)
        model.setTitleFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val noteObserver = model.getNote().test()
        val contentUpdatesObserver = model.getContentUpdates().test()
        val titleUpdatesObserver = model.getTitleUpdates().test()

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
        assertThat(contentUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromView(noteV2.content)))
        assertThat(titleUpdatesObserver.values().toList()).isEqualTo(listOf(updateFromView("changed")))
    }

    @Test
    fun `user change, content`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        val dirtyObserver = model.isDirty().test()
        val updatesObserver = model.getContentUpdates().test()

        // When
        model.setContentFromUser("changed")

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false, true))
        assertThat(updatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromViewModel(noteV1.content),
                updateFromView("changed")
            )
        )
    }

    @Test
    fun `user change, title`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        val dirtyObserver = model.isDirty().test()
        val updatesObserver = model.getTitleUpdates().test()

        // When
        model.setTitleFromUser("changed")

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false, true))
        assertThat(updatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromViewModel(noteV1.title),
                updateFromView("changed")
            )
        )
    }

    @Test
    fun `user change, the same value twice, content`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setContentFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val updatesObserver = model.getContentUpdates().test()

        // When
        model.setContentFromUser("changed")

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true))
        assertThat(updatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromView("changed")
            )
        )
    }

    @Test
    fun `user change, the same value twice, title`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setTitleFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val updatesObserver = model.getTitleUpdates().test()

        // When
        model.setTitleFromUser("changed")

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true))
        assertThat(updatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromView("changed")
            )
        )
    }

    @Test
    fun `user change, resolving the dirty state, content`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setContentFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val updatesObserver = model.getContentUpdates().test()

        // When
        model.setContentFromUser(noteV1.content)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true, false))
        assertThat(updatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromView("changed"),
                updateFromView(noteV1.content)
            )
        )
    }

    @Test
    fun `user change, resolving the dirty state, title`() {
        // Given
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        model.setTitleFromUser("changed")
        val dirtyObserver = model.isDirty().test()
        val updatesObserver = model.getTitleUpdates().test()

        // When
        model.setTitleFromUser(noteV1.title)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(true, false))
        assertThat(updatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromView("changed"),
                updateFromView(noteV1.title)
            )
        )
    }

    @Test
    fun `restore state`() {
        // Given
        val bundle: Bundle = mockk()
        every { bundle.containsKey("content") }.returns(true)
        every { bundle.getString("content") }.returns("Bundle Content")
        val model = DetailViewModel(noteRepository, ioScheduler = scheduler, computationScheduler = scheduler)
        givenAProjectedNoteWithUpdates(aggId, Observable.just(noteV1))
        model.setNote(aggId)
        val dirtyObserver = model.isDirty().test()
        val updatesObserver = model.getContentUpdates().test()

        // When
        model.restoreState(bundle)

        // Then
        assertThat(dirtyObserver.values().toList()).isEqualTo(listOf(false, true))
        assertThat(updatesObserver.values().toList()).isEqualTo(
            listOf(
                updateFromViewModel(noteV1.content),
                updateFromViewModel("Bundle Content")
            )
        )
    }

    private fun givenAProjectedNoteWithUpdates(
        aggId: String,
        immediateResponse: Observable<Note>
    ): PublishSubject<Note> {
        val projectedNoteWithUpdates: PublishSubject<Note> = PublishSubject.create()
        every { noteRepository.getAndUpdate(aggId) }.returns(Observable.defer {
            Observable.concat(
                immediateResponse,
                projectedNoteWithUpdates
            )
        })
        return projectedNoteWithUpdates
    }

    private fun updateFromViewModel(value: String) = Update(value, Update.Origin.VIEW_MODEL)
    private fun updateFromView(value: String) = Update(value, Update.Origin.VIEW)
}
