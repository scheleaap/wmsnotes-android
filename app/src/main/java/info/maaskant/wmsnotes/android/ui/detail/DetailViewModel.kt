package info.maaskant.wmsnotes.android.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.model.note.Note
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
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
    val isDirtyLiveData by lazy {
        isDirty()
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()
    }
    private var isDirtyValue: Boolean = false
    private val noteId: Subject<String> = BehaviorSubject.create()
    private val noteSubject: Subject<Note> = BehaviorSubject.create()
    val noteLiveData by lazy {
        getNote()
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()
    }
    private var noteValue: Note? = null
    private var textValue: String = ""
    private val textUpdatesSubject: BehaviorSubject<TextUpdate> = BehaviorSubject.create()
    val textUpdatesLiveData by lazy {
        getTextUpdates()
            .toFlowable(BackpressureStrategy.ERROR)
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()
    }
    private val titleSubject: BehaviorSubject<String> = BehaviorSubject.create()
    val titleLiveData by lazy {
        getTitle()
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
    private fun setDirty(dirty: Boolean) {
        this.isDirtyValue = dirty
        this.isDirtySubject.onNext(dirty)
    }

    fun getNote(): Observable<Note> = noteSubject

    fun setNote(noteId: String) {
        this.noteId.onNext(noteId)
    }

    @Synchronized
    private fun setNoteInternal(note: Note) {
        if (this.noteValue == null || !isDirtyValue) {
            setDirty(false)
            setTextFromNote(note)
            setTitle(note.title)
        } else if (isDirtyValue && textValue == note.content) {
            setDirty(false)
            setTitle(note.title)
        }
        this.noteValue = note
        this.noteSubject.onNext(note)
    }

    @Synchronized
    fun setTextFromUser(text: String) {
        if (text != this.textValue) {
            val isSameAsNoteContent = text == noteValue?.content
            this.textValue = text
            this.textUpdatesSubject.onNext(TextUpdate(text, source = TextUpdate.Source.USER))
            setDirty(!isSameAsNoteContent)
            Timber.v("Text set by user: %s", this.textValue)
        } else {
        }
    }

    @Synchronized
    private fun setTextFromNote(note: Note) {
        this.textValue = note.content
        this.textUpdatesSubject.onNext(TextUpdate(note.content, source = TextUpdate.Source.SYSTEM))
        Timber.v("Text set from note: %s", this.textValue)
    }

    fun getTextUpdates(): Observable<TextUpdate> = textUpdatesSubject

    fun getTitle(): Observable<String> = titleSubject.distinctUntilChanged()

    @Synchronized
    private fun setTitle(title: String) {
        this.titleSubject.onNext(title)
    }
}

data class TextUpdate(val text: String, val source: Source) {
    enum class Source { USER, SYSTEM }
}
