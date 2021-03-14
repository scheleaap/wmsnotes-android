package info.maaskant.wmsnotes.android.client.synchronization

import android.annotation.SuppressLint
import info.maaskant.wmsnotes.client.synchronization.SynchronizationTask
import info.maaskant.wmsnotes.model.CommandError
import info.maaskant.wmsnotes.utilities.ApplicationService
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyBrake @Inject constructor(
    private val synchronizationTask: SynchronizationTask
) : ApplicationService {

    private val logger by logger()

    private val triggered: Subject<Unit> = BehaviorSubject.create<Unit>().toSerialized()

    private var disposable: Disposable? = null

    private fun connect(): Disposable =
        Observables.combineLatest(
            Observable.just(getEmergencyFileStatus()),
            synchronizationTask.getSynchronizationResult()
        )
            .observeOn(Schedulers.computation())
            .filter { (emergencyFileStatus, synchronizationResult) ->
                emergencyFileStatus != EmergencyFileStatus.OverrideActive &&
                        synchronizationResult.errors.any { (_, commandError) ->
                            commandError !is CommandError.NetworkError
                        }
            }
            .distinct()
            .subscribeBy(
                onNext = {
                    writeEmergencyFile()
                    triggered.onNext(Unit)
                },
                onError = { logger.warn("Error", it) }
            )

    @Synchronized
    override fun start() {
        if (disposable == null) {
            logger.debug("Starting")
            disposable = connect()
        }
    }

    @Synchronized
    override fun shutdown() {
        disposable?.let {
            logger.debug("Shutting down")
            it.dispose()
        }
        disposable = null
    }

    fun triggered(): Observable<Unit> = triggered

    companion object {
        private val basePath = File("/storage/emulated/0/wmsnotes")

        @SuppressLint("SimpleDateFormat")
        fun getEmergencyFileStatus(): EmergencyFileStatus {
            val brakeExists = basePath.walk().filter { it.name.startsWith("emergency") }.any()
            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
            val brakeOverrideExists = basePath.resolve("emergency.$currentDate").exists()

            return if (brakeExists && !brakeOverrideExists) {
                EmergencyFileStatus.EmergencyActive
            } else if (brakeExists && brakeOverrideExists) {
                EmergencyFileStatus.OverrideActive
            } else {
                EmergencyFileStatus.Inactive
            }
        }

        fun writeEmergencyFile() {
            val brakePath = basePath.resolve("emergency")
            if (!brakePath.exists()) {
                brakePath.createNewFile()
            }
        }
    }

    sealed class EmergencyFileStatus {
        object Inactive : EmergencyFileStatus()
        object EmergencyActive : EmergencyFileStatus()
        object OverrideActive : EmergencyFileStatus()
    }
}
