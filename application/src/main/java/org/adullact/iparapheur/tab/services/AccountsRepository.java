package org.adullact.iparapheur.tab.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.adullact.iparapheur.tab.model.Account;

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

    public List<Account> all()
    {
        List<Account> accounts = loadAllFromSharedPreferences();
        if ( !accounts.isEmpty() ) {
            System.out.println( "#######################################################################" );
            System.out.println( "Accounts loaded from SharedPreferences :" );
            System.out.println( Arrays.toString( accounts.toArray() ) );
            System.out.println( "#######################################################################" );
            return accounts;
        }
        // TODO REMOVE BEGIN
        Editor editor = PreferenceManager.getDefaultSharedPreferences( context ).edit();

        editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_TITLE_SUFFIX, "iParapheur de DEV" );
        editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_URL_SUFFIX, "http://parapheur.test.adullact.org/alfresco/service" );
        editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_LOGIN_SUFFIX, "eperalta" );
        editor.putString( PREFS_PREFIX + "AccountTest0" + PREFS_PASSWORD_SUFFIX, "secret" );

        editor.putString( PREFS_PREFIX + "AccountTest1" + PREFS_TITLE_SUFFIX, "Montpellier Agglomération" );
        editor.putString( PREFS_PREFIX + "AccountTest1" + PREFS_URL_SUFFIX, "http://iparapheur.montpellier-agglo.com" );
        editor.putString( PREFS_PREFIX + "AccountTest1" + PREFS_LOGIN_SUFFIX, "john.doe" );
        editor.putString( PREFS_PREFIX + "AccountTest1" + PREFS_PASSWORD_SUFFIX, "changeit" );

        editor.putString( PREFS_PREFIX + "AccountTest2" + PREFS_TITLE_SUFFIX, "Mairie de Montpellier" );
        editor.putString( PREFS_PREFIX + "AccountTest2" + PREFS_URL_SUFFIX, "http://iparapheur.montpellier.fr" );
        editor.putString( PREFS_PREFIX + "AccountTest2" + PREFS_LOGIN_SUFFIX, "john.doe" );
        editor.putString( PREFS_PREFIX + "AccountTest2" + PREFS_PASSWORD_SUFFIX, "changeit" );

        editor.putString( PREFS_PREFIX + "AccountTest3" + PREFS_TITLE_SUFFIX, "Région Languedoc Roussillon" );
        editor.putString( PREFS_PREFIX + "AccountTest3" + PREFS_URL_SUFFIX, "http://iparapheur.laregion.fr" );
        editor.putString( PREFS_PREFIX + "AccountTest3" + PREFS_LOGIN_SUFFIX, "john.doe" );
        editor.putString( PREFS_PREFIX + "AccountTest3" + PREFS_PASSWORD_SUFFIX, "changeit" );

        editor.apply();

        return loadAllFromSharedPreferences();
        // TODO REMOVE END
    }

    private List<Account> loadAllFromSharedPreferences()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        List<Account> accounts = new ArrayList<Account>();

        // Gather Account identities
        List<String> accountIdentities = new ArrayList<String>();
        for ( String eachPrefName : sharedPreferences.getAll().keySet() ) {
            if ( eachPrefName.startsWith( PREFS_PREFIX ) ) {
                String identity = eachPrefName.substring( eachPrefName.indexOf( "_" ) + 1 );
                identity = identity.substring( 0, identity.lastIndexOf( "_" ) );
                if ( !accountIdentities.contains( identity ) ) {
                    accountIdentities.add( identity );
                }
            }
        }

        // Create Account instances
        for ( String eachAccountIdentity : accountIdentities ) {
            System.out.println( eachAccountIdentity );
            String title = sharedPreferences.getString( PREFS_PREFIX + eachAccountIdentity + PREFS_TITLE_SUFFIX, null );
            String url = sharedPreferences.getString( PREFS_PREFIX + eachAccountIdentity + PREFS_URL_SUFFIX, null );
            String login = sharedPreferences.getString( PREFS_PREFIX + eachAccountIdentity + PREFS_LOGIN_SUFFIX, null );
            String password = sharedPreferences.getString( PREFS_PREFIX + eachAccountIdentity + PREFS_PASSWORD_SUFFIX, null );
            accounts.add( new Account( eachAccountIdentity, title, url, login, password ) );
        }

        return accounts;
    }

}
