package info.maaskant.wmsnotes.android.app.instrumentation

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

interface LeakTracing : Instrumentation {
    fun traceLeakage(reference: Any)
}

class LeakCanaryTracing(private val application: Application) : LeakTracing {
    private var refWatcher: RefWatcher? = null

    override fun init() {
        refWatcher = LeakCanary.install(application)
    }

    override fun traceLeakage(reference: Any) {
        refWatcher!!.watch(reference)
    }
}
