package info.maaskant.wmsnotes.android.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.baurine.permissionutil.PermissionUtil
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.ui.main.MainFragment
import info.maaskant.wmsnotes.android.ui.navigation.NavigationFragment


class MainActivity : AppCompatActivity(), MainFragment.Listener {

    private val permissionRequestCode: Int = 0

    private var drawer: Drawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndAskForPermissions()
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            navigateToDebug()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        createAndAddDrawer(toolbar)
    }

    private fun checkAndAskForPermissions() {
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
            .addDrawerItems(debugDrawerItem, notesDrawerItem, settingsDrawerItem)
            .withOnDrawerItemClickListener { view, position, drawerItem ->
                when (drawerItem) {
                    debugDrawerItem -> navigateToDebug()
                    notesDrawerItem -> navigateToNavigation()
                    settingsDrawerItem -> {
//                        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
//                        startActivity(intent)
                    }
                }
                false
            }.build()
    }

    override fun onStartNavigatingButtonPressed() = navigateToNavigation()

    private fun navigateToDebug() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance())
            .commitNow()
    }

    private fun navigateToNavigation() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, NavigationFragment.newInstance())
            .commitNow()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionUtil.onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        PermissionUtil.onActivityResult(this, requestCode)
    }

}
