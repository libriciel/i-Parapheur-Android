package org.adullact.iparapheur.tab.ui.splashscreen;

import android.os.AsyncTask;

public class SplashScreenTask
        extends AsyncTask<Void, Void, Void>
{

    @Override
    protected Void doInBackground( Void... paramss )
    {
        try {
            Thread.sleep( 2000 );
        } catch ( InterruptedException ex ) {
            // Ignored
        }
        return null;
    }

}
