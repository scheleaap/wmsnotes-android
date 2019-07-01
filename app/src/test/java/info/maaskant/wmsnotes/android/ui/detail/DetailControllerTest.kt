package info.maaskant.wmsnotes.android.ui.detail

import info.maaskant.wmsnotes.android.ui.detail.DetailViewModel.Update
import info.maaskant.wmsnotes.model.Command
import info.maaskant.wmsnotes.model.CommandBus
import info.maaskant.wmsnotes.model.CommandRequest
import info.maaskant.wmsnotes.model.Path
import info.maaskant.wmsnotes.model.note.ChangeContentCommand
import info.maaskant.wmsnotes.model.note.ContentChangedEvent
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.model.note.NoteCreatedEvent
import io.mockk.*
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DetailControllerTest {
    private val aggId = "n-10000000-0000-0000-0000-000000000000"
    private val path = Path()
    private val title = "Title"
    private val content = "Text"
    private val note = Note()
        .apply(NoteCreatedEvent(eventId = 1, aggId = aggId, revision = 1, path = path, title = title, content = ""))
        .component1()
        .apply(ContentChangedEvent(eventId = 2, aggId = aggId, revision = 2, content = content)).component1()

    private val detailViewModel: DetailViewModel = mockk()
    private lateinit var isDirtySubject: BehaviorSubject<Boolean>
    private lateinit var textUpdatesSubject: BehaviorSubject<Update>

    private val commandBus: CommandBus = CommandBus()
    private lateinit var commandRequestsObserver: TestObserver<CommandRequest<Command>>

    private val detailActivity: DetailActivity = mockk()

    private val scheduler = Schedulers.trampoline()

    @BeforeEach
    fun init() {
        clearMocks(
            detailViewModel,
            detailActivity
        )

        isDirtySubject = BehaviorSubject.create()
        every { detailViewModel.isDirty() }.returns(isDirtySubject)
        textUpdatesSubject = BehaviorSubject.create()
        every { detailViewModel.getContentUpdates() }.returns(textUpdatesSubject)

        commandRequestsObserver = commandBus.requests.test()

        every { detailActivity.finish() }.just(Runs)
    }

    @Test
    fun `save and quit, not dirty`() {
        // Given
        givenALoadedNote(note)
        givenANotDirtyViewModel()
        val controller = createInstance()

        // When
        // nothing

        // Then
        assertThat(commandRequestsObserver.values().toList()).isEqualTo(emptyList<Command>())
        verify(exactly = 0) { detailActivity.finish() }

        // When
        controller.saveAndQuit()

        // Then
        assertThat(commandRequestsObserver.values().toList()).isEqualTo(emptyList<Command>())
        verify { detailActivity.finish() }
    }

    @Test
    fun `save and quit, dirty`() {
        // Given
        givenALoadedNote(note)
        val changedText = note.content + "!"
        givenADirtyViewModel(changedText)
        val controller = createInstance()

        // When
        controller.saveAndQuit()

        // Then
        commandRequestsObserver.assertNoErrors()
        commandRequestsObserver.assertValueCount(1)
        val request = commandRequestsObserver.values()[0]
        assertThat(request.aggId).isEqualTo(note.aggId)
        assertThat(request.commands).isEqualTo(listOf(ChangeContentCommand(aggId = note.aggId, content = changedText)))
        assertThat(request.lastRevision).isEqualTo(note.revision)
        verify(exactly = 0) { detailActivity.finish() }

        // When
        givenANotDirtyViewModel()

        // Then
        verify { detailActivity.finish() }
    }

    @Test
    fun `regular quit, not dirty`() {
        // Given
        givenALoadedNote(note)
        givenANotDirtyViewModel()
        val controller = createInstance()

        // When
        // nothing

        // Then
        assertThat(commandRequestsObserver.values().toList()).isEqualTo(emptyList<Command>())
        verify(exactly = 0) { detailActivity.finish() }

        // When
        controller.quit()

        // Then
        assertThat(commandRequestsObserver.values().toList()).isEqualTo(emptyList<Command>())
        verify { detailActivity.finish() }
    }

    private fun createInstance() =
        DetailController(
            detailViewModel,
            commandBus,
            detailActivity,
            computationScheduler = scheduler,
            uiScheduler = scheduler
        ).apply {
            this.onCreate()
        }

    private fun givenALoadedNote(note: Note) {
        every { detailViewModel.getNote() }.returns(Observable.just(note))
        textUpdatesSubject.onNext(Update(note.content, Update.Origin.VIEW_MODEL))
    }

    private fun givenADirtyViewModel(text: String) {
        isDirtySubject.onNext(true)
        textUpdatesSubject.onNext(Update(text, Update.Origin.VIEW))
    }

    private fun givenANotDirtyViewModel() {
        isDirtySubject.onNext(false)
    }
}
