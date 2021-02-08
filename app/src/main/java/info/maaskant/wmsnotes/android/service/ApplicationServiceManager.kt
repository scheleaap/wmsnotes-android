package info.maaskant.wmsnotes.android.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import dagger.android.AndroidInjection
import info.maaskant.wmsnotes.android.client.synchronization.EmergencyBrake
import info.maaskant.wmsnotes.android.client.synchronization.PreferenceBoundSynchronizationTask
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.model.folder.FolderCommandExecutor
import info.maaskant.wmsnotes.model.note.NoteCommandExecutor
import info.maaskant.wmsnotes.model.note.policy.NoteTitlePolicy
import info.maaskant.wmsnotes.utilities.ApplicationService
import info.maaskant.wmsnotes.utilities.logger
import leakcanary.LeakSentry
import javax.inject.Inject

class ApplicationServiceManager : Service() {
    private val logger by logger()

    @Inject
    lateinit var emergencyBrake: EmergencyBrake

    @Inject
    lateinit var folderCommandExecutor: FolderCommandExecutor

    @Inject
    lateinit var noteCommandExecutor: NoteCommandExecutor

    @Inject
    lateinit var noteTitlePolicy: NoteTitlePolicy

    @Inject
    lateinit var treeIndex: TreeIndex

    @Inject
    lateinit var synchronizationTask: PreferenceBoundSynchronizationTask

    lateinit var services: List<ApplicationService>

    private var binder: Binder? = null

    override fun onBind(intent: Intent?): IBinder? {
        logger.trace("onBind")
        services.forEach(ApplicationService::start)
        if (binder == null) {
            binder = Binder(this)
        }
        return binder
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        // Not sure why injecting services directly does not work.
        services = listOf(
            emergencyBrake,
            folderCommandExecutor,
            noteCommandExecutor,
            noteTitlePolicy,
            treeIndex,
            synchronizationTask
        )
        logger.trace("onCreate")
        super.onCreate()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logger.trace("onUnbind")
        services.reversed().forEach(ApplicationService::shutdown)
        binder?.onUnbind()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        logger.trace("onDestroy")
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }

    class Binder(applicationServiceManager: ApplicationServiceManager) : android.os.Binder() {
        private var _service: ApplicationServiceManager? = applicationServiceManager

        internal val service: ApplicationServiceManager
            get() = _service!!

        fun onUnbind() {
            // To prevent memory leaks
            _service = null
        }
    }

    class ServiceBindingLifecycleObserver constructor(
        private val context: Context
    ) : ServiceConnection, LifecycleObserver {

        val isBound: Boolean
            get() = boundService != null

        var boundService: ApplicationServiceManager? = null

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            boundService = (binder as Binder).service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            context.bindService(
                Intent(context, ApplicationServiceManager::class.java),
                this,
                AppCompatActivity.BIND_AUTO_CREATE
            )
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            if (isBound) {
                context.unbindService(this)
            }
        }
    }
}
