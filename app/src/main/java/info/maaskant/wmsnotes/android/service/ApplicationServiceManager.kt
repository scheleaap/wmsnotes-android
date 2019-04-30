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
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.client.synchronization.SynchronizationTask
import info.maaskant.wmsnotes.model.folder.FolderCommandExecutor
import info.maaskant.wmsnotes.model.note.NoteCommandExecutor
import info.maaskant.wmsnotes.model.note.policy.NoteTitlePolicy
import info.maaskant.wmsnotes.utilities.ApplicationService
import timber.log.Timber
import javax.inject.Inject

class ApplicationServiceManager : Service() {
    @Inject
    lateinit var synchronizationTask: SynchronizationTask

    @Inject
    lateinit var folderCommandExecutor: FolderCommandExecutor

    @Inject
    lateinit var noteCommandExecutor: NoteCommandExecutor

    @Inject
    lateinit var noteTitlePolicy: NoteTitlePolicy

    @Inject
    lateinit var treeIndex: TreeIndex

    lateinit var services: List<ApplicationService>

    private lateinit var binder: Binder

    override fun onBind(intent: Intent?): IBinder? {
        Timber.v("onBind")
        services.forEach(ApplicationService::start)
        synchronizationTask.pause()
        synchronizationTask.start()

        if (!this::binder.isInitialized) {
            binder = Binder()
        }
        return binder
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        // Not sure why injecting services directly does not work.
        services = listOf(
            folderCommandExecutor,
            noteCommandExecutor,
            noteTitlePolicy,
            treeIndex
        )
        Timber.v("onCreate")
        super.onCreate()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.v("onUnbind")
        synchronizationTask.shutdown()
        services.reversed().forEach(ApplicationService::shutdown)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Timber.v("onDestroy")
        super.onDestroy()
    }

    inner class Binder : android.os.Binder() {
        internal val service: ApplicationServiceManager
            get() = this@ApplicationServiceManager
    }

    class ServiceBindingLifecycleObserver constructor(
        private val context: Context,
        lifecycle: Lifecycle
    ) : ServiceConnection, LifecycleObserver {

        val isBound: Boolean
            get() = boundService != null

        var boundService: ApplicationServiceManager? = null


        init {
            lifecycle.addObserver(this)
        }

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
                context.unbindService(this);
            }
        }
    }
}
