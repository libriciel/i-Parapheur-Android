package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Office;

public class DashboardActivity
        extends Activity
{

    private static final String TAG = DashboardActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.i( TAG, "onCreate" );
        setContentView( R.layout.dashboard );
        new DashboardLoadingTask( this, "Chargement des bureaux" )
        {

            @Override
            protected void beforeDialogDismiss( List<Office> result )
            {
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
