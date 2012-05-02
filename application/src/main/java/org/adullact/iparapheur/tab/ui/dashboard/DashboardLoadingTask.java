package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.util.AsyncTaskWithMessageDialog;

public class DashboardLoadingTask
        extends AsyncTaskWithMessageDialog<Void, String, Map<String, List<Office>>>
{

    private final AccountsRepository accountsRepository;

    private final IParapheurHttpClient client;

    public DashboardLoadingTask( DashboardActivity context )
    {
        super( context, "Veuillez patienter" );
        this.accountsRepository = context.accountsRepository;
        this.client = context.client;
    }

    @Override
    protected Map<String, List<Office>> doInBackground( Void... paramss )
    {
        final Map<String, List<Office>> result = new HashMap<String, List<Office>>();

        for ( Account eachAccount : accountsRepository.all() ) {

            publishProgress( "Chargement des bureaux (" + eachAccount.getTitle() + ")" );

            List<Office> offices = client.getOffices( eachAccount );

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

}
