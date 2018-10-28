//package info.maaskant.wmsnotes.android.app.di;
//
//import android.app.Application;
//import android.content.ContentResolver;
//import android.content.Context;
//import dagger.Module;
//import dagger.Provides;
//
//import javax.inject.Singleton;
//
//@Module
//public class ApplicationModule {
//
//    Application application;
//
//    public ApplicationModule(Application application) {
//        this.application = application;
//    }
//
//    @Provides
//    @Singleton
//    @ForApplication
//    Context providesApplicationContext() {
//        return application;
//    }
//
//    @Provides
//    @Singleton
//    Application providesApplication() {
//        return application;
//    }
//
//    @Provides
//    @Singleton
//    public ContentResolver contentResolver(@ForApplication Context context) {
//        return context.getContentResolver();
//    }
//
//}
