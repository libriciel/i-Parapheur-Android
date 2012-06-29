package org.adullact.iparapheur.tab.ui.actions;

import android.app.Activity;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.os.AsyncTaskResult;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;

public class SignTask
        extends ActionTask
{

    public SignTask( Activity context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super( context, accountsRepository, iParapheurClient );
    }

    @Override
    protected AsyncTaskResult<Void, IParapheurHttpException> doInBackground( ActionTaskParam... parameters )
    {
        publishProgress( "Signature en cours" );
        try {

            ActionTaskParam params = parameters[0];
            Account account = accountsRepository.byIdentity( params.accountIdentity );
            Log.d( context, "Will sign folder with params: " + params + " using account: " + account );
            iParapheurClient.sign( account, params.pubAnnotation, params.privAnnotation, params.folderIdentities );
            sleep( 1 );
            return new AsyncTaskResult<Void, IParapheurHttpException>( ( Void ) null );

        } catch ( IParapheurHttpException ex ) {

            Log.w( context, "Unable to sign folder, will return an error.", ex );
            return new AsyncTaskResult<Void, IParapheurHttpException>( ex );

        }
    }

}
