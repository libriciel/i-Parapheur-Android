package org.adullact.iparapheur.tab.services;

import com.google.inject.Inject;
import de.akquinet.android.androlog.Log;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.model.AbstractFolderFile.FolderFilePageImage;
import org.adullact.iparapheur.tab.model.*;
import org.adullact.iparapheur.tab.model.Progression.Step;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codeartisans.java.toolbox.Strings;
import org.codeartisans.java.toolbox.exceptions.NullArgumentException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.inject.ContextSingleton;

/**
 * Service used to access iParapheur HTTP API.
 */
@ContextSingleton
public class IParapheurHttpClient
{

    /* package */ static final String LOGIN_PATH = "/parapheur/api/login";

    private static final String LOGOUT_PATH = "/parapheur/api/logout";

    private static final String OFFICES_PATH = "/parapheur/api/getBureaux";

    private static final String TYPOLOGY_PATH = "/parapheur/api/getTypologie";

    private static final String FOLDERS_PATH = "/parapheur/api/getDossiersHeaders";

    private static final String FOLDER_PATH = "/parapheur/api/getDossier";

    private static final String PROGRESSION_PATH = "/parapheur/api/getCircuit";

    private static final String SIGN_PATH = "/parapheur/api/sign";

    private static final String VISA_PATH = "/parapheur/api/visa";

    private static final String REJECT_PATH = "/parapheur/api/reject";

    private final FolderFilterMapper folderFilterMapper;

    @Inject
    public IParapheurHttpClient( FolderFilterMapper folderFilterMapper )
    {
        this.folderFilterMapper = folderFilterMapper;
    }

