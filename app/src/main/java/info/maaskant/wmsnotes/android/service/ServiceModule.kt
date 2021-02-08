package info.maaskant.wmsnotes.android.service

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    internal abstract fun applicationServiceManager(): ApplicationServiceManager
}
