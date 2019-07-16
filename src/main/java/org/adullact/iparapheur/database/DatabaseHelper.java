/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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

import static org.adullact.iparapheur.utils.AccountUtils.DEMO_BASE_URL;
import static org.adullact.iparapheur.utils.AccountUtils.DEMO_ID;
import static org.adullact.iparapheur.utils.AccountUtils.DEMO_LOGIN;
import static org.adullact.iparapheur.utils.AccountUtils.DEMO_PASSWORD;
import static org.adullact.iparapheur.utils.AccountUtils.DEMO_TITLE;


/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String LOG_TAG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "iParapheur.db";

    private Dao<Account, Integer> mAccountDao;
    private Dao<Bureau, Integer> mBureauDao;
    private Dao<Dossier, Integer> mDossierDao;
    private Dao<Document, Integer> mDocumentDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        retrieveLegacyAccounts(context);
        createDefaultDemoAccount();
        retrieveSelectedAccount(context);
    }

    // <editor-fold desc="OrmLiteSqliteOpenHelper">

    @Override public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {

        try {
            TableUtils.createTable(connectionSource, Account.class);
            TableUtils.createTable(connectionSource, Bureau.class);
            TableUtils.createTable(connectionSource, Dossier.class);
            TableUtils.createTable(connectionSource, Document.class);
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
        } catch (Exception e) { Log.e(LOG_TAG, e.getLocalizedMessage()); }

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

    private void createDefaultDemoAccount() {

        List<Account> demoList = new ArrayList<>();

        try { demoList.addAll(getAccountDao().queryBuilder().where().eq(Account.DB_FIELD_ID, DEMO_ID).query()); } catch (SQLException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
        }

        if (demoList.isEmpty()) {

            Account demoAccount = new Account(DEMO_ID, DEMO_TITLE, DEMO_BASE_URL, DEMO_LOGIN, DEMO_PASSWORD, null, null);
            demoAccount.setActivated(true);

            try { getAccountDao().createOrUpdate(demoAccount); } catch (SQLException e) { Log.e(LOG_TAG, e.getLocalizedMessage()); }
        }
    }

    private void retrieveSelectedAccount(@NonNull Context context) {

        String selectedAccountId = AccountUtils.loadSelectedAccountId(context);

        // Load from DB

        List<Account> accountList = new ArrayList<>();
        try { accountList.addAll(getAccountDao().queryForAll()); } catch (SQLException e) { Log.e(LOG_TAG, e.getLocalizedMessage()); }

        for (Account account : accountList)
            if (TextUtils.equals(selectedAccountId, account.getId())) {
                AccountUtils.SELECTED_ACCOUNT = account;
            }

        // Default case

        if (AccountUtils.SELECTED_ACCOUNT == null) {
            try {
                AccountUtils.SELECTED_ACCOUNT = getAccountDao().queryBuilder().where().eq(Account.DB_FIELD_ID, DEMO_ID).query().get(0);
            } catch (SQLException e) { Log.e(LOG_TAG, e.getLocalizedMessage()); }
        }
    }

    // </editor-fold desc="Utils">

}
