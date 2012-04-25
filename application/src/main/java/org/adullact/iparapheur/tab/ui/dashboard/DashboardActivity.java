package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import roboguice.activity.RoboActivity;

import com.google.inject.Inject;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;

public class DashboardActivity
        extends RoboActivity
{

    /* package */ static final String TAG = DashboardActivity.class.getSimpleName();

    @Inject
    /* package */ IParapheurHttpClient client;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.i( TAG, "onCreate" );
        setContentView( R.layout.dashboard );
        new DashboardLoadingTask( this )
        {

            @Override
            protected void beforeDialogDismiss( Map<String, Office> result )
            {
                Log.d( TAG, "Got result: " + result );
                // TODO Populate views with result
            }

        }.execute( new Void[]{} );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        return super.onCreateOptionsMenu( menu );
    }

}
