package info.maaskant.wmsnotes.android.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.util.Log
import androidx.fragment.app.Fragment
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import ch.qos.logback.core.util.StatusPrinter
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import info.maaskant.wmsnotes.BuildConfig
import info.maaskant.wmsnotes.android.app.upgrades.ServerHostnameUpgrade
import info.maaskant.wmsnotes.android.client.synchronization.EmergencyBrake
import info.maaskant.wmsnotes.android.client.synchronization.EmergencyBrake.EmergencyFileStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import kotlin.system.exitProcess

class App : Application(), HasActivityInjector, HasSupportFragmentInjector, HasServiceInjector {
    lateinit var component: AppComponent

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var serverHostnameUpgrade: ServerHostnameUpgrade

    private fun createLogcatAppenderAndStart(loggerContext: LoggerContext): LogcatAppender {
        val encoder = PatternLayoutEncoder().also {
            it.context = loggerContext
            it.charset = Charset.forName("UTF-8")
            it.pattern = "%date %logger{25} %level [%thread] %msg%n"
            it.start()
        }

        val tagEncoder = PatternLayoutEncoder().also {
            it.context = loggerContext
            it.charset = Charset.forName("UTF-8")
            it.pattern = "wmsnotes"
            it.start()
        }

        @Suppress("UnnecessaryVariable") val logcatAppender = LogcatAppender().also {
            it.context = loggerContext
            it.encoder = encoder
            it.tagEncoder = tagEncoder
            it.start()
        }
        return logcatAppender
    }

    private fun createFileAppenderForAllMessagesAndStart(
        loggerContext: LoggerContext,
        logDirectory: File
    ): Appender<ILoggingEvent> {
        val encoder = createPatternLayoutEncoderAndStart(loggerContext)

        val rollingFileAppender = RollingFileAppender<ILoggingEvent>().also {
            it.context = loggerContext
            it.isAppend = true
            it.file = "$logDirectory/all-latest.txt"
        }

        val rollingPolicy = SizeAndTimeBasedRollingPolicy<ILoggingEvent>().also {
            it.context = loggerContext
            it.fileNamePattern = "$logDirectory/all-%d{yyyy-MM-dd}.%i.txt"
            it.setMaxFileSize(FileSize.valueOf("1MB"))
            it.maxHistory = 5
            it.setParent(rollingFileAppender)  // parent and context required!
            it.start()
        }

        rollingFileAppender.rollingPolicy = rollingPolicy
        rollingFileAppender.encoder = encoder
        rollingFileAppender.start()

        return rollingFileAppender
    }

    private fun createFileAppenderForWarningMessagesAndStart(
        loggerContext: LoggerContext,
        logDirectory: File
    ): Appender<ILoggingEvent> {
        val encoder = createPatternLayoutEncoderAndStart(loggerContext)

        val fileAppender = FileAppender<ILoggingEvent>().also {
            it.context = loggerContext
            it.isAppend = true
            it.file = "$logDirectory/warn-and-error.txt"
        }

        fileAppender.encoder = encoder
        fileAppender.addFilter(ThresholdFilter().also {
            it.setLevel(Level.WARN.levelStr)
            it.start()
        })
        fileAppender.start()

        return fileAppender
    }

    private fun createPatternLayoutEncoderAndStart(loggerContext: LoggerContext) =
        PatternLayoutEncoder().also {
            it.context = loggerContext
            it.charset = Charset.forName("UTF-8")
            it.pattern = "%date %logger %level [%thread] %msg%n"
            it.start()
        }

    private fun exitIfEmergencyBrakeWasPulled() {
        if (EmergencyBrake.getEmergencyFileStatus() == EmergencyFileStatus.EmergencyActive) {
            Log.e("wmsnotes", "Emergency brake was pulled, exiting")
            exitProcess(1)
        }
    }

    override fun onCreate() {
        super.onCreate()

        exitIfEmergencyBrakeWasPulled()
        setupLogging()
        setupDependencyInjection()
        serverHostnameUpgrade.run()
    }

    private fun setupDependencyInjection() {
        component = DaggerAppComponent.builder()
            .application(this)
            .context(this)
            .build()
        component.inject(this)
    }

    private fun setupLogging() {
        // Source: https://sureshjoshi.com/mobile/file-logging-in-android-with-timber/

        // TODO: Replace with Context.getFilesDir(), see OtherModule as well
        //        val logDirectory = context.filesDir.toString() + "/logs"
        val logDirectory = File("/storage/emulated/0/wmsnotes/logs")

        // Reset the default context (which may already have been initialized) since we want to reconfigure it.
        val loggerContext = (LoggerFactory.getILoggerFactory() as LoggerContext).also {
            it.reset()
        }

        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
        root.let {
            it.level = Level.DEBUG
            // In the future, we may want to make this conditional on BuildConfig.DEBUG.
            it.addAppender(createLogcatAppenderAndStart(loggerContext))
            if (logDirectory.exists()) {
                it.addAppender(
                    createFileAppenderForAllMessagesAndStart(
                        loggerContext,
                        logDirectory
                    )
                )
                it.addAppender(
                    createFileAppenderForWarningMessagesAndStart(
                        loggerContext,
                        logDirectory
                    )
                )
            }
        }

        if (BuildConfig.DEBUG) {
            // Print any status messages (warnings, etc.) encountered in the logback config.
            StatusPrinter.print(loggerContext)
        }

//        val logger = LoggerFactory.getLogger("TEST")
//        logger.trace("TEST TRACE MESSAGE")
//        logger.debug("TEST DEBUG MESSAGE")
//        logger.info("TEST INFO MESSAGE")
//        logger.warn("TEST WARN MESSAGE")
//        logger.error("TEST ERROR MESSAGE")
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector
    override fun supportFragmentInjector() = fragmentInjector
    override fun serviceInjector(): AndroidInjector<Service> = serviceInjector
}
