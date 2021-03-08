package info.maaskant.wmsnotes.android.app

import android.content.Context
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import info.maaskant.wmsnotes.utilities.logger
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class OtherModule {
    private val logger by logger()

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AppDirectory

    @Provides
    @Singleton
    @AppDirectory
    fun appDirectory(@ApplicationContext context: Context): File {
        // TODO: Replace both solutions with Context.getFilesDir() and Context.getCacheDir() (don't forget to move the contents!)
//        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
//        val appDirectory = File(packageInfo.applicationInfo.dataDir)
        val appDirectory = File("/storage/emulated/0/wmsnotes")
        appDirectory.mkdirs()
        logger.debug("The application directory is {}", appDirectory)
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
