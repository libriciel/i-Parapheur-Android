package org.adullact.iparapheur.controller;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.adullact.iparapheur.BuildConfig;
import org.adullact.iparapheur.controller.account.MyAccounts;

import io.fabric.sdk.android.Fabric;


public class IParapheurApplication extends Application {

	private static Context context;

	public static Context getContext() {
		return context;
	}

	@Override public void onCreate() {
		super.onCreate();

		CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
		Fabric.with(this, new Crashlytics.Builder().core(core).build());

		context = this;

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (!sharedPreferences.contains(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_TITLE_SUFFIX)) {
			SharedPreferences.Editor editor = sharedPreferences.edit();

			editor.putString(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_TITLE_SUFFIX, "iParapheur demo");
			editor.putString(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_URL_SUFFIX, "parapheur.demonstrations.adullact.org");
			editor.putString(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_LOGIN_SUFFIX, "bma");
			editor.putString(MyAccounts.PREFS_ACCOUNT_PREFIX + "AccountTest0" + MyAccounts.PREFS_PASSWORD_SUFFIX, "secret");

			editor.apply();
		}
	}
}
