package org.adullact.iparapheur.tab.http.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import roboguice.service.RoboIntentService;

import de.akquinet.android.androlog.Log;

import org.adullact.iparapheur.tab.http.HttpVerb;

public class IParapheurHttpService
        extends RoboIntentService
{

    private static final String CALLBACK_BROADCAST_ACTION = "iparapheur:http:callback";

    /* package */ static final IntentFilter CALLBACK_INTENT_FILTER = new IntentFilter( CALLBACK_BROADCAST_ACTION );

    /* package */ static final String EXTRA_REQUEST_ID = "iparapheur:http:request-id";

    /* package */ static final String EXTRA_REQUEST_VERB = "iparapheur:http:request-verb";

    /* package */ static final String EXTRA_REQUEST_URL = "iparapheur:http:request-url";

    /* package */ static final String EXTRA_REQUEST_BODY = "iparapheur:http:request-body";

    public IParapheurHttpService()
    {
        super( IParapheurHttpService.class.getSimpleName() );
    }

    @Override
    protected void onHandleIntent( Intent intent )
    {
        // Parse incoming Intent
        Log.d( "Handling Intent: " + intent );
        int requestId = intent.getIntExtra( EXTRA_REQUEST_ID, -1 );
        HttpVerb requestVerb = ( HttpVerb ) intent.getExtras().get( EXTRA_REQUEST_VERB );
        String requestUrl = intent.getStringExtra( EXTRA_REQUEST_URL );

        // TODO
        // Dispatch
        // Create request body
        // Pass it to the IParapheurHttpProcessor that:
        //      Store pending status/data row in local database
        //      Attempt to run the HttpMethod

        // TODO REMOVE BEGIN
        try {
            Thread.sleep( 2000 );
        } catch ( InterruptedException ex ) {
        }
        // TODO REMOVE END

        // Setup callback Intent
        Intent callback = new Intent( CALLBACK_BROADCAST_ACTION );
        callback.putExtra( EXTRA_REQUEST_ID, requestId );
        callback.putExtra( EXTRA_REQUEST_VERB, requestVerb );
        callback.putExtra( EXTRA_REQUEST_URL, requestUrl );

        // Send callback
        Log.d( "Broadcasting Callback Intent: " + callback );
        LocalBroadcastManager.getInstance( this ).sendBroadcast( callback );
    }

    enum Plop
    {

        plop, zog

    }

}
