package info.maaskant.wouttest2;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import javax.inject.Inject;

import info.maaskant.wouttest2.data.DataFunctions;
import info.maaskant.wouttest2.navigation.NavigationFragment;
import info.maaskant.wouttest2.settings.SettingsActivity;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String NAVIGATION_FRAGMENT_KEY = "navigationFragment";

    @Inject
    DataFunctions.SetUserSettings setUserSettings;

    NavigationFragment navigationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate (hash: %s, savedInstanceState: %s)", System.identityHashCode(this),
                savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createDrawer(toolbar);
        this.navigationFragment = getOrCreateNavigationFragment(savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(
                view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show());
    }

    /**
     * Creates a navigation drawer and adds it to the activity.
     *
     * @param toolbar
     *            The activity's {@link Toolbar}.
     */
    private void createDrawer(Toolbar toolbar) {
        final PrimaryDrawerItem notesDrawerItem = new PrimaryDrawerItem().withIdentifier(0)
                .withName(R.string.drawer_item_notes);
        final PrimaryDrawerItem settingsDrawerItem = new PrimaryDrawerItem().withIdentifier(1)
                .withName(R.string.drawer_item_settings).withSelectable(false);
        new DrawerBuilder().withActivity(this).withToolbar(toolbar)
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
     *            The {@link Bundle} to
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
        if (!navigationFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, NAVIGATION_FRAGMENT_KEY,
                this.navigationFragment);
    }
}
