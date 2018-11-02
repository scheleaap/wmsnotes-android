package info.maaskant.wmsnotes.android.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import info.maaskant.wmsnotes.android.app.di.ViewModelKey
import info.maaskant.wmsnotes.android.client.indexing.FolderIndex

@Module(
    includes = [
        NavigationModule.ProvideViewModel::class
    ]
)
abstract class NavigationModule {

    @ContributesAndroidInjector(
        modules = [
            InjectViewModel::class
        ]
    )

    abstract fun bind(): NavigationFragment

    @Module
    class ProvideViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(NavigationViewModel::class)
        fun provideNavigationViewModel(folderIndex: FolderIndex): ViewModel =
            NavigationViewModel(folderIndex)
    }

    @Module
    class InjectViewModel {

        @Provides
        fun provideNavigationViewModel(
            factory: ViewModelProvider.Factory,
            target: NavigationFragment
        ): NavigationViewModel =
            ViewModelProviders.of(target, factory).get(NavigationViewModel::class.java)
    }

}
