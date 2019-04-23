package info.maaskant.wmsnotes.android.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.ui.RxAlertDialog
import info.maaskant.wmsnotes.model.note.*
import info.maaskant.wmsnotes.model.CommandProcessor
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class DetailController @VisibleForTesting constructor(
    private val detailViewModel: DetailViewModel,
    private val commandProcessor: CommandProcessor,
    private val detailActivity: DetailActivity,
    private val computationScheduler: Scheduler,
    private val uiScheduler: Scheduler
) : LifecycleObserver {
    private val quitRequest: Subject<QuitRequestType> = PublishSubject.create()
    private val quitFunction: () -> Unit = detailActivity::finish

    private val disposables = CompositeDisposable()

    @Inject
    constructor(
        detailViewModel: DetailViewModel,
        commandProcessor: CommandProcessor,
        detailActivity: DetailActivity
    ) : this(
        detailViewModel,
        commandProcessor,
        detailActivity,
        computationScheduler = Schedulers.computation(),
        uiScheduler = AndroidSchedulers.mainThread()
    )

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        disposables.add(Observables.combineLatest(
            quitRequest.filter { it == QuitRequestType.SAVE_AND_QUIT },
            detailViewModel.isDirty(),
            Observables.combineLatest(detailViewModel.getNote(), detailViewModel.getContentUpdates())
        )
            .observeOn(computationScheduler)
            .filter { (_, isDirty, _) -> isDirty }
            .map { (_, _, it) -> it }
            .map { (note, textUpdate) ->
                ChangeContentCommand(
                    aggId = note.aggId,
                    lastRevision = note.revision,
                    content = textUpdate.value
                )
            }
            .subscribe { commandProcessor.commands.onNext(it) }
        )
        disposables.add(Observables.combineLatest(
            quitRequest,
            detailViewModel.isDirty()
        )
            .observeOn(computationScheduler)
            .filter { (quitRequest, isDirty) ->
                when (quitRequest) {
                    QuitRequestType.SAVE_AND_QUIT -> !isDirty
                    QuitRequestType.DISCARD_AND_QUIT -> true
                    QuitRequestType.ASK_AND_QUIT -> !isDirty
                    else -> false
                }
            }
            .subscribe { quitFunction() }
        )

        disposables.add(
            Observables.combineLatest(
                quitRequest.filter { it == QuitRequestType.ASK_AND_QUIT },
                detailViewModel.isDirty()
            )
                .observeOn(computationScheduler)
                .filter { (_, isDirty) -> isDirty }
                .observeOn(uiScheduler)
                .flatMap {
                    RxAlertDialog.show(
                        context = detailActivity,
                        title = R.string.detail_dismiss_dialog_title,
                        message = R.string.detail_dismiss_dialog_message,
                        positiveButton = R.string.detail_dismiss_dialog_save,
                        negativeButton = R.string.detail_dismiss_dialog_discard
                    )
                }
                .subscribe { button ->
                    when (button) {
                        RxAlertDialog.Event.BUTTON_POSITIVE -> quitRequest.onNext(QuitRequestType.SAVE_AND_QUIT)
                        RxAlertDialog.Event.BUTTON_NEGATIVE -> quitRequest.onNext(QuitRequestType.DISCARD_AND_QUIT)
                        RxAlertDialog.Event.DISMISS_ALERT -> println("dismiss")
                        RxAlertDialog.Event.CANCEL_ALERT -> quitRequest.onNext(QuitRequestType.NO_REQUEST)
                        else -> throw IllegalArgumentException()
                    }
                }
        )

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposables.clear()
    }

    fun deleteAndQuit() {
        disposables.add(
            detailViewModel.getNote()
                .observeOn(computationScheduler)
                .firstElement()
                .map { note ->
                    DeleteNoteCommand(
                        aggId = note.aggId,
                        lastRevision = note.revision
                    )
                }
                .subscribe {
                    commandProcessor.commands.onNext(it)
                    quitFunction()
                }
        )
    }

    fun saveAndQuit() = quitRequest.onNext(QuitRequestType.SAVE_AND_QUIT)

    fun quit() = quitRequest.onNext(QuitRequestType.ASK_AND_QUIT)

    internal enum class QuitRequestType {
        NO_REQUEST,
        SAVE_AND_QUIT,
        DISCARD_AND_QUIT,
        ASK_AND_QUIT
    }
}
