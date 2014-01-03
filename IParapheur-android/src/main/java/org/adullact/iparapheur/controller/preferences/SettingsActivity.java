package org.adullact.iparapheur.controller.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(MyAccounts.INSTANCE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(MyAccounts.INSTANCE);
    }

    /** {@inheritDoc} */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceTitleToValueListener
     */
    public static void bindPreferenceTitleToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceTitleToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        //sBindPreferenceTitleToValueListener.onPreferenceChange(preference, PreferenceManager
        //        .getDefaultSharedPreferences(preference.getContext())
        //        .getString(preference.getKey(), ""));
    }

    public static void bindPreferenceTitleToPreferenceValue(Preference preference, Preference reference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceTitleToValueListener);
    }

    /**
     * A preference value change listener that updates the preference's title
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceTitleToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            preference.setTitle(value.toString());
            return true;
        }
    };


}
