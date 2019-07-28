package info.maaskant.wmsnotes.android.client.synchronization

import androidx.lifecycle.Lifecycle
import com.f2prateek.rx.preferences2.Preference
import info.maaskant.wmsnotes.client.synchronization.SynchronizationTask
import io.sellmair.disposer.disposeBy
import io.sellmair.disposer.onStop

object SynchronizationUtils {
    fun connectSynchronizationToPreference(
        synchronizationEnabled: Preference<Boolean>,
        lifecycle: Lifecycle,
        synchronizationTask: SynchronizationTask
    ) {
        synchronizationEnabled.asObservable()
            .subscribe { enabled ->
                if (enabled) {
                    synchronizationTask.unpause()
                } else {
                    synchronizationTask.pause()
                }
            }
            .disposeBy(lifecycle.onStop)
    }
}
