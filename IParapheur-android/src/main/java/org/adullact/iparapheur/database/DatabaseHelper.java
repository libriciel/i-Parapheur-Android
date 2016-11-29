package org.adullact.iparapheur.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;

import java.sql.SQLException;


/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "iParpaheur.db";
	private static final int DATABASE_VERSION = 3;

	private Dao<Bureau, Integer> bureauDao;
	private Dao<Dossier, Integer> dossierDao;
	private Dao<Document, Integer> documentDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// <editor-fold desc="OrmLiteSqliteOpenHelper">

	@Override public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {

		try {
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

			TableUtils.dropTable(connectionSource, Bureau.class, true);
			TableUtils.dropTable(connectionSource, Dossier.class, true);
			TableUtils.dropTable(connectionSource, Document.class, true);

			onCreate(sqliteDatabase, connectionSource);
		}
		catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
		}
	}

	// </editor-fold desc="OrmLiteSqliteOpenHelper">

	// Create the getDao methods of all database tables to access those from android code.
	// Insert, delete, read, update everything will be happened through DAOs

	public @NonNull Dao<Bureau, Integer> getBureauDao() throws SQLException {

		if (bureauDao == null)
			bureauDao = getDao(Bureau.class);

		return bureauDao;
	}

	public @NonNull Dao<Dossier, Integer> getDossierDao() throws SQLException {

		if (dossierDao == null)
			dossierDao = getDao(Dossier.class);

		return dossierDao;
	}

	public @NonNull Dao<Document, Integer> getDocumentDao() throws SQLException {

		if (documentDao == null)
			documentDao = getDao(Document.class);

		return documentDao;
	}

}
