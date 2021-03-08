package info.maaskant.wmsnotes.android.ui.di

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import info.maaskant.wmsnotes.android.ui.debug.DebugFragment
import info.maaskant.wmsnotes.android.ui.navigation.NavigationFragment
import info.maaskant.wmsnotes.android.ui.settings.SettingsFragment

@Module
@InstallIn(SingletonComponent::class) // TODO ActivityComponent?
abstract class FragmentFactoryModule {

    @Binds
    @IntoMap
    @FragmentKey(DebugFragment::class)
    abstract fun bindDebugFragment(fragment: DebugFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(NavigationFragment::class)
    abstract fun bindNavigationFragment(fragment: NavigationFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SettingsFragment::class)
    abstract fun bindSettingsFragment(fragment: SettingsFragment): Fragment

}
