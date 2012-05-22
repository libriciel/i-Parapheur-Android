package org.adullact.iparapheur.tab;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class IParapheurTabModule
        extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind( String.class ).annotatedWith( Names.named( "androlog.filename" ) ).toInstance( "iparapheur-tab.properties" );
    }

}
