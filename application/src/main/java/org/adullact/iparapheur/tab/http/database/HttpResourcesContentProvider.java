package org.adullact.iparapheur.tab.http.database;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import roboguice.content.RoboContentProvider;

import com.google.inject.Inject;

/**
 * Android ContentProvider for data comming from iParapheur deployments.
 * 
 * Content URI are totally virtuals as iParapheur offers an HTTP API that has nothing todo with REST.
 * All request are POSTs, even the one that are safe and idempotent.
 * 
 * http://www.vogella.com/articles/AndroidSQLite/article.html
 * 
 */
public class HttpResourcesContentProvider
        extends RoboContentProvider
{

    private static final String AUTHORITY = "org.adullact.iparapheur.tab.http.contentprovider";

    private static final String BASE_PATH = "http-resources";

    private static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/" + BASE_PATH );

    private static final String OFFICE_LIST_PATH = BASE_PATH + "/*/offices";

    private static final int OFFICE_LIST_URI_TYPE = 100;

    private static final String FOLDER_LIST_PATH = BASE_PATH + "/*/folders";

    private static final int FOLDER_LIST_URI_TYPE = 200;

    private static final String FOLDER_PATH = BASE_PATH + "/*/folder/#";

    private static final int FOLDER_URI_TYPE = 300;

    private static final UriMatcher URI_MATCHER = new UriMatcher( UriMatcher.NO_MATCH );

    static {
        URI_MATCHER.addURI( AUTHORITY, OFFICE_LIST_PATH, OFFICE_LIST_URI_TYPE );
        URI_MATCHER.addURI( AUTHORITY, FOLDER_LIST_PATH, FOLDER_LIST_URI_TYPE );
        URI_MATCHER.addURI( AUTHORITY, FOLDER_PATH, FOLDER_URI_TYPE );
    }

    @Inject
    private IParapheurSQLiteOpenHelper dbOpenHelper;

    @Override
    public String getType( Uri uri )
    {
        return null;
    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder )
    {
        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns( projection );

        // Set the table
        queryBuilder.setTables( HttpResourcesTable.TABLE_HTTP_RSRC );

        int uriType = URI_MATCHER.match( uri );
        switch ( uriType ) {
            case OFFICE_LIST_URI_TYPE:
                break;
            case FOLDER_LIST_URI_TYPE:
                break;
            case FOLDER_URI_TYPE:
                // Adding the ID to the original query
                queryBuilder.appendWhere( HttpResourcesTable.COLUMN_ID + "=" + uri.getLastPathSegment() );
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI: " + uri );
        }

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query( db, projection, selection, selectionArgs, null, null, sortOrder );

        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri( getContext().getContentResolver(), uri );

        return cursor;
    }

    @Override
    public Uri insert( Uri uri, ContentValues cv )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public int delete( Uri uri, String string, String[] strings )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public int update( Uri uri, ContentValues cv, String string, String[] strings )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    private void checkColumns( String[] projection )
    {
        if ( projection != null ) {
            Set<String> requestedColumns = new HashSet<String>( Arrays.asList( projection ) );
            Set<String> availableColumns = new HashSet<String>( Arrays.asList( HttpResourcesTable.AVAILABLE_COLUMNS ) );
            // Check if all columns which are requested are available
            if ( !availableColumns.containsAll( requestedColumns ) ) {
                throw new IllegalArgumentException( "Unknown columns in projection" );
            }
        }
    }

}
