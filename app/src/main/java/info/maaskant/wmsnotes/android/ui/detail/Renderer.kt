package info.maaskant.wmsnotes.android.ui.detail

import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.commonmark.parser.Parser
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.renderer.SpannableRenderer

internal class Renderer(
    private val detailViewModel: DetailViewModel,
    context: Context,
    private val textView: TextView
) : LifecycleObserver {
    private val configuration: SpannableConfiguration =
        SpannableConfiguration.create(context)
    private val parser: Parser = Markwon.createParser()
    private val renderer: SpannableRenderer = SpannableRenderer()
    private val disposables = CompositeDisposable()

    private val isPaused: BehaviorSubject<Boolean> =
        BehaviorSubject.create()

    init {
        textView.movementMethod = LinkMovementMethod.getInstance();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        disposables.add(
            Observables.combineLatest(
                isPaused,
                detailViewModel.getTextUpdates()
            )
                .observeOn(Schedulers.computation())
                .filter { (isPaused, _) -> !isPaused }
                .map { (_, textUpdate) -> textUpdate.text }
                .map { parser.parse(it) }
                .map { renderer.render(configuration, it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Markwon.unscheduleDrawables(textView);
                    Markwon.unscheduleTableRows(textView);
                    textView.text = it;
                    Markwon.scheduleDrawables(textView);
                    Markwon.scheduleTableRows(textView);
                }
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposables.clear()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        isPaused.onNext(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        isPaused.onNext(false)
    }
}
