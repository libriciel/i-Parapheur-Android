package org.adullact.iparapheur.tab.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.codeartisans.java.toolbox.exceptions.NullArgumentException;

import org.adullact.iparapheur.tab.model.Account;

/**
 * Accounts Repository.
 * 
 * Loads accounts data from SharedPreferences.
 */
@ContextSingleton
public class AccountsRepository
{

    public static final String PREFS_PREFIX = "account_";

    public static final String PREFS_TITLE_SUFFIX = "_title";

    public static final String PREFS_URL_SUFFIX = "_url";

    public static final String PREFS_LOGIN_SUFFIX = "_login";

    public static final String PREFS_PASSWORD_SUFFIX = "_password";

    @Inject
    private Context context;

    private Set<String> accountIdentities = null;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener()
    {

        public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
        {
            if ( key.startsWith( PREFS_PREFIX ) ) {
                accountIdentities = null;
                ensureAccountIdentities();
            }
        }

    };

    public void onActivityResume( @Observes OnResumeEvent event )
    {
        PreferenceManager.getDefaultSharedPreferences( context ).registerOnSharedPreferenceChangeListener( prefListener );
        ensureAccountIdentities();
    }

    public void onActivityPause( @Observes OnPauseEvent event )
    {
        PreferenceManager.getDefaultSharedPreferences( context ).unregisterOnSharedPreferenceChangeListener( prefListener );
        accountIdentities = null;
    }

    public Account addNewAccount()
    {
        String identity = UUID.randomUUID().toString();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString( PREFS_PREFIX + identity + PREFS_TITLE_SUFFIX, "Nouveau Compte" );
        editor.putString( PREFS_PREFIX + identity + PREFS_URL_SUFFIX, "" );
        editor.putString( PREFS_PREFIX + identity + PREFS_LOGIN_SUFFIX, "" );
        editor.putString( PREFS_PREFIX + identity + PREFS_PASSWORD_SUFFIX, "" );
        editor.apply();
        editor.commit();

        return byIdentity( identity );
    }

    public Account byIdentity( String identity )
    {
        NullArgumentException.ensureNotEmpty( "Account identity", identity );
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        Set<String> keySet = sharedPreferences.getAll().keySet();
        if ( !keySet.contains( PREFS_PREFIX + identity + PREFS_TITLE_SUFFIX ) ) {
            return null;
        }
        String title = sharedPreferences.getString( PREFS_PREFIX + identity + PREFS_TITLE_SUFFIX, null );
        String url = sharedPreferences.getString( PREFS_PREFIX + identity + PREFS_URL_SUFFIX, null );
        String login = sharedPreferences.getString( PREFS_PREFIX + identity + PREFS_LOGIN_SUFFIX, null );
        String password = sharedPreferences.getString( PREFS_PREFIX + identity + PREFS_PASSWORD_SUFFIX, null );
        return new Account( identity, title, url, login, password );

    }

    public void deleteAccount( String identity )
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        Set<String> keySet = sharedPreferences.getAll().keySet();
        if ( !keySet.contains( PREFS_PREFIX + identity + PREFS_TITLE_SUFFIX ) ) {
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove( PREFS_PREFIX + identity + PREFS_TITLE_SUFFIX );
        editor.remove( PREFS_PREFIX + identity + PREFS_URL_SUFFIX );
        editor.remove( PREFS_PREFIX + identity + PREFS_LOGIN_SUFFIX );
        editor.remove( PREFS_PREFIX + identity + PREFS_PASSWORD_SUFFIX );
        editor.apply();
        editor.commit();
    }

    public List<Account> all()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        List<Account> accounts = new ArrayList<Account>();

        for ( String eachAccountIdentity : ensureAccountIdentities() ) {
            String title = sharedPreferences.getString( PREFS_PREFIX + eachAccountIdentity + PREFS_TITLE_SUFFIX, null );
            String url = sharedPreferences.getString( PREFS_PREFIX + eachAccountIdentity + PREFS_URL_SUFFIX, null );
            String login = sharedPreferences.getString( PREFS_PREFIX + eachAccountIdentity + PREFS_LOGIN_SUFFIX, null );
            String password = sharedPreferences.getString( PREFS_PREFIX + eachAccountIdentity + PREFS_PASSWORD_SUFFIX, null );
            accounts.add( new Account( eachAccountIdentity, title, url, login, password ) );
        }

        return accounts;
    }

    private synchronized Set<String> ensureAccountIdentities()
    {
        if ( accountIdentities == null ) {
            accountIdentities = new HashSet<String>();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
            for ( String eachPrefName : sharedPreferences.getAll().keySet() ) {
                if ( eachPrefName.startsWith( PREFS_PREFIX ) ) {
                    String identity = eachPrefName.substring( eachPrefName.indexOf( "_" ) + 1 );
                    identity = identity.substring( 0, identity.lastIndexOf( "_" ) );
                    if ( !accountIdentities.contains( identity ) ) {
                        accountIdentities.add( identity );
                    }
                }
            }
        }
        return accountIdentities;
    }

}
