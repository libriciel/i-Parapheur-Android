package org.adullact.iparapheur.tab.ui.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;

import roboguice.activity.RoboPreferenceActivity;

import com.google.inject.Inject;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import static org.adullact.iparapheur.tab.services.AccountsRepository.*;

public class AccountsActivity
        extends RoboPreferenceActivity
{

    @Inject
    private AccountsRepository accountsRepository;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen( this );
        root.setTitle( "Comptes iParapheur" );
        root.setSummary( "Entrez ici le ou les comptes iParapheur ˆ utiliser avec iParapheurTab." );

        for ( Account eachAccount : accountsRepository.all() ) {

            PreferenceCategory eachAccountCategory = new PreferenceCategory( this );
            eachAccountCategory.setTitle( eachAccount.getTitle() );
            root.addPreference( eachAccountCategory );

            // Account Title
            String titlePrefKey = PREFS_PREFIX + eachAccount.getIdentity() + PREFS_TITLE_SUFFIX;
            final EditTextPreference titlePref = new EditTextPreference( this );
            titlePref.setDialogTitle( "Nom du compte" );
            titlePref.setTitle( eachAccount.getTitle() );
            titlePref.setText( eachAccount.getTitle() );
            titlePref.setSummary( "Nom du compte" );
            titlePref.setKey( titlePrefKey );
            titlePref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener()
            {

                public boolean onPreferenceChange( Preference pref, Object o )
                {
                    titlePref.setTitle( o.toString() );
                    return true;
                }

            } );
            eachAccountCategory.addPreference( titlePref );

            // IParapheur URL
            String urlPrefKey = PREFS_PREFIX + eachAccount.getIdentity() + PREFS_URL_SUFFIX;
            final EditTextPreference urlPref = new EditTextPreference( this );
            urlPref.setDialogTitle( "Adresse du serveur iParapheur" );
            urlPref.setTitle( eachAccount.getUrl() );
            urlPref.setText( eachAccount.getUrl() );
            urlPref.setSummary( "Adresse du serveur iParapheur" );
            urlPref.setKey( urlPrefKey );
            urlPref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener()
            {

                public boolean onPreferenceChange( Preference prfrnc, Object o )
                {
                    urlPref.setTitle( o.toString() );
                    return true;
                }

            } );
            eachAccountCategory.addPreference( urlPref );

            // Login
            String loginPrefKey = PREFS_PREFIX + eachAccount.getIdentity() + PREFS_LOGIN_SUFFIX;
            final EditTextPreference loginPref = new EditTextPreference( this );
            loginPref.setDialogTitle( "Identifiant" );
            loginPref.setTitle( eachAccount.getLogin() );
            loginPref.setText( eachAccount.getLogin() );
            loginPref.setSummary( "Identifiant" );
            loginPref.setKey( loginPrefKey );
            loginPref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener()
            {

                public boolean onPreferenceChange( Preference prfrnc, Object o )
                {
                    loginPref.setTitle( o.toString() );
                    return true;
                }

            } );
            eachAccountCategory.addPreference( loginPref );

            // Password
            String passwordPrefKey = PREFS_PREFIX + eachAccount.getIdentity() + PREFS_PASSWORD_SUFFIX;
            final EditTextPreference passwordPref = new EditTextPreference( this );
            passwordPref.getEditText().setTransformationMethod( PasswordTransformationMethod.getInstance() );
            passwordPref.setDialogTitle( "Mot de passe" );
            passwordPref.setTitle( "************" );
            passwordPref.setText( eachAccount.getPassword() );
            passwordPref.setSummary( "Mot de passe" );
            passwordPref.setKey( passwordPrefKey );
            eachAccountCategory.addPreference( passwordPref );

        }

        setFinishOnTouchOutside( true ); // Does it work ?
        setPreferenceScreen( root );

        // Add a button to the header list.
        Button button = new Button( this );
        button.setText( "Ajouter un compte" );
        setListFooter( button );
    }

}
