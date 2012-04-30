package org.adullact.iparapheur.tab.services;

import java.util.ArrayList;
import java.util.List;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Office;

public class DefaultIParapheurHttpClient
        implements IParapheurHttpClient
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
        offices.add( new Office( account.getTitle() + "-A1", "Bureau A/1", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-A2", "Bureau A/2", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-A3", "Bureau A/3", account.getTitle() + " A" ) );
        offices.add( new Office( account.getTitle() + "-B1", "Bureau B/1", account.getTitle() + " B" ) );
        offices.add( new Office( account.getTitle() + "-B2", "Bureau B/2", account.getTitle() + " B" ) );
        // TODO REMOVE ENDS

        return offices;
    }

}
