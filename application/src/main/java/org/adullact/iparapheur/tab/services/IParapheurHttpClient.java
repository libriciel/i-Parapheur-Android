package org.adullact.iparapheur.tab.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.net.http.AndroidHttpClient;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codeartisans.java.toolbox.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Community;
import org.adullact.iparapheur.tab.model.Office;

@ContextSingleton
public class IParapheurHttpClient
{

    private static final String LOGIN_PATH = "/parapheur/api/login";

    private static final String LOGOUT_PATH = "/parapheur/api/logout";

    private static final String OFFICES_PATH = "/parapheur/api/getBureaux";

    private static final String FOLDERS_PATH = "/parapheur/api/getDossiersHeaders";

    private static final String FOLDER_PATH = "/parapheur/api/getDossier";

    @Inject
    private AccountsRepository accountsRepository;

    private HttpClient httpClient = AndroidHttpClient.newInstance( "Android" );

    private final Map<String, String> accountSessionTickets = new HashMap<String, String>();

    public IParapheurHttpClient()
    {
        SchemeRegistry schemeRegistry = ( ( AndroidHttpClient ) httpClient ).getConnectionManager().getSchemeRegistry();
        schemeRegistry.unregister( "https" );
        schemeRegistry.register( new Scheme( "https", TrustAllSSLSocketFactory.getSocketFactory(), 443 ) );
    }

    public Map<Community, List<Office>> getOffices()
            throws IParapheurHttpException
    {
        final Map<Community, List<Office>> result = new HashMap<Community, List<Office>>();

        for ( Account eachAccount : accountsRepository.all() ) {
            try {

                List<Office> offices = getOffices( eachAccount );

                // Sort by community
                for ( Office eachOffice : offices ) {
                    if ( result.get( eachOffice.getCommunity() ) == null ) {
                        result.put( eachOffice.getCommunity(), new ArrayList<Office>() );
                    }
                    result.get( eachOffice.getCommunity() ).add( eachOffice );
                }

            } catch ( IParapheurHttpException ex ) {
                // Ignored for now
                ex.printStackTrace();
            }
        }

        return result;
    }

