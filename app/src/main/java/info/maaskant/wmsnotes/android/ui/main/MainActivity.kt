package info.maaskant.wmsnotes.android.ui.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.baurine.permissionutil.PermissionUtil
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import info.maaskant.wmsnotes.BuildConfig
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.EmergencyBrakeActivityFinisher
import info.maaskant.wmsnotes.android.client.synchronization.EmergencyBrake
import info.maaskant.wmsnotes.android.service.ApplicationServiceManager
import info.maaskant.wmsnotes.android.ui.debug.DebugFragment
import info.maaskant.wmsnotes.android.ui.navigation.NavigationFragment
import info.maaskant.wmsnotes.android.ui.settings.SettingsActivity
import info.maaskant.wmsnotes.android.ui.util.OnBackPressedListener
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var emergencyBrake: EmergencyBrake

    private lateinit var drawer: Drawer

    private val permissionRequestCode: Int = 0

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
    private fun createAndAddDrawer(toolbar: Toolbar, currentSelection: Long?) {
        val header = AccountHeaderBuilder()
            .withActivity(this)
            .withHeaderBackground(R.drawable.drawer_header)
            .build()

        val debugDrawerItem = PrimaryDrawerItem().withIdentifier(debugDrawerItemId)
            .withName(R.string.drawer_item_debug)
            .withIcon(GoogleMaterial.Icon.gmd_developer_mode)
        val notesDrawerItem = PrimaryDrawerItem().withIdentifier(notesDrawerItemId)
            .withName(R.string.drawer_item_notes)
            .withIcon(GoogleMaterial.Icon.gmd_insert_drive_file)
        val settingsDrawerItem = PrimaryDrawerItem().withIdentifier(2)
            .withName(R.string.drawer_item_settings)
            .withIcon(GoogleMaterial.Icon.gmd_settings)
            .withSelectable(false)

        drawer = DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(header)
            .also {
                if (BuildConfig.DEBUG) {
                    it.addDrawerItems(debugDrawerItem)
                }
            }
            .addDrawerItems(notesDrawerItem, DividerDrawerItem(), settingsDrawerItem)
            .withOnDrawerItemClickListener { _, _, drawerItem ->
                when (drawerItem) {
                    debugDrawerItem -> navigateToDebug()
                    notesDrawerItem -> navigateToNavigation()
                    settingsDrawerItem -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
                false
            }
            .also {
                if (currentSelection != null) {
                    it.withSelectedItem(currentSelection)
                }
            }
            .build()
    }

    private fun hasAllRequiredPermissions() =
        PermissionUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun navigateToDebug() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, DebugFragment())
            .commitNow()
        drawer.setSelection(debugDrawerItemId, false)
    }

    private fun navigateToNavigation() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, NavigationFragment())
            .commitNow()
        drawer.setSelection(notesDrawerItemId, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

        findViewById<Toolbar>(R.id.toolbar).let { toolbar ->
            setSupportActionBar(toolbar)
            createAndAddDrawer(toolbar, savedInstanceState?.getLong(NAVIGATION_DRAWER_CURRENT_SELECTION))
        }

        lifecycle.addObserver(ApplicationServiceManager.ServiceBindingLifecycleObserver(this))
        lifecycle.addObserver(EmergencyBrakeActivityFinisher(emergencyBrake, this))

        if (savedInstanceState == null) {
            navigateToNavigation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionUtil.onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(NAVIGATION_DRAWER_CURRENT_SELECTION, drawer.currentSelection)
    }

    override fun onStart() {
        super.onStart()
        checkAndAskForPermissions()
    }

    override fun supportFragmentInjector() = fragmentInjector

    companion object {
        val NAVIGATION_DRAWER_CURRENT_SELECTION = "navigationDrawerCurrentSelection"
        val debugDrawerItemId: Long = 0
        val notesDrawerItemId: Long = 1
    }
}
