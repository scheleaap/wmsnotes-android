package info.maaskant.wmsnotes.android.app.instrumentation

interface Instrumentation {
    fun init()
}

interface ApplicationInstrumentation : Instrumentation {
    val leakTracing: LeakTracing
}

class DebugApplicationInstrumentation(override val leakTracing: LeakTracing) : ApplicationInstrumentation {
    override fun init() {
        leakTracing.init()
    }
}

