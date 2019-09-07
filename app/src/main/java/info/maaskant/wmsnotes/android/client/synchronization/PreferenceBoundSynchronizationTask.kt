package info.maaskant.wmsnotes.android.client.synchronization

import com.f2prateek.rx.preferences2.Preference
import info.maaskant.wmsnotes.android.app.PreferencesModule.SynchronizationEnabled
import info.maaskant.wmsnotes.client.synchronization.SynchronizationTask
import info.maaskant.wmsnotes.utilities.ApplicationService
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceBoundSynchronizationTask @Inject constructor(
    private val wrappedTask: SynchronizationTask,
    @SynchronizationEnabled private val synchronizationEnabled: Preference<Boolean>
) : ApplicationService {

    private val logger by logger()

    private var disposable: Disposable? = null

    private fun connect(): Disposable =
        synchronizationEnabled.asObservable()
            .subscribeBy(
                onNext = { enabled ->
                    if (enabled) {
                        wrappedTask.unpause()
                    } else {
                        wrappedTask.pause()
                    }
                },
                onError = { logger.warn("Error", it) }
            )

    @Synchronized
    override fun start() {
        if (disposable == null) {
            disposable = connect()
            wrappedTask.start()
        }
    }

    @Synchronized
    override fun shutdown() {
        wrappedTask.shutdown()
        disposable?.dispose()
        disposable = null
    }
}
