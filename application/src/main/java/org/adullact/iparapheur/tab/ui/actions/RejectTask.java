package org.adullact.iparapheur.tab.ui.actions;

import android.app.Activity;
import de.akquinet.android.androlog.Log;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;

public class RejectTask
        extends ActionTask
{

    public RejectTask( Activity context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super( context, accountsRepository, iParapheurClient );
    }

    @Override
    protected AsyncTaskResult<Void, IParapheurHttpException> doInBackground( ActionTaskParam... parameters )
    {
        publishProgress( context.getResources().getString( R.string.actions_reject_in_progress ) );
        try {

            ActionTaskParam params = parameters[0];
            Account account = accountsRepository.byIdentity( params.accountIdentity );
            Log.d( context, "Will reject folder with params: " + params + " using account: " + account );
            iParapheurClient.reject( account, params.pubAnnotation, params.privAnnotation, params.officeIdentity, params.folderIdentities );
            sleep( 1 );
            return new AsyncTaskResult<Void, IParapheurHttpException>( ( Void ) null );

        } catch ( IParapheurHttpException ex ) {

            Log.w( context, "Unable to reject folder, will return an error.", ex );
            return new AsyncTaskResult<Void, IParapheurHttpException>( ex );

        }
    }

}
