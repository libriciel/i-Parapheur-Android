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
            Thread.sleep( 500 );
        } catch ( InterruptedException ignored ) {
        }
        if ( true ) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
            if ( !sharedPreferences.contains( PREFS_PREFIX + "AccountTest0" + PREFS_TITLE_SUFFIX ) ) {

                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_TITLE_SUFFIX, "iParapheur local" );
                editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_URL_SUFFIX, "jmaire.test.adullact.org" );
                editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_LOGIN_SUFFIX, "android" );
                editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_PASSWORD_SUFFIX, "secret" );

                editor.apply();
                editor.commit();
            }
        }

        return null;
    }

}
