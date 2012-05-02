package org.adullact.iparapheur.tab.ui.office;

import android.os.Bundle;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboActivity;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;

public class OfficeActivity
        extends RoboActivity
{

    private static final String TAG = OfficeActivity.class.getSimpleName();

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

    @Inject
    private ActionBarActivityObserver actionBarObserver;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Log.i( TAG, "onCreate" );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.office );
    }

}