    private List<Office> getOffices( Account account )
            throws IParapheurHttpException
    {
        if ( account.getLogin().equals( "eperalta" ) ) {
            ensureLoggedIn( account );
            try {
                HttpPost post = new HttpPost( account.getUrl() + OFFICES_PATH + "?alf_ticket=" + accountSessionTickets.get( account.getIdentity() ) );
                HttpEntity data = new StringEntity( "{'username': '" + account.getLogin() + "'}", "UTF-8" );
                post.setEntity( data );
                JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );

                List<Office> result = new ArrayList<Office>();
                if ( json.has( "data" ) && json.getJSONObject( "data" ).has( "bureaux" ) ) {
                    JSONArray bureaux = json.getJSONObject( "data" ).getJSONArray( "bureaux" );
                    for ( int idx = 0; idx < bureaux.length(); idx++ ) {
                        JSONObject eachBureau = bureaux.getJSONObject( idx );
                        String identity = eachBureau.getString( "nodeRef" );
                        String title = eachBureau.getString( "name" );
                        String community = eachBureau.getString( "collectivite" );
                        // BEGIN TODO REMOVE
                        if ( Strings.isEmpty( community ) ) {
                            community = "Ma collectivitŽ";
                        }
                        // END TODO REMOVE
                        String description = eachBureau.getString( "description" );
                        Integer todoFolderCount = eachBureau.getInt( "a_traiter" );
                        Integer lateFolderCount = eachBureau.getInt( "en_retard" );

                        Office office = new Office( identity, title, community );
                        office.setDescription( description );
                        office.setTodoFolderCount( todoFolderCount );
                        office.setLateFolderCount( lateFolderCount );
                        result.add( office );
                    }
                }
                return result;

            } catch ( JSONException ex ) {
                throw new IParapheurHttpException( "Unable to load Offices", ex );
            } catch ( IOException ex ) {
                throw new IParapheurHttpException( "Unable to load Offices", ex );
            }
        } else {
            return generateFakeOffices( account );
        }
    }

    private synchronized void ensureLoggedIn( Account account )
            throws IParapheurHttpException
    {
        if ( !accountSessionTickets.containsKey( account.getIdentity() ) ) {
            synchronized ( accountSessionTickets ) {
                try {
                    HttpPost post = new HttpPost( account.getUrl() + LOGIN_PATH );
                    HttpEntity data = new StringEntity( "{'username': '" + account.getLogin() + "', 'password': '" + account.getPassword() + "'}", "UTF-8" );
                    post.setEntity( data );
                    JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );
                    String ticket = json.getJSONObject( "data" ).getString( "ticket" );
                    accountSessionTickets.put( account.getIdentity(), ticket );
                } catch ( IOException ex ) {
                    throw new IParapheurHttpException( "Unable to login", ex );
                } catch ( JSONException ex ) {
                    throw new IParapheurHttpException( "Unable to login", ex );
                }
            }
        }
    }

    private static final ResponseHandler<JSONObject> JSON_RESPONSE_HANDLER = new ResponseHandler<JSONObject>()
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
                return new JSONObject( data );
            } catch ( JSONException ex ) {
                throw new IOException( "Unable to parse returned JSON", ex );
            }
        }

    };

    private List<Office> generateFakeOffices( Account account )
    {
        // TODO REMOVE BEGIN
        try {
            Thread.sleep( 500 );
        } catch ( InterruptedException ex ) {
            // Ignored
        }
        List<Office> offices = new ArrayList<Office>();
        offices.add( new Office( account.getTitle() + "-A1", "Bureau A/1", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-A2", "Bureau A/2", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-A3", "Bureau A/3", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-B1", "Bureau B/1", account.getTitle() + " B" ) );
        offices.add( new Office( account.getTitle() + "-B2", "Bureau B/2", account.getTitle() + " B" ) );
        // TODO REMOVE ENDS

        return offices;
    }

    private static final class TrustAllSSLSocketFactory
            implements
            LayeredSocketFactory
    {

        private static final TrustAllSSLSocketFactory DEFAULT_FACTORY = new TrustAllSSLSocketFactory();

        public static TrustAllSSLSocketFactory getSocketFactory()
        {
            return DEFAULT_FACTORY;
        }

        private SSLContext sslcontext;

        private javax.net.ssl.SSLSocketFactory socketfactory;

        private TrustAllSSLSocketFactory()
        {
            super();
            TrustManager[] tm = new TrustManager[]{ new X509TrustManager()
        {

            @Override
            public void checkClientTrusted( X509Certificate[] chain,
                                            String authType )
                    throws CertificateException
            {
                // do nothing
            }

            @Override
            public void checkServerTrusted( X509Certificate[] chain,
                                            String authType )
                    throws CertificateException
            {
                // do nothing
            }

            @Override
            public X509Certificate[] getAcceptedIssuers()
            {
                return new X509Certificate[ 0 ];
            }

        } };
            try {
                this.sslcontext = SSLContext.getInstance( SSLSocketFactory.TLS );
                this.sslcontext.init( null, tm, new SecureRandom() );
                this.socketfactory = this.sslcontext.getSocketFactory();
            } catch ( NoSuchAlgorithmException e ) {
                Log.e( IParapheurHttpClient.class, "Failed to instantiate TrustAllSSLSocketFactory!", e );
            } catch ( KeyManagementException e ) {
                Log.e( IParapheurHttpClient.class, "Failed to instantiate TrustAllSSLSocketFactory!", e );
            }
        }

        @Override
        public Socket createSocket( Socket socket, String host, int port,
                                    boolean autoClose )
                throws IOException, UnknownHostException
        {
            SSLSocket sslSocket = ( SSLSocket ) this.socketfactory.createSocket(
                    socket, host, port, autoClose );
            return sslSocket;
        }

        @Override
        public Socket connectSocket( Socket sock, String host, int port,
                                     InetAddress localAddress, int localPort, HttpParams params )
                throws IOException, UnknownHostException, ConnectTimeoutException
        {
            if ( host == null ) {
                throw new IllegalArgumentException(
                        "Target host may not be null." );
            }
            if ( params == null ) {
                throw new IllegalArgumentException(
                        "Parameters may not be null." );
            }

            SSLSocket sslsock = ( SSLSocket ) ( ( sock != null ) ? sock
                                                : createSocket() );

            if ( ( localAddress != null ) || ( localPort > 0 ) ) {

                // we need to bind explicitly
                if ( localPort < 0 ) {
                    localPort = 0; // indicates "any"
                }

                InetSocketAddress isa = new InetSocketAddress( localAddress,
                                                               localPort );
                sslsock.bind( isa );
            }

            int connTimeout = HttpConnectionParams.getConnectionTimeout( params );
            int soTimeout = HttpConnectionParams.getSoTimeout( params );

            InetSocketAddress remoteAddress;
            remoteAddress = new InetSocketAddress( host, port );

            sslsock.connect( remoteAddress, connTimeout );

            sslsock.setSoTimeout( soTimeout );

            return sslsock;
        }

        @Override
        public Socket createSocket()
                throws IOException
        {
            // the cast makes sure that the factory is working as expected
            return ( SSLSocket ) this.socketfactory.createSocket();
        }

        @Override
        public boolean isSecure( Socket sock )
                throws IllegalArgumentException
        {
            return true;
        }

    }

}
