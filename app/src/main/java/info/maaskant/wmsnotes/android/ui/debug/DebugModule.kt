package info.maaskant.wmsnotes.android.ui.debug

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
        DebugModule.ProvideViewModel::class
    ]
)
abstract class DebugModule {
    @ContributesAndroidInjector(modules = [InjectViewModel::class])
    internal abstract fun contributeDebugFragment(): DebugFragment

    @Module
    abstract class ProvideViewModel {
        @Binds
        @IntoMap
        @ViewModelKey(DebugViewModel::class)
        abstract fun provideDebugViewModel(debugViewModel: DebugViewModel): ViewModel
    }

    @Module
    class InjectViewModel {
        @Provides
        fun provideDebugViewModel(
            factory: ViewModelProvider.Factory,
            target: DebugFragment
        ): DebugViewModel =
            ViewModelProvider(target, factory).get(DebugViewModel::class.java)
    }
}
