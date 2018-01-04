package info.maaskant.wouttest2.detail;

import static java.util.Objects.requireNonNull;

import javax.inject.Inject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import info.maaskant.wouttest2.R;
import info.maaskant.wouttest2.WoutTest2App;
import info.maaskant.wouttest2.model.ContentNode;
import info.maaskant.wouttest2.utils.ApplicationInstrumentation;
import io.reark.reark.utils.RxViewBinder;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate (hash: %s, savedInstanceState: %s)", System.identityHashCode(this),
                savedInstanceState);

        ((WoutTest2App) getApplication()).getGraph().inject(this);

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

        this.viewerFragment = getOrCreateViewerFragment(savedInstanceState);

        activityViewBinder = new DetailActivity.ViewBinder(this, detailViewModel);
        // TODO: Is this the right place to call this? It used to be called from ViewerFragment
        detailViewModel.subscribeToDataStore();

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

    /**
     * Retrieves a {@link ViewerFragment} from a saved instance state or creates a new instance.
     *
     * @param savedInstanceState
     *            The {@link Bundle} to load from.
     * @return A new instance if {@code savedInstanceState} is {@code null}, a restored instance
     *         from {@link #getSupportFragmentManager()} otherwise.
     */
    private ViewerFragment getOrCreateViewerFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            ViewerFragment viewerFragment = new ViewerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.viewer_fragment, viewerFragment)
                    .commit();
            return viewerFragment;
        } else {
            return (ViewerFragment) getSupportFragmentManager().getFragment(savedInstanceState,
                    VIEWER_FRAGMENT_KEY);
        }
    }

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // getMenuInflater().inflate(R.menu.menu_main, menu);
    // return true;
    // }

    // @Override
    // public boolean onOptionsItemSelected(MenuItem item) {
    // // Handle action bar item clicks here. The action bar will
    // // automatically handle clicks on the Home/Up button, so long
    // // as you specify a parent activity in AndroidManifest.xml.
    // int id = item.getItemId();
    //
    // // noinspection SimplifiableIfStatement
    // if (id == R.id.action_settings) {
    // return true;
    // }
    //
    // return super.onOptionsItemSelected(item);
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
        }

    }
}
