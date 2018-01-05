package info.maaskant.wmsnotes.detail;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import javax.inject.Inject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import info.maaskant.wmsnotes.R;
import info.maaskant.wmsnotes.WmsNotesApplication;
import info.maaskant.wmsnotes.model.ContentNode;
import info.maaskant.wmsnotes.utils.ApplicationInstrumentation;
import io.reark.reark.utils.RxViewBinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    public static final String NODE_ID_KEY = "nodeId";

    private static final String EDITOR_FRAGMENT_KEY = "editorFragment";

    private static final String VIEWER_FRAGMENT_KEY = "viewerFragment";

    @Inject
    DetailViewModel detailViewModel;

    @Inject
    ApplicationInstrumentation instrumentation;

    private String nodeId;

    private ViewerFragment viewerFragment;

    private DetailActivity.ViewBinder activityViewBinder;

    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate (hash: %s, savedInstanceState: %s)", System.identityHashCode(this),
                savedInstanceState);

        ((WmsNotesApplication) getApplication()).getGraph().inject(this);

        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(NODE_ID_KEY)) {
            nodeId = getIntent().getStringExtra(NODE_ID_KEY);
        } else {
            Timber.e("No node identifier specified");
            finish();
            return;
        }
        Timber.v("Using node identifier " + nodeId);

        saveButton = (Button) findViewById(R.id.detail_save_button);

        // TODO: Support restoring from bundle again (see commented-out code)
        // this.viewerFragment = getOrCreateViewerFragment(savedInstanceState);
        ViewPager viewPager = (ViewPager) findViewById(R.id.detail_view_pager);
        viewPager.setAdapter(new DetailPagerAdapter(getSupportFragmentManager(),
                Arrays.asList(new ViewerFragment(), new EditorFragment())));

        activityViewBinder = new DetailActivity.ViewBinder(this, detailViewModel);
        // TODO: Is this the right place to call this? It used to be called from ViewerFragment
        detailViewModel.subscribeToDataStore();

        // Start loading everything by setting the content node id.
        detailViewModel.setContentNodeId(nodeId);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.v("onResume");
        activityViewBinder.bind();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.v("onPause");
        activityViewBinder.unbind();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.v("onDestroyView");
        detailViewModel.unsubscribeFromDataStore();
        detailViewModel.dispose();
        instrumentation.getLeakTracing().traceLeakage(this);
    }

    // /**
    // * Retrieves a {@link ViewerFragment} from a saved instance state or creates a new instance.
    // *
    // * @param savedInstanceState
    // * The {@link Bundle} to load from.
    // * @return A new instance if {@code savedInstanceState} is {@code null}, a restored instance
    // * from {@link #getSupportFragmentManager()} otherwise.
    // */
    // private ViewerFragment getOrCreateViewerFragment(Bundle savedInstanceState) {
    // if (savedInstanceState == null) {
    // ViewerFragment viewerFragment = new ViewerFragment();
    // getSupportFragmentManager().beginTransaction().add(R.id.viewer_fragment, viewerFragment)
    // .commit();
    // return viewerFragment;
    // } else {
    // return (ViewerFragment) getSupportFragmentManager().getFragment(savedInstanceState,
    // VIEWER_FRAGMENT_KEY);
    // }
    // }

    // @Override
    // public void onBackPressed() {
    // if (!navigationFragment.onBackPressed()) {
    // super.onBackPressed();
    // }
    // }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, VIEWER_FRAGMENT_KEY, this.viewerFragment);
    }

    void setSupportActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /**
     * Binds a {@link DetailViewModel} to a {@link DetailActivity}.
     */
    static class ViewBinder extends RxViewBinder {

        private final DetailActivity view;
        private final DetailViewModel viewModel;

        public ViewBinder(@NonNull final DetailActivity view,
                @NonNull final DetailViewModel viewModel) {
            this.view = requireNonNull(view);
            this.viewModel = requireNonNull(viewModel);
        }

        @Override
        protected void bindInternal(@NonNull final CompositeSubscription s) {
            s.add(viewModel.getContentNode().observeOn(AndroidSchedulers.mainThread())
                    .map(ContentNode::getName).subscribe(view::setSupportActionBarTitle));

            s.add(Observable.create(subscriber -> {
                view.saveButton.setOnClickListener(this::saveButtonOnClick);
                subscriber
                        .add(Subscriptions.create(() -> view.saveButton.setOnClickListener(null)));
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe());

        }

        private void saveButtonOnClick(View clickedView) {
            viewModel.save();
        }

    }
}
