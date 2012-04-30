package org.adullact.iparapheur.tab.ui.splashscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.akquinet.android.androlog.Log;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.dashboard.DashboardActivity;

public class SplashScreenActivity
        extends Activity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Log.i( "onCreate" );

        super.onCreate( savedInstanceState );

        setContentView( R.layout.splashscreen );
        new SplashScreenTask()
        {

            @Override
            protected void onPostExecute( Void result )
            {
                startActivity( new Intent( getApplication(), DashboardActivity.class ) );
            }

        }.execute( new Void[]{} );
    }

}
