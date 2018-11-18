package info.maaskant.wmsnotes.android.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import info.maaskant.wmsnotes.android.app.di.viewmodel.ViewModelKey

@Module(
    includes = [
        DetailModule.ProvideViewModel::class
    ]
)
abstract class DetailModule {
    @ContributesAndroidInjector(modules = [InjectViewModel::class, DetailFragmentsModule::class])
    internal abstract fun contributeActivityInjector(): DetailActivity

    @Module
    abstract class ProvideViewModel {
        @Binds
        @IntoMap
        @ViewModelKey(DetailViewModel::class)
        abstract fun provideDetailViewModel(detailViewModel: DetailViewModel): ViewModel
    }

    @Module
    class InjectViewModel {
        @Provides
        fun provide(
            factory: ViewModelProvider.Factory,
            target: DetailActivity
        ): DetailViewModel {
            @Suppress("UnnecessaryVariable")
            val viewModel = ViewModelProviders.of(target, factory).get(DetailViewModel::class.java)
            return viewModel
        }
    }

    @Module
    abstract class DetailFragmentsModule {
        @ContributesAndroidInjector
        internal abstract fun contributeViewerFragment(): ViewerFragment

        @ContributesAndroidInjector
        internal abstract fun contributeEditorFragment(): EditorFragment
    }
}

