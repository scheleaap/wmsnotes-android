package info.maaskant.wmsnotes.android.ui.detail

import info.maaskant.wmsnotes.model.Command
import info.maaskant.wmsnotes.model.CommandProcessor
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
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DetailControllerTest {
    private val aggId = "note"
    private val path = Path()
    private val title = "Title"
    private val content = "Text"
    private val note = Note()
        .apply(NoteCreatedEvent(eventId = 1, aggId = aggId, revision = 1, path = path, title = title, content = ""))
        .component1()
        .apply(ContentChangedEvent(eventId = 2, aggId = aggId, revision = 2, content = content)).component1()

    private val detailViewModel: DetailViewModel = mockk()
    private lateinit var isDirtySubject: BehaviorSubject<Boolean>
    private lateinit var textUpdatesSubject: BehaviorSubject<TextUpdate>

    private val commandProcessor: CommandProcessor = mockk()
    private lateinit var commandsObserver: TestObserver<Command>

    private val detailActivity: DetailActivity = mockk()

    private val scheduler = Schedulers.trampoline()

    @BeforeEach
    fun init() {
        clearMocks(
            detailViewModel,
            commandProcessor,
            detailActivity
        )

        isDirtySubject = BehaviorSubject.create()
        every { detailViewModel.isDirty() }.returns(isDirtySubject)
        textUpdatesSubject = BehaviorSubject.create()
        every { detailViewModel.getTextUpdates() }.returns(textUpdatesSubject)

        val commandsSubject: Subject<Command> = PublishSubject.create()
        every { commandProcessor.commands }.returns(commandsSubject)
        commandsObserver = commandsSubject.test()

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
        assertThat(commandsObserver.values().toList()).isEqualTo(emptyList<Command>())
        verify(exactly = 0) { detailActivity.finish() }

        // When
        controller.saveAndQuit()

        // Then
        assertThat(commandsObserver.values().toList()).isEqualTo(emptyList<Command>())
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
        assertThat(commandsObserver.values().toList()).isEqualTo(
            listOf(
                ChangeContentCommand(aggId = note.aggId, lastRevision = note.revision, content = changedText)
            )
        )
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
        assertThat(commandsObserver.values().toList()).isEqualTo(emptyList<Command>())
        verify(exactly = 0) { detailActivity.finish() }

        // When
        controller.quit()

        // Then
        assertThat(commandsObserver.values().toList()).isEqualTo(emptyList<Command>())
        verify { detailActivity.finish() }
    }

    private fun createInstance() =
        DetailController(
            detailViewModel,
            commandProcessor,
            detailActivity,
            computationScheduler = scheduler,
            uiScheduler = scheduler
        ).apply {
            this.onCreate()
        }

    private fun givenALoadedNote(note: Note) {
        every { detailViewModel.getNote() }.returns(Observable.just(note))
        textUpdatesSubject.onNext(TextUpdate(note.content, TextUpdate.Source.SYSTEM))
    }

    private fun givenADirtyViewModel(text: String) {
        isDirtySubject.onNext(true)
        textUpdatesSubject.onNext(TextUpdate(text, TextUpdate.Source.USER))
    }

    private fun givenANotDirtyViewModel() {
        isDirtySubject.onNext(false)
    }
}
