package info.maaskant.wmsnotes.android.client.synchronization

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class SynchronizationTaskLifecycleObserver constructor(
    private val synchronizationTask: SynchronizationTask,
    private val context: Context,
    private val lifecycle: Lifecycle
) :
    LifecycleObserver {
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
