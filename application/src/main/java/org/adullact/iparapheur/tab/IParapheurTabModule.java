package org.adullact.iparapheur.tab;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.codeartisans.android.toolbox.http.HttpClient;

public class IParapheurTabModule
        extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind( String.class ).annotatedWith( Names.named( "androlog.filename" ) ).toInstance( "iparapheur-tab.properties" );

        bind( Long.class ).annotatedWith( Names.named( HttpClient.HTTP_CACHE_SIZE_NAME ) ).toInstance( Long.valueOf( 10 * 1024 * 1024 ) ); // 10MiB

        bind( org.apache.http.client.HttpClient.class ).to( HttpClient.class );
    }

}
