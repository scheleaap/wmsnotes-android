package info.maaskant.wouttest2.detail;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import info.maaskant.wouttest2.R;
import io.reark.reark.utils.RxViewBinder;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * <p>
 * A {@link View} that shows a rendered node.
 * </p>
 * <p>
 * The node is displayed using a {@link android.webkit.WebView}.
 * </p>
 */
class ViewerView extends RelativeLayout {

    private ProgressBar progressBar;

    private WebView webView;

    public ViewerView(Context context) {
        super(context, null);
    }

    public ViewerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        progressBar = (ProgressBar) findViewById(R.id.viewer_progress);

        webView = (WebView) findViewById(R.id.viewer_webview);
        webView.setBackgroundColor(Color.TRANSPARENT);
        // webView.setWebChromeClient(new WebChromeClient() {
        // @Override
        // public void onProgressChanged(WebView view, int newProgress) {
        // if (newProgress == 100) {
        // // Hide the progressbar
        // progressBar.setVisibility(View.GONE);
        // }
        // }
        // });

        clear();
        // progressBar.setVisibility(GONE);
    }

    private void clear() {
        Timber.v("Clearing");
        // progressBar.setVisibility(VISIBLE);
        webView.setVisibility(GONE);
    }

    private void setContent(@NonNull final String htmlContent) {
        Timber.v("Setting content");
        requireNonNull(htmlContent);
        webView.setVisibility(VISIBLE);
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html",
                        "UTF-8", null);
            }
        });

    }

    /**
     * Binds a {@link DetailViewModel} to a {@link ViewerView}.
     */
    static class ViewBinder extends RxViewBinder {

        private final ViewerView view;
        private final DetailViewModel viewModel;

        public ViewBinder(@NonNull final ViewerView view,
                @NonNull final DetailViewModel viewModel) {
            this.view = requireNonNull(view);
            this.viewModel = requireNonNull(viewModel);
        }

        @Override
        protected void bindInternal(@NonNull final CompositeSubscription s) {
            s.add(viewModel.getHtmlContent().observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view::setContent));
        }

    }
}
