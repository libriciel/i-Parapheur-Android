package org.adullact.iparapheur.tab.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import roboguice.activity.RoboPreferenceActivity;

import com.google.inject.Inject;

import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import static org.adullact.iparapheur.tab.services.AccountsRepository.*;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;

public class AccountsActivity
        extends RoboPreferenceActivity
        implements Refreshable
{

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

    @Inject
    private ActionBarActivityObserver actionBarObserver;

    @Inject
    private AccountsRepository accountsRepository;

    private PreferenceCategory accountsCategory;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Button add = new Button( this );
        add.setText( "Ajouter un compte" );
        add.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                Account account = accountsRepository.addNewAccount();
                buildAccountPrefScreen( accountsCategory, account );
            }

        } );
        setListFooter( add );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        refresh();
    }

    public void refresh()
    {
        PreferenceScreen rootScreen = getPreferenceManager().createPreferenceScreen( this );
        rootScreen.setTitle( "Comptes iParapheur" );

        accountsCategory = new PreferenceCategory( this );
        accountsCategory.setTitle( "Comptes iParapheur" );
        rootScreen.addPreference( accountsCategory );

        for ( final Account account : accountsRepository.all() ) {
            buildAccountPrefScreen( accountsCategory, account );
        }

        setPreferenceScreen( rootScreen );
    }

    private void buildAccountPrefScreen( PreferenceCategory parent, Account account )
    {
        final PreferenceScreen accountScreen = getPreferenceManager().createPreferenceScreen( this );
        accountScreen.setTitle( account.getTitle() );
        accountScreen.setSummary( buildAccountSummary( account ) );
        parent.addPreference( accountScreen );

        final PreferenceCategory accountCategory = new PreferenceCategory( this );
        accountCategory.setTitle( account.getTitle() );
        accountScreen.addPreference( accountCategory );

        // Account Title
        String titlePrefKey = PREFS_PREFIX + account.getIdentity() + PREFS_TITLE_SUFFIX;
        final EditTextPreference titlePref = new EditTextPreference( this );
        titlePref.setDialogTitle( "Nom du compte" );
        titlePref.setTitle( account.getTitle() );
        titlePref.setText( account.getTitle() );
        titlePref.setSummary( "Nom du compte" );
        titlePref.setKey( titlePrefKey );
        titlePref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener()
        {

            public boolean onPreferenceChange( Preference pref, Object value )
            {
                String title = value.toString();
                accountScreen.setTitle( title );
                accountCategory.setTitle( title );
                titlePref.setTitle( title );
                return true;
            }

        } );
        accountCategory.addPreference( titlePref );

        // IParapheur URL
        String urlPrefKey = PREFS_PREFIX + account.getIdentity() + PREFS_URL_SUFFIX;
        final EditTextPreference urlPref = new EditTextPreference( this );
        urlPref.setDialogTitle( "Adresse du serveur iParapheur" );
        urlPref.setTitle( account.getUrl() );
        urlPref.setText( account.getUrl() );
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
        accountCategory.addPreference( urlPref );

        // Login
        String loginPrefKey = PREFS_PREFIX + account.getIdentity() + PREFS_LOGIN_SUFFIX;
        final EditTextPreference loginPref = new EditTextPreference( this );
        loginPref.setDialogTitle( "Identifiant" );
        loginPref.setTitle( account.getLogin() );
        loginPref.setText( account.getLogin() );
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
        accountCategory.addPreference( loginPref );

        // Password
        String passwordPrefKey = PREFS_PREFIX + account.getIdentity() + PREFS_PASSWORD_SUFFIX;
        final EditTextPreference passwordPref = new EditTextPreference( this );
        passwordPref.getEditText().setTransformationMethod( PasswordTransformationMethod.getInstance() );
        passwordPref.setDialogTitle( "Mot de passe" );
        passwordPref.setTitle( "************" );
        passwordPref.setText( account.getPassword() );
        passwordPref.setSummary( "Mot de passe" );
        passwordPref.setKey( passwordPrefKey );
        accountCategory.addPreference( passwordPref );

        accountCategory.addPreference( new DeletePreference( this, accountsRepository, account ) );
    }

    private static class DeletePreference
            extends Preference
    {

        private final AccountsRepository accountsRepository;

        private final Account account;

        public DeletePreference( AccountsActivity context, AccountsRepository accountsRepository, Account account )
        {
            super( context );
            this.accountsRepository = accountsRepository;
            this.account = account;
            setTitle( "Supprimer ce compte" );
        }

        @Override
        public View getView( View convertView, ViewGroup parent )
        {
            if ( convertView == null ) {
                Button delete = new Button( getContext() );
                delete.setText( "Supprimer ce compte" );
                delete.setOnClickListener( new View.OnClickListener()
                {

                    public void onClick( View view )
                    {
                        accountsRepository.deleteAccount( account.getIdentity() );
                        getContext().startActivity( new Intent( getContext(), AccountsActivity.class ) );
                    }

                } );
                convertView = delete;
            }
            return convertView;
        }

    }

    private static String buildAccountSummary( Account account )
    {
        if ( account.validates() ) {
            return account.getLogin() + " @ " + account.getUrl();
        } else {
            return "Vous devriez compléter ce compte.";
        }
    }

}
