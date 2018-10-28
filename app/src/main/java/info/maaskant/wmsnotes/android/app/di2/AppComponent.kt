package info.maaskant.wmsnotes.android.app.di2

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import info.maaskant.wmsnotes.android.app.WmsNotesApplication
import javax.inject.Singleton

@Component(modules = [
    OtherModule::class,
    DataModule::class,
    AndroidSupportInjectionModule::class,
    UiModule::class
])
@Singleton
interface AppComponent {

    fun inject(app: WmsNotesApplication)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: WmsNotesApplication): Builder

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}
