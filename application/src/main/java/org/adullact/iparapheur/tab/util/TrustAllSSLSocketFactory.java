package org.adullact.iparapheur.tab.util;

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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.akquinet.android.androlog.Log;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.adullact.iparapheur.tab.services.IParapheurHttpClient;

@Deprecated
public class TrustAllSSLSocketFactory
        implements LayeredSocketFactory
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
