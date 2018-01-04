package info.maaskant.wouttest2.detail;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import info.maaskant.wouttest2.R;
import io.reark.reark.utils.RxViewBinder;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * A {@link View} that shows a node to be edited.
 */
class EditorView extends RelativeLayout {

    private EditText editText;

    public EditorView(Context context) {
        super(context, null);
    }

    public EditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        editText = (EditText) findViewById(R.id.editor_text);
    }

    private void setContent(@NonNull final String markdownContent) {
        Timber.v("Setting content");
        requireNonNull(markdownContent);
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.setText(markdownContent);
            }
        });

    }

    /**
     * Binds a {@link DetailViewModel} to a {@link EditorView}.
     */
    static class ViewBinder extends RxViewBinder {

        private final EditorView view;
        private final DetailViewModel viewModel;

        public ViewBinder(@NonNull final EditorView view,
                @NonNull final DetailViewModel viewModel) {
            this.view = requireNonNull(view);
            this.viewModel = requireNonNull(viewModel);
        }

        @Override
        protected void bindInternal(@NonNull final CompositeSubscription s) {
            s.add(viewModel.getMarkdown().observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view::setContent));
        }

    }
}
