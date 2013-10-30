package org.adullact.iparapheur.controller.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.adullact.iparapheur.R;

/**
 * Created by jmaire on 29/10/13.
 */
/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
public class GeneralPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }
}