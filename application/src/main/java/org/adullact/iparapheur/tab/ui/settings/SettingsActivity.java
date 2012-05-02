package org.adullact.iparapheur.tab.ui.settings;

import roboguice.activity.RoboPreferenceActivity;

import com.google.inject.Inject;

import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;

public class SettingsActivity
        extends RoboPreferenceActivity
{

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

}
