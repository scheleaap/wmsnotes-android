package info.maaskant.wmsnotes.android.app

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.work.Worker
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import info.maaskant.wmsnotes.android.app.di.workmanager.HasWorkerInjector
import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
import timber.log.Timber
import javax.inject.Inject


class App : Application(), HasActivityInjector, HasSupportFragmentInjector, HasWorkerInjector {

    @Inject
    lateinit var instrumentation: ApplicationInstrumentation

//    @Inject
//    internal var navigationViewModel: NavigationViewModel? = null

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var workerInjector: DispatchingAndroidInjector<Worker>

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

// TODO: LEAK TESTING
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
// TODO: LEAK TESTING

        Timber.plant(Timber.DebugTree())
        component = DaggerAppComponent.builder()
            .application(this)
            .context(this)
            .build()
        component.inject(this)

// TODO: LEAK TESTING
//        instrumentation.init()

//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        val notebookPath = sharedPreferences.getString(
//            SettingsFragment.NOTEBOOK_PATH_KEY,
//            resources.getString(R.string.pref_default_notebook_path)
//        )
//        navigationViewModel!!.navigateForward(notebookPath)
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector
    override fun supportFragmentInjector() = fragmentInjector
    override fun workerInjector() = workerInjector
}
