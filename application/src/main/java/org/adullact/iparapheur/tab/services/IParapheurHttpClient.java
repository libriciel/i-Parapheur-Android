package org.adullact.iparapheur.tab.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.net.http.AndroidHttpClient;

import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

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

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.model.FolderDocument;
import org.adullact.iparapheur.tab.model.FolderRequestedAction;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.model.OfficeFacetChoices;
import org.adullact.iparapheur.tab.util.TrustAllSSLSocketFactory;

/**
 * Service used to access iParapheur HTTP API.
 * 
 * TODO Split in a facade using a static singleton for HTTP operations and session ticket management.
 * Response parsing should be kept bound to the context for localisation support.
 * 
 * ----
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
@ContextSingleton
public class IParapheurHttpClient
{

    private static final String LOGIN_PATH = "/parapheur/api/login";

    private static final String LOGOUT_PATH = "/parapheur/api/logout";

    private static final String OFFICES_PATH = "/parapheur/api/getBureaux";

    private static final String TYPOLOGY_PATH = "/parapheur/api/getTypologie";

    private static final String FOLDERS_PATH = "/parapheur/api/getDossiersHeaders";

    private static final String FOLDER_PATH = "/parapheur/api/getDossier";

    private static final String SIGN_PATH = "/parapheur/api/sign";

    private static final String VISA_PATH = "/parapheur/api/visa";

    private static final String REJECT_PATH = "/parapheur/api/reject";

    private final Map<String, String> accountSessionTickets = new HashMap<String, String>();

    private AndroidHttpClient httpClient;

    private final FolderFilterMapper folderFilterMapper;

    @Inject
    public IParapheurHttpClient( FolderFilterMapper folderFilterMapper )
    {
        this.folderFilterMapper = folderFilterMapper;
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

            // Prepare request body
            String requestBody = "{'username': '" + account.getLogin() + "'}";
            Log.d( IParapheurHttpClient.class, "REQUEST: " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( buildUrl( account, OFFICES_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );

            // Process response
            List<Office> result = new ArrayList<Office>();
            if ( json.has( "data" ) && json.getJSONObject( "data" ).has( "bureaux" ) ) {
                JSONArray bureaux = json.getJSONObject( "data" ).getJSONArray( "bureaux" );
                for ( int idx = 0; idx < bureaux.length(); idx++ ) {
                    JSONObject eachBureau = bureaux.getJSONObject( idx );
                    String identity = eachBureau.getString( "nodeRef" );
                    String title = eachBureau.getString( "name" );
                    String community = eachBureau.getString( "collectivite" );
                    // BEGIN TODO Remove default community name
                    if ( Strings.isEmpty( community ) ) {
                        community = "Ma collectivité";
                    }
                    // END REMOVE
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

        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Offices " + account.getTitle() + " : " + ex.getMessage(), ex );
        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Offices " + account.getTitle() + " : " + ex.getMessage(), ex );
        }
    }

    public SortedMap<String, List<String>> fetchOfficeTypology( Account account, String officeIdentity )
    {
        NullArgumentException.ensureNotEmpty( "Office Identity", officeIdentity );
        ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = "{'bureauRef': '" + officeIdentity + "'}";
            Log.d( IParapheurHttpClient.class, "REQUEST: " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( buildUrl( account, TYPOLOGY_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );

            // Process response
            SortedMap<String, List<String>> result = new TreeMap<String, List<String>>();
            if ( json.has( "data" ) && json.getJSONObject( "data" ).has( "typology" ) ) {
                JSONObject typology = json.getJSONObject( "data" ).getJSONObject( "typology" );
                Iterator<String> it = typology.keys();
                while ( it.hasNext() ) {
                    String type = it.next();
                    List<String> subtypes = new ArrayList<String>();
                    JSONArray subtypesJson = typology.optJSONArray( type );
                    if ( subtypesJson != null ) {
                        for ( int index = 0; index < subtypesJson.length(); index++ ) {
                            subtypes.add( subtypesJson.getString( index ) );
                        }
                    }
                    result.put( type, subtypes );
                }
            }
            return result;

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Office Typology " + officeIdentity + " : " + ex.getMessage(), ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Office Typology" + officeIdentity + " : " + ex.getMessage(), ex );
        }
    }

    public List<Folder> fetchFolders( Account account, String officeIdentity, OfficeFacetChoices facetSelection, int page, int pageSize )
            throws IParapheurHttpException
    {
        NullArgumentException.ensureNotEmpty( "Office Identity", officeIdentity );
        ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = "{'bureauRef': '" + officeIdentity
                                 + "', filters: " + folderFilterMapper.buildFilters( facetSelection )
                                 + ", 'page': " + page
                                 + ", 'pageSize': " + pageSize + "}";
            Log.d( IParapheurHttpClient.class, "REQUEST: " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( buildUrl( account, FOLDERS_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );

            // Process response
            List<Folder> result = new ArrayList<Folder>();
            if ( json.has( "data" ) && json.getJSONObject( "data" ).has( "dossiers" ) ) {
                JSONArray dossiers = json.getJSONObject( "data" ).getJSONArray( "dossiers" );
                for ( int idx = 0; idx < dossiers.length(); idx++ ) {
                    JSONObject eachDossier = dossiers.getJSONObject( idx );
                    Folder folder = folderFromJSON( eachDossier );
                    if ( folder != null ) {
                        result.add( folder );
                    }
                }
            }
            return result;

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Office " + officeIdentity + " : " + ex.getMessage(), ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Office " + officeIdentity + " : " + ex.getMessage(), ex );
        }
    }

    public Folder fetchFolder( Account account, String folderIdentity )
            throws IParapheurHttpException
    {
        NullArgumentException.ensureNotEmpty( "Folder Identity", folderIdentity );
        ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = "{'dossierRef': '" + folderIdentity + "'}";
            Log.d( IParapheurHttpClient.class, "REQUEST: " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( buildUrl( account, FOLDER_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = httpClient.execute( post, JSON_RESPONSE_HANDLER );

            // Process response
            JSONObject dossier = json.getJSONObject( "data" );
            return folderFromJSON( dossier );

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Folder " + folderIdentity + " : " + ex.getMessage(), ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Folder " + folderIdentity + " : " + ex.getMessage(), ex );
        }
    }

    public void sign( Account account, String pubAnnotation, String privAnnotation, String... folderIdentities )
    {
        ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = prepareSignVisaRejectRequestBody( pubAnnotation, privAnnotation, folderIdentities );
            Log.d( IParapheurHttpClient.class, "REQUEST: " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( buildUrl( account, SIGN_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            HttpResponse response = httpClient.execute( post );

            // Process response
            if ( response.getStatusLine().getStatusCode() != 200 ) {
                throw new IParapheurHttpException( "Sign " + Arrays.toString( folderIdentities )
                                                   + " HTTP/" + response.getStatusLine().getStatusCode()
                                                   + " " + response.getStatusLine().getReasonPhrase() );
            }

        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Sign " + Arrays.toString( folderIdentities ) + " : " + ex.getMessage(), ex );
        }
    }

    public void visa( Account account, String pubAnnotation, String privAnnotation, String... folderIdentities )
    {
        ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = prepareSignVisaRejectRequestBody( pubAnnotation, privAnnotation, folderIdentities );
            Log.d( IParapheurHttpClient.class, "REQUEST: " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( buildUrl( account, VISA_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            HttpResponse response = httpClient.execute( post );

            // Process response
            if ( response.getStatusLine().getStatusCode() != 200 ) {
                throw new IParapheurHttpException( "Visa " + Arrays.toString( folderIdentities )
                                                   + " HTTP/" + response.getStatusLine().getStatusCode()
                                                   + " " + response.getStatusLine().getReasonPhrase() );
            }

        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Visa " + Arrays.toString( folderIdentities ) + " : " + ex.getMessage(), ex );
        }
    }

    public void reject( Account account, String pubAnnotation, String privAnnotation, String... folderIdentities )
    {
        ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = prepareSignVisaRejectRequestBody( pubAnnotation, privAnnotation, folderIdentities );
            Log.d( IParapheurHttpClient.class, "REQUEST: " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( buildUrl( account, REJECT_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            HttpResponse response = httpClient.execute( post );

            // Process response
            if ( response.getStatusLine().getStatusCode() != 200 ) {
                throw new IParapheurHttpException( "Reject " + Arrays.toString( folderIdentities )
                                                   + " HTTP/" + response.getStatusLine().getStatusCode()
                                                   + " " + response.getStatusLine().getReasonPhrase() );
            }
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Reject " + Arrays.toString( folderIdentities ) + " : " + ex.getMessage(), ex );
        }
    }

    private String prepareSignVisaRejectRequestBody( String pubAnnotation, String privAnnotation, String... folderIdentities )
    {
        NullArgumentException.ensureNotEmpty( "Folder Identities", folderIdentities );
        try {
            JSONObject json = new JSONObject();
            JSONArray dossiers = new JSONArray();
            for ( int index = 0; index < folderIdentities.length; index++ ) {
                String folderId = folderIdentities[index];
                dossiers.put( folderId );
            }
            json.put( "dossiers", dossiers );
            json.put( "annotPub", pubAnnotation == null ? Strings.EMPTY : pubAnnotation );
            json.put( "annotPriv", privAnnotation == null ? Strings.EMPTY : privAnnotation );
            return json.toString();
        } catch ( JSONException ex ) {
            // This should not happen but we don't want to fail silently!
            throw new IParapheurTabException( "Unable to prepare request JSON body: " + ex.getMessage(), ex );
        }
    }

    private static Folder folderFromJSON( JSONObject dossier )
            throws JSONException
    {
        String identity = dossier.getString( "dossierRef" );
        String title = dossier.getString( "titre" );
        String actionDemandee = dossier.getString( "actionDemandee" );
        String type = dossier.getString( "type" );
        String subtype = dossier.getString( "sousType" );
        String creationDate = dossier.getString( "dateCreation" );
        String dueDate = dossier.optString( "dateLimite" );
        FolderRequestedAction requestedAction = null;
        if ( "VISA".equals( actionDemandee ) ) {
            requestedAction = FolderRequestedAction.VISA;
        } else if ( "SIGNATURE".equals( actionDemandee ) ) {
            requestedAction = FolderRequestedAction.SIGNATURE;
        } else {
            // WARN Unsupported FolderRequestedAction
            requestedAction = FolderRequestedAction.UNSUPPORTED;
        }
        Folder folder = new Folder( identity, title, requestedAction, type, subtype );
        if ( dossier.has( "documents" ) ) {
            JSONArray documents = dossier.getJSONArray( "documents" );
            for ( int index = 0; index < documents.length(); index++ ) {
                JSONObject doc = documents.getJSONObject( index );
                String docName = doc.getString( "name" );
                Integer docSize = doc.getInt( "size" );
                folder.addDocument( new FolderDocument( docName, docSize, "file:///android_asset/index.html" ) ); // TODO Parse document pages URLs
            }
        }
        return folder;
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
                throw new IParapheurHttpException( account.getTitle() + " : " + ex.getMessage(), ex );
            } catch ( JSONException ex ) {
                throw new IParapheurHttpException( account.getTitle() + " : " + ex.getMessage(), ex );
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
                JSONObject json = new JSONObject( data );
                Log.d( IParapheurHttpClient.class, "RESPONSE: " + json.toString() );
                return json;
            } catch ( JSONException ex ) {
                throw new IOException( "Unable to parse returned JSON", ex );
            }
        }

    };

}
