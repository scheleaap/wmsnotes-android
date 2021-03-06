package info.maaskant.wmsnotes.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.android.app.di.viewmodel.AppViewModelFactory
import info.maaskant.wmsnotes.android.ui.debug.DebugModule
import info.maaskant.wmsnotes.android.ui.detail.DetailModule
import info.maaskant.wmsnotes.android.ui.main.MainModule
import info.maaskant.wmsnotes.android.ui.navigation.NavigationModule
import info.maaskant.wmsnotes.android.ui.settings.SettingsModule
import javax.inject.Provider
import javax.inject.Singleton

@Module(
    includes = [
        DebugModule::class,
        DetailModule::class,
        MainModule::class,
        NavigationModule::class,
        SettingsModule::class
    ]
)
class UiModule {

    @Provides
    @Singleton
    fun provideViewModelFactory(
        providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory =
        AppViewModelFactory(providers)
}
