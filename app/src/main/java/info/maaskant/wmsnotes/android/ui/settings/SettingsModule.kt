package info.maaskant.wmsnotes.android.ui.settings

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsModule {
    @ContributesAndroidInjector
    internal abstract fun contributeSettingsFragment(): SettingsFragment
}
