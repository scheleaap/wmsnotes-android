package info.maaskant.wouttest2.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import info.maaskant.wouttest2.R;

public class SettingsFragment extends PreferenceFragment {

    public static String NOTEBOOK_PATH_KEY = "notebook_path";

    private static final Preference.OnPreferenceChangeListener BIND_PREFERENCE_SUMMARY_TO_VALUE_LISTENER = new BindPreferenceSummaryToValueListener();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        bindPreferenceSummaryToValue(findPreference(NOTEBOOK_PATH_KEY));
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is
     * changed, its summary (line of text below the preference title) is updated to reflect the
     * value. The summary is also immediately updated upon calling this method.
     *
     * @see BindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(BIND_PREFERENCE_SUMMARY_TO_VALUE_LISTENER);

        // Trigger the listener immediately with the preference's
        // current value.
        BIND_PREFERENCE_SUMMARY_TO_VALUE_LISTENER.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

}
