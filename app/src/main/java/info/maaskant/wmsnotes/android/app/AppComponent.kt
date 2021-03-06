package info.maaskant.wmsnotes.android.app

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import info.maaskant.wmsnotes.android.client.indexing.IndexingModule
import info.maaskant.wmsnotes.android.client.synchronization.SynchronizationModule
import info.maaskant.wmsnotes.android.model.AggregateModule
import info.maaskant.wmsnotes.android.model.CommandModule
import info.maaskant.wmsnotes.android.model.EventModule
import info.maaskant.wmsnotes.android.service.ServiceModule
import info.maaskant.wmsnotes.android.ui.UiModule
import info.maaskant.wmsnotes.android.ui.util.glide.GlideModule
import javax.inject.Singleton

@Component(
    modules = [
        AggregateModule::class,
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        CommandModule::class,
        EventModule::class,
        IndexingModule::class,
        OtherModule::class,
        PreferencesModule::class,
        ServiceModule::class,
        StringsModule::class,
        SynchronizationModule::class,
        UiModule::class
    ]
)
@Singleton
interface AppComponent {

    fun inject(app: App)

    fun inject(glide: GlideModule)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: App): Builder

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}
