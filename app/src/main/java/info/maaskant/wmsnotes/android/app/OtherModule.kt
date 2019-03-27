package info.maaskant.wmsnotes.android.app

import android.content.Context
import android.content.pm.PackageInfo
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import timber.log.Timber
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class OtherModule {

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AppDirectory

    @Provides
    @Singleton
    @AppDirectory
    fun appDirectory(context: Context): File {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
//        val appDirectory = File(packageInfo.applicationInfo.dataDir)
        val appDirectory = File("/storage/emulated/0/wmsnotes")
        appDirectory.mkdirs()
        Timber.d("The application directory is %s", appDirectory)
        return appDirectory
    }

    @Provides
    @Singleton
    fun kryoPool(): Pool<Kryo> {
        return object : Pool<Kryo>(true, true) {
            override fun create(): Kryo = Kryo()
        }
    }

}
