package info.maaskant.wmsnotes.android.ui.detail

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.commonmark.parser.Parser
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.renderer.SpannableRenderer
import javax.inject.Inject

class ViewerFragment : Fragment() {

    @Inject
    lateinit var detailViewModel: DetailViewModel

    //    @Inject
    //    ApplicationInstrumentation instrumentation;

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.viewer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView = view.findViewById<TextView>(R.id.viewer_textview)
        lifecycle.addObserver(Renderer(detailViewModel, requireContext(), textView))
    }

    override fun onDestroy() {
        super.onDestroy()
        //        instrumentation.getLeakTracing().traceLeakage(this);
    }
}

internal class Renderer(
    private val detailViewModel: DetailViewModel,
    context: Context,
    private val textView: TextView
) : LifecycleObserver {
    private val configuration: SpannableConfiguration = SpannableConfiguration.create(context)
    private val parser: Parser = Markwon.createParser()
    private val renderer: SpannableRenderer = SpannableRenderer()
    private val disposables = CompositeDisposable()

    private val isPaused: BehaviorSubject<Boolean> = BehaviorSubject.create()

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
