package org.adullact.iparapheur.tab.ui.dashboard;

import de.akquinet.android.androlog.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Community;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;
import org.codeartisans.android.toolbox.os.AsyncTaskWithMessageDialog;

public class DashboardLoadingTask
        extends AsyncTaskWithMessageDialog<Void, String, AsyncTaskResult<Map<Community, List<Office>>, IParapheurTabException>>
{

    private final AccountsRepository accountsRepository;

    private final IParapheurHttpClient iParapheurClient;

    public DashboardLoadingTask( DashboardActivity context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super( context, context.getResources().getString( R.string.words_wait ) );
        this.accountsRepository = accountsRepository;
        this.iParapheurClient = iParapheurClient;
    }

    @Override
    protected AsyncTaskResult<Map<Community, List<Office>>, IParapheurTabException> doInBackground( Void... paramss )
    {
        String loadingMessage = context.getResources().getString( R.string.dashboard_loading );
        publishProgress( loadingMessage );

        final Map<Community, List<Office>> result = new HashMap<Community, List<Office>>();
        List<Account> accounts = accountsRepository.all();
        List<IParapheurTabException> errors = new ArrayList<IParapheurTabException>();

        Log.d( context, "Will load offices from " + accounts.size() + " accounts" );
        for ( Account eachAccount : accounts ) {

            if ( !eachAccount.validates() ) {
                Log.w( context, "Account '" + eachAccount + "' is not valid, skipping." );
                continue;
            }

            try {

                publishProgress( loadingMessage + " (" + eachAccount.getTitle() + ")" );

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
        publishProgress( context.getResources().getString( R.string.dashboard_loading_done ) );
        return new AsyncTaskResult<Map<Community, List<Office>>, IParapheurTabException>( result, errors );
    }

}
