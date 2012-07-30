package org.adullact.iparapheur.tab.ui.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import com.google.inject.Inject;
import de.akquinet.android.androlog.Log;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.dashboard.DashboardActivity;
import org.codeartisans.android.toolbox.activity.RoboActivity;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;

public class SplashScreenActivity
        extends RoboActivity
{

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Log.d( "onCreate" );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.splashscreen );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        new SplashScreenTask( this )
        {

            @Override
            protected void onPostExecute( Void result )
            {
                startActivity( new Intent( getApplication(), DashboardActivity.class ) );
            }

        }.execute( new Void[]{} );
    }

}
