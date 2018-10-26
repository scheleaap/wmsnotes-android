package info.maaskant.wmsnotes.android

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import info.maaskant.wmsnotes.android.ui.navigation.NavigationViewModel
import timber.log.Timber

class WmsNotesApplication : Application() {

//    @Inject
//    internal var instrumentation: ApplicationInstrumentation? = null
//
//    @Inject
//    internal var navigationViewModel: NavigationViewModel? = null
//
//    var graph: Graph? = null
//        private set

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

//        graph = Graph.Initializer.init(this)
//        graph!!.inject(this)
//
//        instrumentation!!.init()
//
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        val notebookPath = sharedPreferences.getString(
//            SettingsFragment.NOTEBOOK_PATH_KEY,
//            resources.getString(R.string.pref_default_notebook_path)
//        )
//        navigationViewModel!!.navigateForward(notebookPath)
    }
}
