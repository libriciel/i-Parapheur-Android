package org.adullact.iparapheur.tab.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.net.http.AndroidHttpClient;

import de.akquinet.android.androlog.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codeartisans.java.toolbox.Strings;
import org.codeartisans.java.toolbox.exceptions.NullArgumentException;
import org.json.JSONException;
import org.json.JSONObject;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.util.TrustAllSSLSocketFactory;

/**
 * Singleton allowing to keep one HttpClient instance only.
 * 
 * Static fields are attached to the Class instance as a whole, which is in turn attached to the ClassLoader which
 * loaded the class. the_instance would be unloaded when the entire ClassLoader is reclaimed. I am 90% sure this happens
 * when Android destroys the app (not when it goes into the background, or pauses, but is completely shut down.)
 * 
 * It'll be retained until your entire process is destroyed. When your process is revived, your singleton will reappear!
 * 
 * It should be noted that the singleton will be recreated, but the original state of the singleton is not automaticaly
 * restored. This would have to be done manually.
 */
public class StaticHttpClient
{

    private static StaticHttpClient instance;

    /* package */ static synchronized StaticHttpClient getInstance()
    {
        if ( instance == null ) {
            instance = new StaticHttpClient();
        }
        return instance;
    }

    public static synchronized void releaseInstance()
    {
        if ( instance != null ) {
            instance.httpClient.close();
            instance.accountSessionTickets.clear();
            instance = null;
        }
    }


    /* package */ final AndroidHttpClient httpClient;

    private final Map<String, String> accountSessionTickets = new HashMap<String, String>();

    private StaticHttpClient()
    {
        httpClient = AndroidHttpClient.newInstance( "Android" );
        if ( false ) { // WARN This activates TRUST ALL SSL support
            SchemeRegistry schemeRegistry = ( ( AndroidHttpClient ) httpClient ).getConnectionManager().getSchemeRegistry();
            schemeRegistry.unregister( "https" );
            schemeRegistry.register( new Scheme( "https", TrustAllSSLSocketFactory.getSocketFactory(), 443 ) );
        }
    }

    /* package */ synchronized void ensureLoggedIn( Account account )
            throws IParapheurHttpException
    {
        NullArgumentException.ensureNotNull( "Account", account );
        if ( !accountSessionTickets.containsKey( account.getIdentity() ) ) {
            try {
                HttpPost post = new HttpPost( account.getUrl() + IParapheurHttpClient.LOGIN_PATH );
                HttpEntity data = new StringEntity( "{'username': '" + account.getLogin() + "', 'password': '" + account.getPassword() + "'}", "UTF-8" );
                post.setEntity( data );
                JSONObject json = StaticHttpClient.getInstance().httpClient.execute( post, JSON_RESPONSE_HANDLER );
                String ticket = json.getJSONObject( "data" ).getString( "ticket" );
                accountSessionTickets.put( account.getIdentity(), ticket );
            } catch ( IOException ex ) {
                throw new IParapheurHttpException( account.getTitle() + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
            } catch ( JSONException ex ) {
                throw new IParapheurHttpException( account.getTitle() + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
            }
        } else {
            Log.d( "Already logged-in" );
        }
    }

    /* package */ String buildUrl( Account account, String path )
    {
        NullArgumentException.ensureNotNull( "Account", account );
        NullArgumentException.ensureNotEmpty( "Path", path );
        return account.getUrl() + path + "?alf_ticket=" + accountSessionTickets.get( account.getIdentity() );
    }


    /* package */ static final ResponseHandler<JSONObject> JSON_RESPONSE_HANDLER = new ResponseHandler<JSONObject>()
    {

        public JSONObject handleResponse( HttpResponse response )
                throws ClientProtocolException, IOException
        {
            try {
                StatusLine statusLine = response.getStatusLine();
                HttpEntity entity = response.getEntity();
                if ( statusLine.getStatusCode() >= 300 ) {
                    EntityUtils.toByteArray( entity );
                    throw new HttpResponseException( statusLine.getStatusCode(), statusLine.getReasonPhrase() );
                }
                if ( entity == null ) {
                    throw new HttpResponseException( statusLine.getStatusCode(), "NO RESPONSE" );
                }
                String data = EntityUtils.toString( entity );
                JSONObject json = new JSONObject( data );
                Log.d( IParapheurHttpClient.class, "RESPONSE: " + json.toString() );
                return json;
            } catch ( JSONException ex ) {
                throw new IOException( "Unable to parse returned JSON", ex );
            }
        }

    };

}
