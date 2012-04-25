package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.util.AsyncTaskWithMessageDialog;

public class DashboardLoadingTask
        extends AsyncTaskWithMessageDialog<Void, String, Map<String, List<Office>>>
{

    private final IParapheurHttpClient client;

    public DashboardLoadingTask( DashboardActivity context )
    {
        super( context, "Veuillez patienter" );
        this.client = context.client;
    }

    @Override
    protected Map<String, List<Office>> doInBackground( Void... paramss )
    {
        // TODO REMOVE BEGINS
        List<Account> accounts = new ArrayList<Account>();
        accounts.add( new Account( "Col.A.Role.A" ) );
        accounts.add( new Account( "Col.A.Role.C" ) );
        accounts.add( new Account( "Col.B.Role.D" ) );
        // TODO REMOVE ENDS

        final Map<String, List<Office>> result = new HashMap<String, List<Office>>();

        for ( Account eachAccount : accounts ) {

            publishProgress( "Chargement des bureaux (" + eachAccount.getTitle() + ")" );

            List<Office> offices = client.getOffices( eachAccount );

            // Store in a Map by community
            for ( Office eachOffice : offices ) {
                if ( result.get( eachOffice.getCommunity() ) == null ) {
                    result.put( eachOffice.getCommunity(), new ArrayList<Office>() );
                }
                result.get( eachOffice.getCommunity() ).add( eachOffice );
            }

        }

        return result;
    }

}
