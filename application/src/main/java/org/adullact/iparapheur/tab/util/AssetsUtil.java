package org.adullact.iparapheur.tab.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.res.AssetManager;

import org.adullact.iparapheur.tab.IParapheurTabException;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

public final class AssetsUtil
{

    public static String loadAssetAsString( AssetManager assetManager, String assetPath )
    {
        InputStream input = null;
        try {
            input = assetManager.open( assetPath );
            return CharStreams.toString( new InputStreamReader( input, Charsets.UTF_8 ) );
        } catch ( IOException ex ) {
            throw new IParapheurTabException( "Unable to load html PDF viewer", ex );
        } finally {
            Closeables.closeQuietly( input );
        }
    }

    private AssetsUtil()
    {
    }

}
