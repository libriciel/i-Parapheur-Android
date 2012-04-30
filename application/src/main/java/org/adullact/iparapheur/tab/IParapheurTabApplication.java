package org.adullact.iparapheur.tab;

import android.app.Application;
import android.content.res.Configuration;

import de.akquinet.android.androlog.Log;

public class IParapheurTabApplication
        extends Application
{

    @Override
    public void onConfigurationChanged( Configuration newConfig )
    {
        Log.i( this, "Application configuration changed. New config is: " + newConfig );
        super.onConfigurationChanged( newConfig );
    }

    @Override
    public void onCreate()
    {
        Log.i( this, "Application is about to be created." );
        super.onCreate();
        Log.i( this, "Application has been created." );
    }

    @Override
    public void onLowMemory()
    {
        Log.i( this, "Application is running low on memory." );
        super.onLowMemory();
    }

    @Override
    public void onTerminate()
    {
        Log.i( this, "Application is about to terminate NOW!" );
        super.onTerminate();
    }

}
