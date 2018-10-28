package info.maaskant.wmsnotes.android.app

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import info.maaskant.wmsnotes.android.app.di2.AppComponent
import info.maaskant.wmsnotes.android.app.di2.DaggerAppComponent
import timber.log.Timber
import javax.inject.Inject


class WmsNotesApplication : Application(), HasSupportFragmentInjector {
//    @Inject
//    internal var instrumentation: ApplicationInstrumentation? = null
//
//    @Inject
//    internal var navigationViewModel: NavigationViewModel? = null
//
//    var graph: Graph? = null
//        private set
@Inject
lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    lateinit var component: AppComponent


    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        component = DaggerAppComponent.builder()
            .application(this)
            .context(this)
            .build()
        component.inject(this)

//        DaggerWmsNotesApplicationComponent.create().inject(this)
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

    override fun supportFragmentInjector() = fragmentInjector
}
