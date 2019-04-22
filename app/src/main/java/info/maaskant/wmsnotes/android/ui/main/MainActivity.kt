package info.maaskant.wmsnotes.android.ui.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.baurine.permissionutil.PermissionUtil
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
import info.maaskant.wmsnotes.android.client.synchronization.SynchronizationTaskLifecycleObserver
import info.maaskant.wmsnotes.android.client.synchronization.SynchronizationWorker
import info.maaskant.wmsnotes.android.ui.OnBackPressedListener
import info.maaskant.wmsnotes.android.ui.navigation.NavigationFragment
import info.maaskant.wmsnotes.android.ui.settings.SettingsActivity
import info.maaskant.wmsnotes.client.synchronization.SynchronizationTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector, MainFragment.Listener {
    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var instrumentation: ApplicationInstrumentation

    @Inject
    lateinit var synchronizationTask: SynchronizationTask

    private val permissionRequestCode: Int = 0

    private lateinit var drawer: Drawer

    private fun checkAndAskForPermissions() {
        if (!hasAllRequiredPermissions()) {
            PermissionUtil.checkPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                permissionRequestCode,
                getString(R.string.storage_permission_request),
                getString(R.string.storage_permission_denied)
            ) { success ->
                if (!success) {
                    Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_SHORT).show()
                    this@MainActivity.moveTaskToBack(true)
                } else {
                    recreate()
                }
            }
        }
    }

    /**
     * Creates a navigation drawer and adds it to the activity.
     *
     * @param toolbar
     * The activity's [Toolbar].
     */
    private fun createAndAddDrawer(toolbar: Toolbar) {
        val debugDrawerItem = PrimaryDrawerItem().withIdentifier(0)
            .withName(R.string.drawer_item_debug)
        val notesDrawerItem = PrimaryDrawerItem().withIdentifier(1)
            .withName(R.string.drawer_item_notes)
        val settingsDrawerItem = PrimaryDrawerItem().withIdentifier(2)
            .withName(R.string.drawer_item_settings).withSelectable(false)
        drawer = DrawerBuilder().withActivity(this).withToolbar(toolbar)
            .addDrawerItems(/*debugDrawerItem,*/ notesDrawerItem, settingsDrawerItem)
            .withOnDrawerItemClickListener { view, position, drawerItem ->
                when (drawerItem) {
                    debugDrawerItem -> navigateToDebug()
                    notesDrawerItem -> navigateToNavigation()
                    settingsDrawerItem -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
                false
            }.build()
    }

    private fun hasAllRequiredPermissions() =
        PermissionUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun navigateToDebug() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, MainFragment())
            .commitNow()
    }

    private fun navigateToNavigation() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, NavigationFragment())
            .commitNow()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        PermissionUtil.onActivityResult(this, requestCode)
    }

    override fun onBackPressed() {
        if (this::drawer.isInitialized && drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_container)
            val handledByFragment = if (currentFragment != null && currentFragment is OnBackPressedListener) {
                currentFragment.onBackPressed()
            } else {
                false
            }
            if (!handledByFragment) {
                super.onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        if (!hasAllRequiredPermissions()) return

        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            navigateToNavigation()
        }

        findViewById<Toolbar>(R.id.toolbar).let { toolbar ->
            setSupportActionBar(toolbar)
            createAndAddDrawer(toolbar)
        }

        lifecycle.addObserver(SynchronizationTaskLifecycleObserver(synchronizationTask))
    }

    override fun onDestroy() {
        super.onDestroy()
// TODO: LEAK TESTING
//        instrumentation.leakTracing.traceLeakage(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionUtil.onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }

    override fun onStart() {
        super.onStart()
        checkAndAskForPermissions()
    }

    override fun onStartNavigatingButtonPressed() = navigateToNavigation()

    private fun scheduleSynchronizationUsingWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<SynchronizationWorker>(
            repeatInterval = 10,
            repeatIntervalTimeUnit = TimeUnit.SECONDS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance().enqueue(workRequest).get()
    }

    override fun supportFragmentInjector() = fragmentInjector
}
