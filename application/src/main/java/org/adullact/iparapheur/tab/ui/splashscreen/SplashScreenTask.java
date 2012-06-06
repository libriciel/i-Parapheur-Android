package org.adullact.iparapheur.tab.ui.splashscreen;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import static org.adullact.iparapheur.tab.services.AccountsRepository.*;

public class SplashScreenTask
        extends AsyncTask<Void, Void, Void>
{

    private final SplashScreenActivity context;

    public SplashScreenTask( SplashScreenActivity context )
    {
        this.context = context;
    }

    @Override
    protected Void doInBackground( Void... paramss )
    {
        try {
            Thread.sleep( 1000 );
        } catch ( InterruptedException ex ) {
            // Ignored
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        if ( !sharedPreferences.contains( PREFS_PREFIX + "AccountTest0" + PREFS_TITLE_SUFFIX ) ) {

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_TITLE_SUFFIX, "iParapheur de DEV" );
            editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_URL_SUFFIX, "http://parapheur.test.adullact.org/alfresco/service" );
            editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_LOGIN_SUFFIX, "eperalta" );
            editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_PASSWORD_SUFFIX, "secret" );

            /*
            editor.putString( PREFS_PREFIX + "AccountTest1" + PREFS_TITLE_SUFFIX, "Mairie de Montpellier" );
            editor.putString( PREFS_PREFIX + "AccountTest1" + PREFS_URL_SUFFIX, "http://iparapheur.montpellier.fr" );
            editor.putString( PREFS_PREFIX + "AccountTest1" + PREFS_LOGIN_SUFFIX, "john.doe" );
            editor.putString( PREFS_PREFIX + "AccountTest1" + PREFS_PASSWORD_SUFFIX, "changeit" );
            */

            editor.apply();
            editor.commit();
        }

        return null;
    }

}
