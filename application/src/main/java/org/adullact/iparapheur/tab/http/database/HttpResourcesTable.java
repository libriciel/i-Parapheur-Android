package org.adullact.iparapheur.tab.http.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import de.akquinet.android.androlog.Log;

public class HttpResourcesTable
{
    // Database table

    /* package */ static final String TABLE_HTTP_RSRC = "http-resources";

    public static final String COLUMN_ID = BaseColumns._ID;

    public static final String COLUMN_ACCOUNT = "account"; // ???? url+login ?

    public static final String COLUMN_RESOURCE = "resource"; // TEXT

    public static final String COLUMN_STATE = "state"; // INTEGER

    public static final String COLUMN_DATA = "data"; // BLOB

    public static final String COLUMN_PAYLOAD = "payload"; // BLOB

    public static final String[] AVAILABLE_COLUMNS = new String[]{ COLUMN_ID,
                                                                   COLUMN_ACCOUNT,
                                                                   COLUMN_RESOURCE,
                                                                   COLUMN_STATE,
                                                                   COLUMN_DATA,
                                                                   COLUMN_PAYLOAD };

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_HTTP_RSRC + " ("
                                                  + COLUMN_ID + " TEXT, "
                                                  + COLUMN_ACCOUNT + " TEXT, "
                                                  + COLUMN_RESOURCE + " TEXT, "
                                                  + COLUMN_STATE + " INTEGER, "
                                                  + COLUMN_DATA + " BLOB, "
                                                  + COLUMN_PAYLOAD + " BLOB);";

    public static void onCreate( SQLiteDatabase db )
    {
        db.execSQL( DATABASE_CREATE );
    }

    public static void onUpgrade( SQLiteDatabase db, int from, int to )
    {
        Log.w( "Upgrading database from version " + from + " to " + to + ", which will destroy all old cached http resources." );
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_HTTP_RSRC );
        onCreate( db );
    }

}
