package info.maaskant.wmsnotes.android.client.synchronization

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import info.maaskant.wmsnotes.client.synchronization.SynchronizationTask

class SynchronizationTaskLifecycleObserver constructor(
    private val synchronizationTask: SynchronizationTask
) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        synchronizationTask.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun shutdown() {
        synchronizationTask.shutdown()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun pause() {
        synchronizationTask.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun unpause() {
        synchronizationTask.unpause()
    }
}
