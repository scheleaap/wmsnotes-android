package info.maaskant.wmsnotes.android.ui.settings

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import info.maaskant.wmsnotes.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)

        bindPreferenceSummaryToValue(findPreference(SERVER_HOSTNAME_KEY))
    }

    companion object {

        var SERVER_HOSTNAME_KEY = "server_hostname"

        private val BIND_PREFERENCE_SUMMARY_TO_VALUE_LISTENER = BindPreferenceSummaryToValueListener()

        /**
         * Binds a preference's summary to its value. More specifically, when the preference's value is
         * changed, its summary (line of text below the preference title) is updated to reflect the
         * value. The summary is also immediately updated upon calling this method.
         *
         * @see BindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = BIND_PREFERENCE_SUMMARY_TO_VALUE_LISTENER

            // Trigger the listener immediately with the preference's
            // current value.
            BIND_PREFERENCE_SUMMARY_TO_VALUE_LISTENER.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, "") as Any
            )
        }
    }

}
