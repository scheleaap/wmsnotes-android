package info.maaskant.wmsnotes.android.ui.settings

//import info.maaskant.wmsnotes.android.app.PreferencesModule
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.PreferencesModule
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.rxkotlin.subscribeBy
import io.sellmair.disposer.disposeBy
import io.sellmair.disposer.onStop
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment @Inject constructor(
    @PreferencesModule.SynchronizationEnabled // Thanks to JakeWharton, https://github.com/google/dagger/issues/848#issuecomment-323554193
    private val synchronizationEnabled: com.f2prateek.rx.preferences2.Preference<Boolean>,
) : PreferenceFragmentCompat() {
    private val logger by logger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupServerHostnamePreference()
    }

    private fun setupServerHostnamePreference() {
        val serverHostnamePreference: EditTextPreference? = findPreference("server_hostname")
        synchronizationEnabled.asObservable()
            .subscribeBy(onNext = {
                serverHostnamePreference?.isEnabled = it
            }, onError = { logger.warn("Error", it) })
            .disposeBy(onStop)
    }
}
