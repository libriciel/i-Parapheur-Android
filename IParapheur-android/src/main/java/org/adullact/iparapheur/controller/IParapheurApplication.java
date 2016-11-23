/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.controller;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.adullact.iparapheur.BuildConfig;
import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.account.MyAccounts;

import io.fabric.sdk.android.Fabric;


public class IParapheurApplication extends Application {

	@Override public void onCreate() {
		super.onCreate();

		// Crashlytics init

		CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
		Fabric.with(this, new Crashlytics.Builder().core(core).build());

		// Database init

		// Accounts init

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String basePrefsAccount = MyAccounts.PREFS_ACCOUNT_PREFIX + getString(R.string.demo_account_id);

		if (!sharedPreferences.contains(basePrefsAccount + MyAccounts.PREFS_TITLE_SUFFIX)) {
			SharedPreferences.Editor editor = sharedPreferences.edit();

			editor.putString(basePrefsAccount + MyAccounts.PREFS_TITLE_SUFFIX, getString(R.string.demo_account_title));
			editor.putString(basePrefsAccount + MyAccounts.PREFS_URL_SUFFIX, getString(R.string.demo_account_url));
			editor.putString(basePrefsAccount + MyAccounts.PREFS_LOGIN_SUFFIX, getString(R.string.demo_account_login));
			editor.putString(basePrefsAccount + MyAccounts.PREFS_PASSWORD_SUFFIX, getString(R.string.demo_account_password));

			editor.apply();
		}
	}
}
