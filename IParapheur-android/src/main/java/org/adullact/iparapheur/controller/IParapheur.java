package org.adullact.iparapheur.controller;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.adullact.iparapheur.controller.account.MyAccounts;

/**
 * Created by jmaire on 23/10/13.
 */
public class IParapheur extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPreferences.contains(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_TITLE_SUFFIX))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_TITLE_SUFFIX, "iParapheur demo" );
            editor.putString(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_URL_SUFFIX, "parapheur.demonstrations.adullact.org" );
            editor.putString(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_LOGIN_SUFFIX, "bma" );
            editor.putString(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_PASSWORD_SUFFIX, "secret" );

            editor.apply();
            editor.commit();
        }
    }

    @Override
    public void onTerminate() {

    }

    public static Context getContext(){
        return context;
    }
}
