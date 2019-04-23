package info.maaskant.wmsnotes.android.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
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
    val contentUpdatesLiveData by lazy {
        getContentUpdates()
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()
    }

    private var titleValue: String = ""
    val titleLiveData by lazy {
        getTitleUpdates()
            .map {it.value}
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()
    }

    private val titleUpdatesSubject: BehaviorSubject<Update> = BehaviorSubject.create()
    val titleUpdatesLiveData by lazy {
        getTitleUpdates()
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()
    }

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
            this.contentUpdatesSubject.onNext(Update(note.content, source = Update.Source.SYSTEM))
            Timber.v("Content set from note: %s", this.contentValue)
        }
    }

    @Synchronized
    fun setContentFromUser(content: String) {
        if (content != this.contentValue) {
            val isSameAsNoteContent = content == noteValue?.content
            this.contentValue = content
            this.contentUpdatesSubject.onNext(Update(content, source = Update.Source.USER))
            setContentDirty(!isSameAsNoteContent)
            Timber.v("Content set by user: %s", this.contentValue)
        } else {
        }
    }

    fun getContentUpdates(): Observable<Update> = contentUpdatesSubject

    fun getTitleUpdates(): Observable<Update> = titleUpdatesSubject

    @Synchronized
    private fun setTitleFromNoteIfDifferent(note: Note) {
        if (this.titleValue != note.title) {
            this.titleValue = note.title
            this.titleUpdatesSubject.onNext(Update(note.title, source = Update.Source.SYSTEM))
            Timber.v("Title set from note: %s", this.titleValue)
        }
    }

    @Synchronized
    fun setTitleFromUser(title: String) {
        if (title != this.titleValue) {
            val isSameAsNoteTitle = title == noteValue?.title
            this.titleValue = title
            this.titleUpdatesSubject.onNext(Update(title, source = Update.Source.USER))
            setTitleDirty(!isSameAsNoteTitle)
            Timber.v("Title set by user: %s", this.titleValue)
        } else {
        }
    }

    data class Update(val value: String, val source: Source) {
        enum class Source { USER, SYSTEM }
    }
}
