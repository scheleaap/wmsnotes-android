package info.maaskant.wmsnotes.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.android.app.di2.AppViewModelFactory
import info.maaskant.wmsnotes.android.ui.navigation.NavigationModule
import javax.inject.Provider

@Module(
    includes = [
        NavigationModule::class
    ]
)
class UiModule {

    @Provides
    fun provideViewModelFactory(
        providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory =
        AppViewModelFactory(providers)
}
