package info.maaskant.wmsnotes.detail;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import info.maaskant.wmsnotes.R;
import io.reark.reark.utils.RxViewBinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * A {@link View} that shows a node to be edited.
 */
class EditorView extends RelativeLayout {

    private EditText editText;

    private boolean listenForChanges = true;

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
                listenForChanges = false;
                editText.setText(markdownContent);
                listenForChanges = true;
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
            s.add(viewModel.getMarkdownChange().observeOn(AndroidSchedulers.mainThread())
                    .filter(i -> !i.isFromUser()).subscribe(i -> view.setContent(i.getContent())));

            s.add(Observable.create(subscriber -> {
                view.editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (view.listenForChanges) {
                            viewModel
                                    .setMarkdownContentFromUser(view.editText.getText().toString());
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                // We might be causing a memory leak here by not removing the listener here.
                // subscriber
                // .add(Subscriptions.create(() -> view.editText.removeTextChangedListener(TODO)));
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe());
        }

    }
}
