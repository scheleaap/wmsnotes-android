package info.maaskant.wmsnotes.android.ui.settings

import android.preference.ListPreference
import android.preference.Preference

/**
 * A preference value change listener that updates the preference's summary to reflect its new
 * value. The exact display format is dependent on the type of preference.
 */
internal class BindPreferenceSummaryToValueListener : Preference.OnPreferenceChangeListener {

    override fun onPreferenceChange(preference: Preference, value: Any): Boolean {
        val stringValue = value.toString()

        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            val index = preference.findIndexOfValue(stringValue)

            // Set the summary to reflect the new value.
            preference.setSummary(if (index >= 0) preference.entries[index] else null)

        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.summary = stringValue
        }
        return true
    }

}
