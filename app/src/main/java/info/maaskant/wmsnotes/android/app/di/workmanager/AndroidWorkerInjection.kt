package info.maaskant.wmsnotes.android.app.di.workmanager

import androidx.work.Worker

object AndroidWorkerInjection {

    fun inject(worker: Worker) {
        val application = worker.applicationContext
        if (application !is HasWorkerInjector) {
            throw RuntimeException("${application.javaClass.canonicalName} does not implement ${HasWorkerInjector::class.java.canonicalName}")
        }

        val workerInjector = application.workerInjector()
        checkNotNull(workerInjector) { "${application.javaClass}.workerInjector() return null" }
        workerInjector.inject(worker)
    }
}
