package info.maaskant.wmsnotes.android.client.synchronization

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface SynchronizationWorkerSubcomponent : AndroidInjector<SynchronizationWorker> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<SynchronizationWorker>()
}
