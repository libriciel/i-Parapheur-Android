package org.adullact.iparapheur.tab.http.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

@ContextSingleton
public class IParapheurSQLiteOpenHelper
        extends SQLiteOpenHelper
{

    private static final String DATABASE_NAME = "iparapheur-tab.db";

    private static final int DATABASE_VERSION = 1;

    @Inject
    public IParapheurSQLiteOpenHelper( Context context )
    {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase db )
    {
        HttpResourcesTable.onCreate( db );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int from, int to )
    {
        HttpResourcesTable.onUpgrade( db, from, to );
    }

}
