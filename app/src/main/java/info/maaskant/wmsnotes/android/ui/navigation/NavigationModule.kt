package info.maaskant.wmsnotes.android.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import info.maaskant.wmsnotes.android.app.di.viewmodel.ViewModelKey

@Module(
    includes = [
        NavigationModule.ProvideViewModel::class
    ]
)
abstract class NavigationModule {
    @ContributesAndroidInjector(modules = [InjectViewModel::class])
    internal abstract fun contributeNavigationFragment(): NavigationFragment

    @Module
    abstract class ProvideViewModel {
        @Binds
        @IntoMap
        @ViewModelKey(NavigationViewModel::class)
        abstract fun provideNavigationViewModel(navigationViewModel: NavigationViewModel): ViewModel
    }

    @Module
    class InjectViewModel {
        @Provides
        fun provideNavigationViewModel(
            factory: ViewModelProvider.Factory,
            target: NavigationFragment
        ): NavigationViewModel =
            ViewModelProvider(target, factory).get(NavigationViewModel::class.java)
    }
}
