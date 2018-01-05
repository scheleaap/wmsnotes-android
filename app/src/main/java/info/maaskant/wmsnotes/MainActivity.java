package info.maaskant.wmsnotes;

import static info.maaskant.wmsnotes.MemoryUtil.fixInputMethod;
import static java.util.Objects.requireNonNull;

import javax.inject.Inject;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import info.maaskant.wmsnotes.data.DataFunctions;
import info.maaskant.wmsnotes.detail.DetailActivity;
import info.maaskant.wmsnotes.detail.DetailViewModel;
import info.maaskant.wmsnotes.navigation.NavigationFragment;
import info.maaskant.wmsnotes.navigation.NavigationViewModel;
import info.maaskant.wmsnotes.settings.SettingsActivity;
import info.maaskant.wmsnotes.utils.ApplicationInstrumentation;
import io.reark.reark.utils.RxViewBinder;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String NAVIGATION_FRAGMENT_KEY = "navigationFragment";

    @Inject
    DataFunctions.SetUserSettings setUserSettings;

    @Inject
    NavigationViewModel navigationViewModel;

    @Inject
    ApplicationInstrumentation instrumentation;

    private Drawer drawer;

    private NavigationFragment navigationFragment;

    private MainActivity.ViewBinder activityViewBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate (hash: %s, savedInstanceState: %s)", System.identityHashCode(this),
                savedInstanceState);

        ((WmsNotesApplication) getApplication()).getGraph().inject(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();

        createAndAddDrawer(toolbar);
        this.navigationFragment = getOrCreateNavigationFragment(savedInstanceState);

        // FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        // fab.setOnClickListener(
        // view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        // .setAction("Action", null).show());

        activityViewBinder = new MainActivity.ViewBinder(this, navigationViewModel);
        // TODO: Is this the right place to call this? It used to be called from NavigationFragment
        navigationViewModel.subscribeToDataStore();
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
        navigationViewModel.unsubscribeFromDataStore();
        navigationViewModel.dispose();
        fixInputMethod(this);
        instrumentation.getLeakTracing().traceLeakage(this);
    }

    /**
     * Creates a navigation drawer and adds it to the activity.
     *
     * @param toolbar
     *            The activity's {@link Toolbar}.
     */
    private void createAndAddDrawer(Toolbar toolbar) {
        final PrimaryDrawerItem notesDrawerItem = new PrimaryDrawerItem().withIdentifier(0)
                .withName(R.string.drawer_item_notes);
        final PrimaryDrawerItem settingsDrawerItem = new PrimaryDrawerItem().withIdentifier(1)
                .withName(R.string.drawer_item_settings).withSelectable(false);
        drawer = new DrawerBuilder().withActivity(this).withToolbar(toolbar)
                .addDrawerItems(notesDrawerItem, settingsDrawerItem)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem == settingsDrawerItem) {
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                    return true;
                }).build();
    }

    /**
     * Retrieves a {@link NavigationFragment} from a saved instance state or creates a new instance.
     *
     * @param savedInstanceState
     *            The {@link Bundle} to load from.
     * @return A new instance if {@code savedInstanceState} is {@code null}, a restored instance
     *         from {@link #getSupportFragmentManager()} otherwise.
     */
    private NavigationFragment getOrCreateNavigationFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            NavigationFragment navigationFragment = new NavigationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_main, navigationFragment).commit();
            return navigationFragment;
        } else {
            return (NavigationFragment) getSupportFragmentManager().getFragment(savedInstanceState,
                    NAVIGATION_FRAGMENT_KEY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            if (!navigationFragment.onBackPressed()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, NAVIGATION_FRAGMENT_KEY,
                this.navigationFragment);
    }

    void setSupportActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /**
     * Binds a {@link DetailViewModel} to a {@link DetailActivity}.
     */
    static class ViewBinder extends RxViewBinder {

        private final MainActivity view;
        private final NavigationViewModel viewModel;

        public ViewBinder(@NonNull final MainActivity view,
                @NonNull final NavigationViewModel viewModel) {
            this.view = requireNonNull(view);
            this.viewModel = requireNonNull(viewModel);
        }

        @Override
        protected void bindInternal(@NonNull final CompositeSubscription s) {
            // TODO Implement
            // s.add(viewModel.getNavigationState().observeOn(AndroidSchedulers.mainThread())
            // .map(i -> i.getParentNode().getName())
            // .subscribe(view::setSupportActionBarTitle));
        }

    }

}
