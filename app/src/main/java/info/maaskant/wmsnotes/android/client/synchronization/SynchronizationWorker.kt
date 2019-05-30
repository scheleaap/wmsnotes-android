package info.maaskant.wmsnotes.android.client.synchronization

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import info.maaskant.wmsnotes.android.app.di.workmanager.AndroidWorkerInjection
import info.maaskant.wmsnotes.client.synchronization.LocalEventImporter
import info.maaskant.wmsnotes.client.synchronization.RemoteEventImporter
import info.maaskant.wmsnotes.client.synchronization.Synchronizer
import javax.inject.Inject

class SynchronizationWorker constructor(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject
    lateinit var localEventImporter: LocalEventImporter

    @Inject
    lateinit var remoteEventImporter: RemoteEventImporter

    @Inject
    lateinit var synchronizer: Synchronizer

    override fun doWork(): Result {
        return Result.FAILURE // TODO
//        return try {
//            logger.debug("Running {}", this::class.java.simpleName)
//            injectDependencies()
//            localEventImporter.loadAndStoreLocalEvents()
//            remoteEventImporter.loadAndStoreRemoteEvents()
//            synchronizer.synchronize()
//            Result.SUCCESS
//        } catch (t: Throwable) {
//            logger.warn(t)
//            Result.RETRY
//        }
    }

    private fun injectDependencies() {
        // Replace with official Dagger support in the future: https://github.com/google/dagger/issues/1183
        AndroidWorkerInjection.inject(this)
    }
}
