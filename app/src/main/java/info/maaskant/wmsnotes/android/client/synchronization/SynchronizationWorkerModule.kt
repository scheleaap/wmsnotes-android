package info.maaskant.wmsnotes.android.client.synchronization

import androidx.work.Worker
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import info.maaskant.wmsnotes.android.app.di.workmanager.WorkerKey

@Module(subcomponents = [SynchronizationWorkerSubcomponent::class])
abstract class SynchronizationWorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(SynchronizationWorker::class)
    abstract fun bindMyWorkerFactory(builder: SynchronizationWorkerSubcomponent.Builder): AndroidInjector.Factory<out Worker>

}
