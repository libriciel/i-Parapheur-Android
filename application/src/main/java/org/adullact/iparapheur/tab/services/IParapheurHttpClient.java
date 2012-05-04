package org.adullact.iparapheur.tab.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.apache.http.client.HttpClient;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Community;
import org.adullact.iparapheur.tab.model.Office;

@ContextSingleton
public class IParapheurHttpClient
{

    @Inject
    private AccountsRepository accountsRepository;

    @Inject
    private HttpClient httpClient;

    public Map<Community, List<Office>> getOffices()
    {
        final Map<Community, List<Office>> result = new HashMap<Community, List<Office>>();

        for ( Account eachAccount : accountsRepository.all() ) {

            List<Office> offices = getOffices( eachAccount );

            // Sort by community
            for ( Office eachOffice : offices ) {
                if ( result.get( eachOffice.getCommunity() ) == null ) {
                    result.put( eachOffice.getCommunity(), new ArrayList<Office>() );
                }
                result.get( eachOffice.getCommunity() ).add( eachOffice );
            }

        }

        return result;
    }

    private List<Office> getOffices( Account account )
    {
        System.out.println( "HTTP CLIENT IN iParapheur CLIENT is: " + httpClient );
        // TODO REMOVE BEGIN
        try {
            Thread.sleep( 500 );
        } catch ( InterruptedException ex ) {
            // Ignored
        }
        List<Office> offices = new ArrayList<Office>();
        offices.add( new Office( account.getTitle() + "-A1", "Bureau A/1", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-A2", "Bureau A/2", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-A3", "Bureau A/3", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-B1", "Bureau B/1", account.getTitle() + " B" ) );
        offices.add( new Office( account.getTitle() + "-B2", "Bureau B/2", account.getTitle() + " B" ) );
        // TODO REMOVE ENDS

        return offices;
    }

}
