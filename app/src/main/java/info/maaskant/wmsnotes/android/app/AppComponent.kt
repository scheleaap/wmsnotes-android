package info.maaskant.wmsnotes.android.app

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import info.maaskant.wmsnotes.android.client.indexing.IndexingModule
import info.maaskant.wmsnotes.android.model.ModelModule
import info.maaskant.wmsnotes.android.ui.UiModule
import javax.inject.Singleton

@Component(modules = [
    OtherModule::class,
    ModelModule::class,
    IndexingModule::class,
    AndroidSupportInjectionModule::class,
    UiModule::class
])
@Singleton
interface AppComponent {

    fun inject(app: App)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: App): Builder

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}
