package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class DetailViewModel @VisibleForTesting constructor(
    private val noteRepository: AggregateRepository<Note>,
    ioScheduler: Scheduler,
    computationScheduler: Scheduler
) : ViewModel() {
    private val logger by logger()

    private val isDirtySubject: Subject<Boolean> = BehaviorSubject.create()

    private var isContentDirtyValue: Boolean = false
    private var isTitleDirtyValue: Boolean = false

    private val noteId: Subject<String> = BehaviorSubject.create()

    private val noteSubject: Subject<Note> = BehaviorSubject.create()
    private var noteValue: Note? = null

    private var contentValue: String = ""

    private val contentUpdatesSubject: BehaviorSubject<Update> = BehaviorSubject.create()

    private var titleValue: String = ""

    private val titleUpdatesSubject: BehaviorSubject<Update> = BehaviorSubject.create()

    private val disposables = CompositeDisposable()

    init {
        disposables.add(noteId
            .observeOn(computationScheduler)
            .firstElement()
            .toObservable()
            .concatMap {
                noteRepository.getAndUpdate(it)
                    .subscribeOn(ioScheduler)
            }
            .observeOn(computationScheduler)
            .subscribe(::setNoteInternal) { logger.warn("Error", it) }
        )
    }

    @Inject
    constructor(noteRepository: AggregateRepository<Note>) : this(
        noteRepository,
        ioScheduler = Schedulers.io(),
        computationScheduler = Schedulers.computation()
    )

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun isDirty(): Observable<Boolean> = isDirtySubject.distinctUntilChanged()

    @Synchronized
    private fun setContentDirty(dirty: Boolean) {
        this.isContentDirtyValue = dirty
        this.isDirtySubject.onNext(isContentDirtyValue || isTitleDirtyValue)
        println(dirty)
    }

    @Synchronized
    private fun setTitleDirty(dirty: Boolean) {
        this.isTitleDirtyValue = dirty
        this.isDirtySubject.onNext(isContentDirtyValue || isTitleDirtyValue)
    }

    fun getNote(): Observable<Note> = noteSubject

    fun setNote(noteId: String) {
        this.noteId.onNext(noteId)
    }

    @Synchronized
    private fun setNoteInternal(note: Note) {
        if (this.noteValue == null || !(isContentDirtyValue || isTitleDirtyValue)) {
            setContentDirty(false)
            setContentFromNoteIfDifferent(note)
            setTitleDirty(false)
            setTitleFromNoteIfDifferent(note)
        } else {
            if (contentValue == note.content) {
                setContentDirty(false)
            }
            if (titleValue == note.title) {
                setTitleDirty(false)
            }
            if (!(isContentDirtyValue || isTitleDirtyValue)) {
                setTitleFromNoteIfDifferent(note)
                setContentFromNoteIfDifferent(note)
            }
        }
        this.noteValue = note
        this.noteSubject.onNext(note)
    }

    @Synchronized
    private fun setContentFromNoteIfDifferent(note: Note) {
        if (this.contentValue != note.content) {
            this.contentValue = note.content
            this.contentUpdatesSubject.onNext(Update(note.content, origin = Update.Origin.VIEW_MODEL))
            logger.trace("Content set from note: {}", this.contentValue)
        }
    }

    @Synchronized
    fun setContentFromUser(content: String) = setContentFromUser(content, origin = Update.Origin.VIEW)

    @Synchronized
    private fun setContentFromUser(content: String, origin: Update.Origin) {
        if (content != this.contentValue) {
            val isSameAsNoteContent = content == noteValue?.content
            this.contentValue = content
            this.contentUpdatesSubject.onNext(Update(content, origin))
            setContentDirty(!isSameAsNoteContent)
            logger.trace("Content set by user: {}", this.contentValue)
        }
    }

    fun getContentUpdates(): Observable<Update> = contentUpdatesSubject

    fun getTitleUpdates(): Observable<Update> = titleUpdatesSubject

    @Synchronized
    private fun setTitleFromNoteIfDifferent(note: Note) {
        if (this.titleValue != note.title) {
            this.titleValue = note.title
            this.titleUpdatesSubject.onNext(Update(note.title, origin = Update.Origin.VIEW_MODEL))
            logger.trace("Title set from note: {}", this.titleValue)
        }
    }

    @Synchronized
    fun setTitleFromUser(title: String) {
        if (title != this.titleValue) {
            val isSameAsNoteTitle = title == noteValue?.title
            this.titleValue = title
            this.titleUpdatesSubject.onNext(Update(title, origin = Update.Origin.VIEW))
            setTitleDirty(!isSameAsNoteTitle)
            logger.trace("Title set by user: {}", this.titleValue)
        } else {
        }
    }

    /**
     * @return The state of the view model that needs to be saved.
     */
    // Source: https://github.com/googlesamples/android-architecture/blob/dev-todo-mvvm-rxjava/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksFragment.java
    @Synchronized
    fun getStateToSave(): Bundle {
        val bundle = Bundle()
        bundle.putSerializable(CONTENT_KEY, contentValue)
        return bundle
    }

    /**
     * Restore the state of the view model based on a bundle.
     *
     * @param bundle The bundle containing the state.
     */
    // Source: https://github.com/googlesamples/android-architecture/blob/dev-todo-mvvm-rxjava/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksFragment.java
    @Synchronized
    fun restoreState(bundle: Bundle?) {
        if (bundle != null && bundle.containsKey(CONTENT_KEY)) {
            disposables.add(this.noteSubject
                .firstOrError()
                .map { bundle.getString(CONTENT_KEY)!! }
                .subscribe(
                    { setContentFromUser(it, origin = Update.Origin.VIEW_MODEL) },
                    { logger.warn("Error", it) }
                )
            )
        }
    }

    companion object {
        private const val CONTENT_KEY = "content"
    }

    data class Update(val value: String, val origin: Origin) {
        enum class Origin { VIEW, VIEW_MODEL }
    }
}
