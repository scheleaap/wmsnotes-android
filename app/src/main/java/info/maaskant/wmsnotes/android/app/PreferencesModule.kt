package info.maaskant.wmsnotes.android.app

import android.content.Context
import android.content.res.Resources
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.R
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class PreferencesModule {
    @Provides
    @Singleton
    fun preferences(context: Context): RxSharedPreferences =
        RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(context))

    @Provides
    @Singleton
    fun resources(context: Context): Resources =
        context.resources

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ServerHostname

    @Provides
    @Singleton
    @ServerHostname
    fun preference001(rxPreferences: RxSharedPreferences, resources: Resources): Preference<String> =
        rxPreferences.getString(
            resources.getString(R.string.pref_server_hostname_key),
            resources.getString(R.string.pref_server_hostname_default)
        )

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class SynchronizationEnabled

    @Provides
    @Singleton
    @SynchronizationEnabled
    fun preference002(rxPreferences: RxSharedPreferences, resources: Resources): Preference<Boolean> =
        rxPreferences.getBoolean(
            resources.getString(R.string.pref_synchronization_enabled_key),
            resources.getBoolean(R.bool.pref_synchronization_enabled_default)
        )
}
