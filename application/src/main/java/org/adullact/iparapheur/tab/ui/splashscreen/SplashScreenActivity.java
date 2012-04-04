package org.adullact.iparapheur.tab.ui.splashscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.folder.FolderActivity;

public class SplashScreenActivity
        extends Activity
{

    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.i( TAG, "onCreate" );
        setContentView( R.layout.splashscreen );
        new SplashScreenTask()
        {

            @Override
            protected void onPostExecute( Void result )
            {
                startActivity( new Intent( getApplication(), FolderActivity.class ) );
            }

        }.execute( new Void[]{} );
    }

}
