package org.adullact.iparapheur.tab.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import roboguice.activity.event.OnCreateEvent;
import roboguice.activity.event.OnDestroyEvent;
import roboguice.event.Observes;
import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.model.Account;

@ContextSingleton
public class IParapheurHttpHelper
{

    private static final AtomicInteger REQUEST_COUNT = new AtomicInteger( 0 );

    public static interface IParapheurServiceCallback
    {

        void onServiceCallback();

    }

    private final Map<Integer, IParapheurServiceCallback> requestCallbacks = new HashMap<Integer, IParapheurServiceCallback>();

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive( Context cntxt, Intent intent )
        {
            int requestId = intent.getIntExtra( IParapheurHttpService.EXTRA_REQUEST_ID, -1 );
            if ( requestId == -1 ) {
                Log.d( "Received Intent don't hold a " + IParapheurHttpService.EXTRA_REQUEST_ID + " and will be ignored." );
            } else {
                IParapheurServiceCallback callback = requestCallbacks.get( requestId );
                if ( callback != null ) {
                    callback.onServiceCallback();
                }
            }
        }

    };

    @Inject
    private Context context;

    public void handleOnActivityCreate( @Observes OnCreateEvent event )
    {
        LocalBroadcastManager.getInstance( context ).registerReceiver( receiver, IParapheurHttpService.CALLBACK_INTENT_FILTER );
    }

    public void handleOnActivityDestroy( @Observes OnDestroyEvent event )
    {
        LocalBroadcastManager.getInstance( context ).unregisterReceiver( receiver );
    }

    public void getOffices( Account account, IParapheurServiceCallback callback )
    {
        // Generate new Request ID
        final int requestId = REQUEST_COUNT.getAndIncrement();

        // Build HTTP Request Intent parameters
        String method = "GET"; // TODO enum
        String url = account.getUrl() + "/offices";
        String body = null;

        // Setup HTTP Request Intent
        Intent intent = new Intent( context, IParapheurHttpService.class );
        intent.putExtra( IParapheurHttpService.EXTRA_REQUEST_ID, requestId );
        intent.putExtra( IParapheurHttpService.EXTRA_REQUEST_VERB, method );
        intent.putExtra( IParapheurHttpService.EXTRA_REQUEST_URL, url );
        intent.putExtra( IParapheurHttpService.EXTRA_REQUEST_BODY, body );

        // Register callback
        requestCallbacks.put( requestId, callback );

        // Start service invocation
        ComponentName serviceCompName = context.startService( intent );
        if ( serviceCompName == null ) {
            requestCallbacks.remove( requestId );
            throw new IParapheurTabException( "Unable to start IParapheurTab Http Intent Service." );
        }
    }

}
