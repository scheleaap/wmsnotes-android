//package info.maaskant.wmsnotes.android.app.di
//
//import android.app.Application
//import dagger.Module
//import dagger.Provides
//import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
//import info.maaskant.wmsnotes.android.app.instrumentation.DebugApplicationInstrumentation
//import info.maaskant.wmsnotes.android.app.instrumentation.LeakCanaryTracing
//import info.maaskant.wmsnotes.android.app.instrumentation.LeakTracing
//
//import javax.inject.Singleton
//
//@Module
//class InstrumentationModule {
//
//    @Provides
//    @Singleton
//    fun providesInstrumentation(leakTracing: LeakTracing): ApplicationInstrumentation {
//        return DebugApplicationInstrumentation(leakTracing)
//    }
//
//    @Provides
//    @Singleton
//    fun providesLeakTracing(application: Application): LeakTracing {
//        return LeakCanaryTracing(application)
//    }
//
//}
