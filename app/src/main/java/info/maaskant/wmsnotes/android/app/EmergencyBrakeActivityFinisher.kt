package info.maaskant.wmsnotes.android.app

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import info.maaskant.wmsnotes.android.client.synchronization.EmergencyBrake
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class EmergencyBrakeActivityFinisher constructor(
    private val emergencyBrake: EmergencyBrake,
    private val activity: AppCompatActivity
) : LifecycleObserver {
    private val logger by logger()

    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        disposables.add(
            emergencyBrake.triggered()
                .observeOn(Schedulers.computation())
                .subscribeBy(
                    onNext = {
                        logger.error("Emergency brake was pulled, finishing activity $activity")
                        activity.finishAndRemoveTask()
                    },
                    onError = { logger.warn("Error", it) })
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposables.clear()
    }
}
