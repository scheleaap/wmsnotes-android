package info.maaskant.wmsnotes.android.ui.detail

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.Target
import info.maaskant.wmsnotes.android.ui.util.glide.NoteAttachmentModel
import info.maaskant.wmsnotes.utilities.logger
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

internal class Renderer(
    private val detailViewModel: DetailViewModel,
    context: Context,
    private val textView: TextView
) : LifecycleObserver {
    private val logger by logger()
    private val markwon: Markwon = Markwon.builder(context)
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TablePlugin.create(context))
        .usePlugin(HtmlPlugin.create())
        .usePlugin(GlideImagesPlugin.create(object : GlideImagesPlugin.GlideStore {
            override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
                val model: Any = if (drawable.destination.startsWith(attachmentPrefix)) {
                    val note = detailViewModel.getNote().blockingFirst()
                    NoteAttachmentModel(
                        aggId = note.aggId,
                        revision = note.revision,
                        attachmentName = drawable.destination.substring(attachmentPrefix.length)
                    )
                } else {
                    drawable.destination
                }
                return Glide.with(context)
                    .load(model)
                    .transform(RoundedCorners(24))
            }

            override fun cancel(target: Target<*>) {
                Glide.with(context).clear(target)
            }
        }
        ))
        .build()
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
                detailViewModel.getContentUpdates()
            )
                .observeOn(Schedulers.computation())
                .filter { (isPaused, _) -> !isPaused }
                .map { (_, textUpdate) -> textUpdate.value }
                .map { markwon.parse(it) }
                .map { markwon.render(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    markwon.setParsedMarkdown(textView, it)
                }, onError = { logger.warn("Error", it) })
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

    companion object {
        private const val attachmentPrefix = "attachment:"
    }
}
