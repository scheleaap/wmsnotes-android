package info.maaskant.wmsnotes.android.ui.di

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DefaultFragmentFactory @Inject constructor(
    private val providerMap: Map<Class<out Fragment>,
            @JvmSuppressWildcards Provider<Fragment>>
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val fragmentClass = loadFragmentClass(classLoader, className)

        val creator = providerMap[fragmentClass] ?: providerMap.entries.firstOrNull {
            fragmentClass.isAssignableFrom(it.key)
        }?.value

        return creator?.get() ?: super.instantiate(classLoader, className)
    }

    companion object {
        /** Sets up FragmentFactory for an activity. Call this method before super.onCreate() is called. */
        fun setUp(activity: AppCompatActivity) {
            val entryPoint = EntryPointAccessors.fromActivity(
                activity,
                DefaultFragmentFactoryEntryPoint::class.java
            )
            activity.supportFragmentManager.fragmentFactory = entryPoint.getFragmentFactory()
        }
    }
}
