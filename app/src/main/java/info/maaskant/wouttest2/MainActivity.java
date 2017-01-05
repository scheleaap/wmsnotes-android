package info.maaskant.wouttest2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import javax.inject.Inject;

import info.maaskant.wouttest2.data.DataFunctions;
import info.maaskant.wouttest2.navigation.NodeListFragment;

public class MainActivity extends AppCompatActivity {

    @Inject
    DataFunctions.SetUserSettings setUserSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            createDrawer(toolbar);
            createNavigationFragment();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * Creates a navigation drawer and adds it to the activity.
     *
     * @param toolbar The activity's {@link Toolbar}.
     */
    private void createDrawer(Toolbar toolbar) {
        final PrimaryDrawerItem notesDrawerItem = new PrimaryDrawerItem().withIdentifier(0)
                .withName(R.string.drawer_item_notes);
        final PrimaryDrawerItem settingsDrawerItem = new PrimaryDrawerItem().withIdentifier(1)
                .withName(R.string.drawer_item_settings).withSelectable(false);
        new DrawerBuilder().withActivity(this).withToolbar(toolbar)
                .addDrawerItems(notesDrawerItem, settingsDrawerItem)
//                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
//                    @Override
//                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
//                        if (drawerItem == settingsDrawerItem) {
//                            startSettingsActivity();
//                        }
//                        return true;
//                    }
//                })
            .build();
    }

    /**
     * Creates a {@link NodeListFragment} and adds it to the activity.
     */
    private void createNavigationFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_main, new NodeListFragment())
                .commit();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