    public List<Office> fetchOffices( Account account )
            throws IParapheurHttpException
    {
        StaticHttpClient staticHttpClient = StaticHttpClient.getInstance();
        staticHttpClient.ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = "{'username': '" + account.getLogin() + "'}";
            Log.d( IParapheurHttpClient.class, "REQUEST on " + OFFICES_PATH + ": " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( staticHttpClient.buildUrl( account, OFFICES_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = staticHttpClient.httpClient.execute( post, StaticHttpClient.JSON_RESPONSE_HANDLER );

            // Process response
            List<Office> result = new ArrayList<Office>();
            // stv: API change from oct.2012
            //if ( json.has( "data" ) && json.getJSONObject( "data" ).has( "bureaux" ) ) {
            if ( json.has( "bureaux" ) ) {
                JSONArray bureaux = json.getJSONArray( "bureaux" );
                for ( int idx = 0; idx < bureaux.length(); idx++ ) {
                    JSONObject eachBureau = bureaux.getJSONObject( idx );
                    String identity = eachBureau.getString( "nodeRef" );
                    String title = eachBureau.getString( "name" );
                    String community = eachBureau.optString( "collectivite" );
                    if ( Strings.isEmpty( community ) ) {
                        community = "Ma collectivité";
                    }
                    String description = eachBureau.getString( "description" );
                    String image = eachBureau.optString( "image" );
                    Integer todoFolderCount = eachBureau.getInt( "a_traiter" );
                    Integer lateFolderCount = eachBureau.getInt( "en_retard" );
                    // // new: a_archiver et retournes
                    //Integer doneFolderCount = eachBureau.getInt( "a_archiver" );
                    //Integer failFolderCount = eachBureau.getInt( "retournes" );

                    Office office = new Office( identity, title, community, account.getIdentity() );
                    office.setDescription( description );
                    if ( !Strings.isEmpty( image ) ) {
                        office.setLogoUrl( image );
                    }
                    office.setTodoFolderCount( todoFolderCount );
                    office.setLateFolderCount( lateFolderCount );
                    result.add( office );
                }
            }
            return result;

        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Offices " + account.getTitle() + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Offices " + account.getTitle() + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        }
    }

    public SortedMap<String, List<String>> fetchOfficeTypology( Account account, String officeIdentity )
    {
        NullArgumentException.ensureNotEmpty( "Office Identity", officeIdentity );
        StaticHttpClient staticHttpClient = StaticHttpClient.getInstance();
        staticHttpClient.ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = "{'bureauRef': '" + officeIdentity + "'}";
            Log.d( IParapheurHttpClient.class, "REQUEST on " + TYPOLOGY_PATH + ": " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( staticHttpClient.buildUrl( account, TYPOLOGY_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = staticHttpClient.httpClient.execute( post, StaticHttpClient.JSON_RESPONSE_HANDLER );

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
            throw new IParapheurHttpException( "Office Typology " + officeIdentity + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Office Typology" + officeIdentity + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        }
    }

    public List<Folder> fetchFolders( Account account, String officeIdentity, OfficeFacetChoices facetSelection, int page, int pageSize )
            throws IParapheurHttpException
    {
        NullArgumentException.ensureNotEmpty( "Office Identity", officeIdentity );
        StaticHttpClient staticHttpClient = StaticHttpClient.getInstance();
        staticHttpClient.ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = "{'bureauRef': '" + officeIdentity
                                 + "', 'filters': " + folderFilterMapper.buildFilters( facetSelection )
                                 + ", 'page': " + page
                                 + ", 'pageSize': " + pageSize + "}";
            Log.d( IParapheurHttpClient.class, "REQUEST on " + FOLDERS_PATH + ": " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( staticHttpClient.buildUrl( account, FOLDERS_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = staticHttpClient.httpClient.execute( post, StaticHttpClient.JSON_RESPONSE_HANDLER );

            // Process response
            List<Folder> result = new ArrayList<Folder>();
            if ( json.has( "data" ) && json.getJSONObject( "data" ).has( "dossiers" ) ) {
                JSONArray dossiers = json.getJSONObject( "data" ).getJSONArray( "dossiers" );
                for ( int idx = 0; idx < dossiers.length(); idx++ ) {
                    JSONObject eachDossier = dossiers.getJSONObject( idx );
                    Folder folder = folderFromJSON( account, eachDossier );
                    if ( folder != null ) {
                        result.add( folder );
                    }
                }
            }
            return result;

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Office " + officeIdentity + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Office " + officeIdentity + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        }
    }

    public Folder fetchFolder( Account account, String folderIdentity )
            throws IParapheurHttpException
    {
        NullArgumentException.ensureNotEmpty( "Folder Identity", folderIdentity );
        StaticHttpClient staticHttpClient = StaticHttpClient.getInstance();
        staticHttpClient.ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = "{'dossierRef': '" + folderIdentity + "'}";
            Log.d( IParapheurHttpClient.class, "REQUEST on " + FOLDER_PATH + ": " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( staticHttpClient.buildUrl( account, FOLDER_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = staticHttpClient.httpClient.execute( post, StaticHttpClient.JSON_RESPONSE_HANDLER );

            // Process response
            JSONObject dossier = json.getJSONObject( "data" );
            return folderFromJSON( account, dossier );

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Folder " + folderIdentity + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Folder " + folderIdentity + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        }
    }

    public Progression fetchFolderProgression( Account account, String folderIdentity )
    {
        NullArgumentException.ensureNotEmpty( "Folder Identity", folderIdentity );
        StaticHttpClient staticHttpClient = StaticHttpClient.getInstance();
        staticHttpClient.ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = "{'dossierRef': '" + folderIdentity + "'}";
            Log.d( IParapheurHttpClient.class, "REQUEST on " + PROGRESSION_PATH + ": " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( staticHttpClient.buildUrl( account, PROGRESSION_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            JSONObject json = staticHttpClient.httpClient.execute( post, StaticHttpClient.JSON_RESPONSE_HANDLER );

            // Process response
            JSONObject jsonData = json.getJSONObject( "data" );
            String privAnnotation = jsonData.optString( "annotPriv" );
            Progression progression = new Progression( folderIdentity, privAnnotation );
            JSONArray circuit = jsonData.optJSONArray( "circuit" );
            if ( circuit != null ) {
                for ( int idx = 0; idx < circuit.length(); idx++ ) {
                    JSONObject step = circuit.getJSONObject( idx );

                    // Json data
                    String dateValidation = step.getString( "dateValidation" );
                    String parapheurName = step.getString( "parapheurName" );
                    String signataire = step.optString( "signataire" );
                    boolean approved = step.getBoolean( "approved" );
                    String annotPub = step.optString( "annotPub" );
                    String actionDemandee = step.getString( "actionDemandee" );

                    // Model data
                    Date validationDate = parseISO8601Date( dateValidation );
                    FolderRequestedAction requestedAction;
                    if ( "VISA".equals( actionDemandee ) ) {
                        requestedAction = FolderRequestedAction.VISA;
                    } else if ( "SIGNATURE".equals( actionDemandee ) ) {
                        requestedAction = FolderRequestedAction.SIGNATURE;
                    } else if ( "TDT".equals( actionDemandee ) ) {
                        requestedAction = FolderRequestedAction.TDT;
                    } else if ( "ARCHIVAGE".equals( actionDemandee ) ) {
                        requestedAction = FolderRequestedAction.ARCHIVAGE;
                    } else if ( "MAILSEC".equals( actionDemandee ) ) {
                        requestedAction = FolderRequestedAction.MAILSEC;
                    } else {
                        // WARN Unsupported FolderRequestedAction
                        requestedAction = FolderRequestedAction.UNSUPPORTED;
                    }

                    progression.add( new Step( validationDate, approved, parapheurName, signataire, requestedAction, annotPub ) );
                }
            }
            return progression;

        } catch ( JSONException ex ) {
            throw new IParapheurHttpException( "Folder " + folderIdentity + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Folder " + folderIdentity + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        }
    }

    public void sign( Account account, String pubAnnotation, String privAnnotation, String... folderIdentities )
    {
        StaticHttpClient staticHttpClient = StaticHttpClient.getInstance();
        staticHttpClient.ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = prepareSignVisaRejectRequestBody( pubAnnotation, privAnnotation, folderIdentities );
            Log.d( IParapheurHttpClient.class, "REQUEST on " + SIGN_PATH + ": " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( staticHttpClient.buildUrl( account, SIGN_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            HttpResponse response = staticHttpClient.httpClient.execute( post );

            // Process response
            if ( response.getStatusLine().getStatusCode() != 200 ) {
                throw new IParapheurHttpException( "Sign " + Arrays.toString( folderIdentities )
                                                   + " HTTP/" + response.getStatusLine().getStatusCode()
                                                   + " " + response.getStatusLine().getReasonPhrase() );
            }

        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Sign " + Arrays.toString( folderIdentities ) + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        }
    }

    public void visa( Account account, String pubAnnotation, String privAnnotation, String... folderIdentities )
    {
        StaticHttpClient staticHttpClient = StaticHttpClient.getInstance();
        staticHttpClient.ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = prepareSignVisaRejectRequestBody( pubAnnotation, privAnnotation, folderIdentities );
            Log.d( IParapheurHttpClient.class, "REQUEST on " + VISA_PATH + ": " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( staticHttpClient.buildUrl( account, VISA_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            HttpResponse response = staticHttpClient.httpClient.execute( post );

            // Process response
            if ( response.getStatusLine().getStatusCode() != 200 ) {
                throw new IParapheurHttpException( "Visa " + Arrays.toString( folderIdentities )
                                                   + " HTTP/" + response.getStatusLine().getStatusCode()
                                                   + " " + response.getStatusLine().getReasonPhrase() );
            }

        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Visa " + Arrays.toString( folderIdentities ) + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        }
    }

    public void reject( Account account, String pubAnnotation, String privAnnotation, String... folderIdentities )
    {
        StaticHttpClient staticHttpClient = StaticHttpClient.getInstance();
        staticHttpClient.ensureLoggedIn( account );
        try {

            // Prepare request body
            String requestBody = prepareSignVisaRejectRequestBody( pubAnnotation, privAnnotation, folderIdentities );
            Log.d( IParapheurHttpClient.class, "REQUEST on " + REJECT_PATH + ": " + requestBody );

            // Execute HTTP request
            HttpPost post = new HttpPost( staticHttpClient.buildUrl( account, REJECT_PATH ) );
            HttpEntity data = new StringEntity( requestBody, "UTF-8" );
            post.setEntity( data );
            HttpResponse response = staticHttpClient.httpClient.execute( post );

            // Process response
            if ( response.getStatusLine().getStatusCode() != 200 ) {
                throw new IParapheurHttpException( "Reject " + Arrays.toString( folderIdentities )
                                                   + " HTTP/" + response.getStatusLine().getStatusCode()
                                                   + " " + response.getStatusLine().getReasonPhrase() );
            }
        } catch ( IOException ex ) {
            throw new IParapheurHttpException( "Reject " + Arrays.toString( folderIdentities ) + " : " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
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
            throw new IParapheurTabException( "Unable to prepare request JSON body: " + ( Strings.isEmpty( ex.getMessage() ) ? ex.getClass().getSimpleName() : ex.getMessage() ), ex );
        }
    }

    private static Folder folderFromJSON( Account account, JSONObject dossier )
            throws JSONException
    {
        String identity = dossier.getString( "dossierRef" );
        String title = dossier.getString( "titre" );
        String actionDemandee = dossier.getString( "actionDemandee" );
        String type = dossier.getString( "type" );
        String subtype = dossier.getString( "sousType" );
        String creationDate = dossier.getString( "dateCreation" );
        String dueDate = dossier.optString( "dateLimite" );
        if ( Strings.isEmpty( dueDate ) || "null".equals( dueDate ) ) {
            dueDate = null;
        }
        FolderRequestedAction requestedAction;
        if ( "VISA".equals( actionDemandee ) ) {
            requestedAction = FolderRequestedAction.VISA;
        } else if ( "SIGNATURE".equals( actionDemandee ) ) {
            requestedAction = FolderRequestedAction.SIGNATURE;
        } else {
            // WARN Unsupported FolderRequestedAction
            
            requestedAction = FolderRequestedAction.UNSUPPORTED;
        }
        Folder folder = new Folder( identity, title, requestedAction, type, subtype, parseISO8601Date( creationDate ), parseISO8601Date( dueDate ) );
        if ( dossier.has( "documents" ) ) {
            JSONArray documents = dossier.getJSONArray( "documents" );
            for ( int index = 0; index < documents.length(); index++ ) {
                JSONObject doc = documents.getJSONObject( index );
                String docName = doc.getString( "name" );
                Integer docSize = doc.getInt( "size" );
                /**
                 * FIXME plus tard: pour le moment, il n'y a QU'UN SEUL DOCUMENT, les autres pieces sont necessairement des ANNEXES !
                 */
                if (index==0) {
                    FolderDocument folderDocument = new FolderDocument( docName, docSize, "file:///android_asset/html/file_viewer.html" );
                    if ( doc.has( "images" ) ) {
                        List<FolderFilePageImage> pageImages = new ArrayList<FolderFilePageImage>();
                        JSONArray images = doc.getJSONArray( "images" );
                        for ( int indexImages = 0; indexImages < images.length(); indexImages++ ) {
                            JSONObject image = images.getJSONObject( indexImages );
                            FolderFilePageImage folderFilePageImage = new FolderFilePageImage( StaticHttpClient.getInstance().buildUrl( account, image.getString( "image" ) ) );
                            pageImages.add( folderFilePageImage );
                        }
                        folderDocument.setPageImages( pageImages );
                    }
                    folder.addDocument( folderDocument );
                }
                else {
                    // EN ATTENDANT LE SUPPORT DE MULTIPLES PIECES PRINCIPALES: LES AUTRES DOCS SONT DES ANNEXES
                    FolderAnnex folderDocument = new FolderAnnex( docName, docSize, "file:///android_asset/html/file_viewer.html" );
                    if ( doc.has( "images" ) ) {
                        List<FolderFilePageImage> pageImages = new ArrayList<FolderFilePageImage>();
                        JSONArray images = doc.getJSONArray( "images" );
                        for ( int indexImages = 0; indexImages < images.length(); indexImages++ ) {
                            JSONObject image = images.getJSONObject( indexImages );
                            FolderFilePageImage folderFilePageImage = new FolderFilePageImage( StaticHttpClient.getInstance().buildUrl( account, image.getString( "image" ) ) );
                            pageImages.add( folderFilePageImage );
                        }
                        folderDocument.setPageImages( pageImages );
                    }
                    folder.addAnnex( folderDocument );
                }
            }
        }
        return folder;
    }

    private static Date parseISO8601Date( String iso8601Date )
    {
        if ( Strings.isEmpty( iso8601Date ) ) {
            return null;
        }
        try {
            return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).parse( iso8601Date );
        } catch ( ParseException ex ) {
            Log.w( IParapheurHttpClient.class, "Unable to parse iParapheur date (" + iso8601Date + ")", ex );
            return null;
        }
    }

}
