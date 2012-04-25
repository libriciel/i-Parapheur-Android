package org.adullact.iparapheur.tab.services;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Singleton;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Office;

// TODO Introduce interface (needs guice binding definition)
@Singleton
public class IParapheurHttpClient
{

    public List<Office> getOffices( Account account )
    {
        // TODO REMOVE BEGIN
        try {
            Thread.sleep( 500 );
        } catch ( InterruptedException ex ) {
            // Ignored
        }
        List<Office> offices = new ArrayList<Office>();
        offices.add( new Office( "Bureau A/1", "Collectivit� A" ) );
        offices.add( new Office( "Bureau A/2", "Collectivit� A" ) );
        offices.add( new Office( "Bureau A/3", "Collectivit� A" ) );
        offices.add( new Office( "Bureau B/1", "Collectivit� B" ) );
        offices.add( new Office( "Bureau B/2", "Collectivit� B" ) );
        // TODO REMOVE ENDS

        return offices;
    }

    public String foo()
    {
        return "bar";
    }

}
