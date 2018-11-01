package info.maaskant.wmsnotes.android.ui.main

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainModule {
    @ContributesAndroidInjector
    internal abstract fun contributeActivityInjector(): MainActivity
}
