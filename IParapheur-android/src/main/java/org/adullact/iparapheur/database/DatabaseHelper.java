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
package org.adullact.iparapheur.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.AccountUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	public static final String DATABASE_NAME = "iParapheur.db";
	private static final int DATABASE_VERSION = 1;

	private Dao<Account, Integer> mAccountDao;
	private Dao<Bureau, Integer> mBureauDao;
	private Dao<Dossier, Integer> mDossierDao;
	private Dao<Document, Integer> mDocumentDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		retrieveLegacyAccounts(context);
		createDefaultDemoAccount();
	}

	// <editor-fold desc="OrmLiteSqliteOpenHelper">

	@Override public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {

		try {
			TableUtils.createTable(connectionSource, Account.class);
			TableUtils.createTable(connectionSource, Bureau.class);
			TableUtils.createTable(connectionSource, Dossier.class);
			TableUtils.createTable(connectionSource, Document.class);
		}
		catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create databases", e);
		}
	}

	@Override public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {

		try {
			// In case of change in database of next version of application, please increase the value of DATABASE_VERSION variable,
			// then this method will be invoked automatically. Developer needs to handle the upgrade logic here,
			// i.e. create a new table or a new column to an existing table, take the backups of the existing database etc.

			TableUtils.dropTable(connectionSource, Document.class, true);
			TableUtils.dropTable(connectionSource, Dossier.class, true);
			TableUtils.dropTable(connectionSource, Bureau.class, true);
			TableUtils.dropTable(connectionSource, Account.class, true);

			onCreate(sqliteDatabase, connectionSource);
		}
		catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
		}
	}

	// </editor-fold desc="OrmLiteSqliteOpenHelper">

	// Create the getDao methods of all database tables to access those from android code.
	// Insert, delete, read, update everything will be happened through DAOs

	public @NonNull Dao<Account, Integer> getAccountDao() throws SQLException {

		if (mAccountDao == null)
			mAccountDao = getDao(Account.class);

		return mAccountDao;
	}

	public @NonNull Dao<Bureau, Integer> getBureauDao() throws SQLException {

		if (mBureauDao == null)
			mBureauDao = getDao(Bureau.class);

		return mBureauDao;
	}

	public @NonNull Dao<Dossier, Integer> getDossierDao() throws SQLException {

		if (mDossierDao == null)
			mDossierDao = getDao(Dossier.class);

		return mDossierDao;
	}

	public @NonNull Dao<Document, Integer> getDocumentDao() throws SQLException {

		if (mDocumentDao == null)
			mDocumentDao = getDao(Document.class);

		return mDocumentDao;
	}

	/**
	 * This will probably call multiples operations at once,
	 * You should wrap it in a {@link Dao#callBatchTasks(Callable)} to avoid multiple DB operations at once,
	 * and keep respectable performances:
	 *
	 * @param instanceToDelete the target
	 * @throws SQLException
	 */
	public void deleteCascade(@NonNull Dossier instanceToDelete) throws SQLException {
		getDocumentDao().delete(instanceToDelete.getChildrenDocuments());
		getDossierDao().delete(instanceToDelete);
	}

	/**
	 * This will probably call multiples operations at once,
	 * You should wrap it in a {@link Dao#callBatchTasks(Callable)} to avoid multiple DB operations at once,
	 * and keep respectable performances:
	 *
	 * @param instanceToDelete the target
	 * @throws SQLException
	 */
	public void deleteCascade(@NonNull Bureau instanceToDelete) throws SQLException {

		for (Dossier dossier : instanceToDelete.getChildrenDossiers())
			getDocumentDao().delete(dossier.getChildrenDocuments());

		getDossierDao().delete(instanceToDelete.getChildrenDossiers());
		getBureauDao().delete(instanceToDelete);
	}

	// <editor-fold desc="Utils">

	public void retrieveLegacyAccounts(@NonNull Context context) {

		final List<Account> legacyAccountList = new ArrayList<>();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		// Retrieve data

		for (String pref : sharedPreferences.getAll().keySet()) {
			if (pref.startsWith("account_")) {

				String id = pref.substring(pref.indexOf("_") + 1);
				id = id.substring(0, id.lastIndexOf("_"));

				Account account = new Account(id);
				account.setTitle(sharedPreferences.getString("account_" + id + "_title", ""));
				account.setLogin(sharedPreferences.getString("account_" + id + "_login", ""));
				account.setServerBaseUrl(sharedPreferences.getString("account_" + id + "_url", ""));
				account.setPassword(sharedPreferences.getString("account_" + id + "_password", ""));
				account.setActivated(sharedPreferences.getBoolean("account_" + id + "_activated", true));

				legacyAccountList.add(account);
			}
		}

		// Saving in DataBase...

		try {
			getAccountDao().callBatchTasks(new Callable<Object>() {

				@Override public Object call() throws Exception {

					for (Account account : legacyAccountList)
						mAccountDao.createOrUpdate(account);

					return null;
				}
			});
		}
		catch (Exception e) { e.printStackTrace(); }

		// Deleting old data

		SharedPreferences.Editor editor = sharedPreferences.edit();
		for (Account legacyAccount : legacyAccountList) {
			String id = legacyAccount.getId();
			editor.remove("account_" + id + "_title");
			editor.remove("account_" + id + "_url");
			editor.remove("account_" + id + "_login");
			editor.remove("account_" + id + "_password");
			editor.remove("account_" + id + "_activated");
		}
		editor.commit();
	}

	public void createDefaultDemoAccount() {

		List<Account> demoList = new ArrayList<>();

		try { demoList.addAll(getAccountDao().queryBuilder().where().eq(Account.DB_FIELD_ID, AccountUtils.DEMO_ID).query()); }
		catch (SQLException e) { e.printStackTrace(); }

		if (demoList.isEmpty()) {
			Account demoAccount = AccountUtils.getDemoAccount();
			demoAccount.setActivated(true);

			try { getAccountDao().createOrUpdate(demoAccount); }
			catch (SQLException e) { e.printStackTrace(); }

			AccountUtils.SELECTED_ACCOUNT = demoAccount;
		}
	}

	// </editor-fold desc="Utils">

}
