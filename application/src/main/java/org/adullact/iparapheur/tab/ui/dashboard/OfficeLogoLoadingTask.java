package org.adullact.iparapheur.tab.ui.dashboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import de.akquinet.android.androlog.Log;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import roboguice.util.Strings;

/* package */ class OfficeLogoLoadingTask
        extends AsyncTask<String, Void, Bitmap>
{

    private static final int LOGO_SIZE = 192;

    private static final BitmapCache CACHE = new BitmapCache();

    @Override
    protected final Bitmap doInBackground( String... params )
    {
        String url = params[0];
        if ( Strings.isEmpty( url ) ) {
            return null;
        }
        try {
            synchronized( CACHE ) {
                if ( CACHE.hasBitmap( url ) ) {
                    return CACHE.get( url );
                }
                Bitmap bitmap = BitmapFactory.decodeStream( ( InputStream ) new URL( url ).getContent() );
                Bitmap scaledBitmap = Bitmap.createScaledBitmap( bitmap, LOGO_SIZE, LOGO_SIZE, true );
                CACHE.put( url, scaledBitmap );
                return scaledBitmap;
            }
        } catch ( IOException ex ) {
            Log.w( this, "Unabe to load Office logo from: " + url + "(" + ex.getMessage() + ")" );
            return null;
        }
    }

    private static class BitmapCache
    {

        private final Map<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();

        private Bitmap put( String key, Bitmap value )
        {
            SoftReference<Bitmap> old = cache.put( key, new SoftReference<Bitmap>( value ) );
            if ( old == null ) {
                return null;
            }
            return old.get();
        }

        private Bitmap get( String key )
        {
            SoftReference<Bitmap> val = cache.get( key );
            if ( val == null ) {
                return null;
            }
            Bitmap ret = val.get();
            if ( ret == null ) {
                cache.remove( key );
            }
            return ret;
        }

        private boolean hasBitmap( String url )
        {
            return cache.containsKey( url );
        }

    }

}
