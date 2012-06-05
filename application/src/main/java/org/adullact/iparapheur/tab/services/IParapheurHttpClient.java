package org.adullact.iparapheur.tab.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.http.AndroidHttpClient;

import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.model.FolderRequestedAction;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.util.TrustAllSSLSocketFactory;

@ContextSingleton
public class IParapheurHttpClient
{

    private static final String LOGIN_PATH = "/parapheur/api/login";

    private static final String LOGOUT_PATH = "/parapheur/api/logout";

    private static final String OFFICES_PATH = "/parapheur/api/getBureaux";

    private static final String FOLDERS_PATH = "/parapheur/api/getDossiersHeaders";

    private static final String FOLDER_PATH = "/parapheur/api/getDossier";

    private final Map<String, String> accountSessionTickets = new HashMap<String, String>();

    private AndroidHttpClient httpClient;

    public IParapheurHttpClient()
    {
        setupHttpClient();
    }

    public void onActivityResume( @Observes OnResumeEvent event )
    {
        if ( httpClient == null ) {
            setupHttpClient();
        }
    }

    public void onActivityPause( @Observes OnPauseEvent event )
    {
        httpClient.close();
        httpClient = null;
        accountSessionTickets.clear();
    }

    private void setupHttpClient()
    {
        httpClient = AndroidHttpClient.newInstance( "Android" );
        SchemeRegistry schemeRegistry = ( ( AndroidHttpClient ) httpClient ).getConnectionManager().getSchemeRegistry();
        schemeRegistry.unregister( "https" );
        schemeRegistry.register( new Scheme( "https", TrustAllSSLSocketFactory.getSocketFactory(), 443 ) );
    }

    public List<Office> fetchOffices( Account account )
            throws IParapheurHttpException
    {
        ensureLoggedIn( account );
        try {

            HttpPost post = new HttpPost( buildUrl( account, OFFICES_PATH ) );
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
                        community = "Ma collectivité";
                    }
                    // END TODO REMOVE
                    String description = eachBureau.getString( "description" );
                    Integer todoFolderCount = eachBureau.getInt( "a_traiter" );
                    Integer lateFolderCount = eachBureau.getInt( "en_retard" );

                    Office office = new Office( identity, title, community, account.getIdentity() );
                    office.setDescription( description );
                    office.setTodoFolderCount( todoFolderCount );
                    office.setLateFolderCount( lateFolderCount );
                    result.add( office );
                }
            }
            return result;

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Unable to load Offices from account '" + account.getTitle() + "'", ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Unable to load Offices from account '" + account.getTitle() + "'", ex );
        }
    }

    public List<Folder> fetchFolders( Account account, String officeIdentity, int page, int pageSize )
            throws IParapheurHttpException
    {
        ensureLoggedIn( account );
        try {

            HttpPost post = new HttpPost( buildUrl( account, FOLDERS_PATH ) );
            HttpEntity data = new StringEntity( "{'bureauRef': '" + officeIdentity + "', 'page': " + page + ", 'pageSize': " + pageSize + "}" );
            post.setEntity( data );
            JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );

            List<Folder> result = new ArrayList<Folder>();
            if ( json.has( "data" ) && json.getJSONObject( "data" ).has( "dossiers" ) ) {
                JSONArray dossiers = json.getJSONObject( "data" ).getJSONArray( "dossiers" );
                for ( int idx = 0; idx < dossiers.length(); idx++ ) {
                    JSONObject eachDossier = dossiers.getJSONObject( idx );
                    Folder folder = parseFolder( eachDossier );
                    if ( folder != null ) {
                        result.add( folder );
                    }
                }
            }
            return result;

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Unable to load Folders", ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Unable to load Folders", ex );
        }
    }

    public Folder fetchFolder( Account account, String folderIdentity )
            throws IParapheurHttpException
    {
        ensureLoggedIn( account );
        try {
            HttpPost post = new HttpPost( buildUrl( account, FOLDER_PATH ) );
            HttpEntity data = new StringEntity( "{'dossierRef': '" + folderIdentity + "'}" );
            post.setEntity( data );
            JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );

            if ( !json.has( "data" ) ) {
                return null;
            }

            JSONObject dossier = json.getJSONObject( "data" );
            return parseFolder( dossier );

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Unable to load Folder", ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Unable to load Folder", ex );
        }
    }

    private Folder parseFolder( JSONObject dossier )
            throws JSONException
    {
        String identity = dossier.getString( "dossierRef" );
        String title = dossier.getString( "titre" );
        String actionDemandee = dossier.getString( "actionDemandee" );
        String type = dossier.getString( "type" );
        String subtype = dossier.getString( "sousType" );
        // String dueDate = dossier.getString( "dateLimite" );
        FolderRequestedAction requestedAction = null;
        if ( "VISA".equals( actionDemandee ) ) {
            requestedAction = FolderRequestedAction.VISA;
        } else if ( "SIGNATURE".equals( actionDemandee ) ) {
            requestedAction = FolderRequestedAction.SIGNATURE;
        } else {
            Log.w( "Unsupported FolderRequestedAction(" + actionDemandee + "). This Folder (" + identity + ") will have no requested action." );
        }
        return new Folder( identity, title, requestedAction, type, subtype );
    }

    private String buildUrl( Account account, String path )
    {
        NullArgumentException.ensureNotNull( "Account", account );
        NullArgumentException.ensureNotEmpty( "Path", path );
        return account.getUrl() + path + "?alf_ticket=" + accountSessionTickets.get( account.getIdentity() );
    }

    private synchronized void ensureLoggedIn( Account account )
            throws IParapheurHttpException
    {
        NullArgumentException.ensureNotNull( "Account", account );
        if ( !accountSessionTickets.containsKey( account.getIdentity() ) ) {
            try {
                HttpPost post = new HttpPost( account.getUrl() + LOGIN_PATH );
                HttpEntity data = new StringEntity( "{'username': '" + account.getLogin() + "', 'password': '" + account.getPassword() + "'}", "UTF-8" );
                post.setEntity( data );
                JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );
                String ticket = json.getJSONObject( "data" ).getString( "ticket" );
                accountSessionTickets.put( account.getIdentity(), ticket );
            } catch ( IOException ex ) {
                throw new IParapheurHttpException( "Unable to login into account '" + account.getTitle() + "'", ex );
            } catch ( JSONException ex ) {
                throw new IParapheurHttpException( "Unable to login into account " + account.getTitle() + "'", ex );
            }
        } else {
            Log.d( "Already logged-in" );
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

}
