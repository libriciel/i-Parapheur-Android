package org.adullact.iparapheur.tab;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.adullact.iparapheur.tab.services.DefaultIParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;

public class IParapheurTabModule
        extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind( String.class ).annotatedWith( Names.named( "androlog.filename" ) ).toInstance( "iparapheur-tab.properties" );

        bind( IParapheurHttpClient.class ).to( DefaultIParapheurHttpClient.class ).in( Singleton.class );
    }

}
