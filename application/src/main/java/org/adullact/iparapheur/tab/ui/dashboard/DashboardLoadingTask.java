package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.os.AsyncTaskResult;
import org.codeartisans.android.toolbox.os.AsyncTaskWithMessageDialog;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Community;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;

public class DashboardLoadingTask
        extends AsyncTaskWithMessageDialog<Void, String, AsyncTaskResult<Map<Community, List<Office>>, IParapheurHttpException>>
{

    private final AccountsRepository accountsRepository;

    private final IParapheurHttpClient iParapheurClient;

    public DashboardLoadingTask( DashboardActivity context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super( context, "Veuillez patienter" );
        this.accountsRepository = accountsRepository;
        this.iParapheurClient = iParapheurClient;
    }

    @Override
    protected AsyncTaskResult<Map<Community, List<Office>>, IParapheurHttpException> doInBackground( Void... paramss )
    {
        publishProgress( "Chargement des bureaux" );

        final Map<Community, List<Office>> result = new HashMap<Community, List<Office>>();
        List<Account> accounts = accountsRepository.all();
        List<IParapheurHttpException> errors = new ArrayList<IParapheurHttpException>();
        Log.d( context, "Will load offices from " + accounts.size() + " accounts" );
        for ( Account eachAccount : accounts ) {
            try {

                publishProgress( "Chargement des bureaux (" + eachAccount.getTitle() + ")" );

                List<Office> offices = iParapheurClient.fetchOffices( eachAccount );

                Log.i( context, "Loaded " + offices.size() + " offices for account " + eachAccount.getTitle() );

                // Sort by community
                for ( Office eachOffice : offices ) {
                    if ( result.get( eachOffice.getCommunity() ) == null ) {
                        result.put( eachOffice.getCommunity(), new ArrayList<Office>() );
                    }
                    result.get( eachOffice.getCommunity() ).add( eachOffice );
                }

            } catch ( IParapheurHttpException ex ) {
                Log.w( context, ex.getMessage(), ex );
                errors.add( ex );
            }
        }
        publishProgress( "Chargement des bureaux terminé" );
        return new AsyncTaskResult<Map<Community, List<Office>>, IParapheurHttpException>( result, errors );
    }

}
