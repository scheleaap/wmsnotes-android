package info.maaskant.wmsnotes.android.app.di.workmanager

import androidx.work.Worker
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.Multibinds

@Module
abstract class AndroidWorkerInjectionModule {
    @Multibinds
    @Suppress(names = ["UNUSED"])
    abstract fun workerInjectorFactories(): Map<Class<out Worker>, AndroidInjector.Factory<out Worker>>

    @Multibinds
    @Suppress(names = ["UNUSED"])
    abstract fun workerStringInjectorFactories(): Map<String, AndroidInjector.Factory<out Worker>>
}
