package info.maaskant.wmsnotes.android.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import info.maaskant.wmsnotes.model.projection.Note
import info.maaskant.wmsnotes.model.projection.NoteProjector
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject


class DetailViewModel @VisibleForTesting constructor(
    private val noteProjector: NoteProjector,
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
    private val textSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val textUpdatesForEditorSubject: Subject<String> = BehaviorSubject.create()
    val textUpdatesForEditorLiveData by lazy {
        getTextUpdatesForEditor()
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
                noteProjector.projectAndUpdate(it)
                    .subscribeOn(ioScheduler)
            }
            .observeOn(computationScheduler)
            .subscribe(::setNoteInternal) { logger.warn("Error", it) }
        )
    }

    @Inject
    constructor(noteProjector: NoteProjector) : this(
        noteProjector,
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
            setText(note, true)
            setTitle(note.title)
        } else if (isDirtyValue && textValue == note.content) {
            setDirty(false)
            setTitle(note.title)
        }
        this.noteValue = note
        this.noteSubject.onNext(note)
    }

    fun getText(): Observable<String> = textSubject

    @Synchronized
    fun setText(text: String) {
        if (text != this.textValue) {
            val isSameAsNoteContent = text == noteValue?.content
            this.textValue = text
            this.textSubject.onNext(text)
            setDirty(!isSameAsNoteContent)
        }
    }

    @Synchronized
    private fun setText(note: Note, updateEditor: Boolean) {
        this.textValue = note.content
        this.textSubject.onNext(note.content)
        if (updateEditor) {
            this.textUpdatesForEditorSubject.onNext(this.textValue)
        }
    }

    fun getTextUpdatesForEditor(): Observable<String> = textUpdatesForEditorSubject

    fun getTitle(): Observable<String> = titleSubject.distinctUntilChanged()

    @Synchronized
    private fun setTitle(title: String) {
        this.titleSubject.onNext(title)
    }
}
