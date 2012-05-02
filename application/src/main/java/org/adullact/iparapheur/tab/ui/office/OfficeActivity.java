package org.adullact.iparapheur.tab.ui.office;

import android.os.Bundle;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;

public class OfficeActivity
        extends RoboFragmentActivity
{

    public static final String EXTRA_OFFICE_IDENTITY = "office:identity";

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

    @Inject
    private ActionBarActivityObserver actionBarObserver;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        String officeIdentity = getIntent().getExtras().getString( EXTRA_OFFICE_IDENTITY );
        Log.i( "onCreate for office: " + officeIdentity );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.office );
    }

}
