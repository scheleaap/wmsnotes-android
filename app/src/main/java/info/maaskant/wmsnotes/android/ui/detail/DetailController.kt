package info.maaskant.wmsnotes.android.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.ui.util.RxAlertDialog
import info.maaskant.wmsnotes.model.CommandBus
import info.maaskant.wmsnotes.model.note.ChangeContentCommand
import info.maaskant.wmsnotes.model.note.DeleteNoteCommand
import info.maaskant.wmsnotes.model.note.NoteCommandRequest
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class DetailController @VisibleForTesting constructor(
    private val detailViewModel: DetailViewModel,
    private val commandBus: CommandBus,
    private val detailActivity: DetailActivity,
    private val computationScheduler: Scheduler,
    private val uiScheduler: Scheduler
) : LifecycleObserver {
    private val logger by logger()
    private val quitRequest: Subject<QuitRequestType> = PublishSubject.create()
    private val quitFunction: () -> Unit = detailActivity::finish

    private val disposables = CompositeDisposable()

    @Inject
    constructor(
        detailViewModel: DetailViewModel,
        commandBus: CommandBus,
        detailActivity: DetailActivity
    ) : this(
        detailViewModel,
        commandBus,
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
                NoteCommandRequest.of(
                    command = ChangeContentCommand(
                        aggId = note.aggId,
                        content = textUpdate.value
                    ),
                    lastRevision = note.revision
                )
            }
            .subscribeBy(onNext = { commandBus.requests.onNext(it) }, onError = { logger.warn("Error", it) })
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
            .subscribeBy(onNext = { quitFunction() }, onError = { logger.warn("Error", it) })
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
                .subscribeBy(onNext = { button ->
                    when (button) {
                        RxAlertDialog.Event.BUTTON_POSITIVE -> quitRequest.onNext(QuitRequestType.SAVE_AND_QUIT)
                        RxAlertDialog.Event.BUTTON_NEGATIVE -> quitRequest.onNext(QuitRequestType.DISCARD_AND_QUIT)
                        RxAlertDialog.Event.DISMISS_ALERT -> Unit
                        RxAlertDialog.Event.CANCEL_ALERT -> quitRequest.onNext(QuitRequestType.NO_REQUEST)
                        else -> throw IllegalArgumentException()
                    }
                }, onError = { logger.warn("Error", it) })
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
                    NoteCommandRequest.of(
                        command = DeleteNoteCommand(
                            aggId = note.aggId
                        ),
                        lastRevision = note.revision
                    )
                }
                .subscribeBy(onSuccess = {
                    commandBus.requests.onNext(it)
                    quitFunction()
                }, onError = { logger.warn("Error", it) })
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
