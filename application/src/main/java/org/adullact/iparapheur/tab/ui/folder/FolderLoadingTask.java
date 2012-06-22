package org.adullact.iparapheur.tab.ui.folder;

import java.io.Serializable;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.os.AsyncTaskResult;
import org.codeartisans.android.toolbox.os.AsyncTaskWithMessageDialog;

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;

public class FolderLoadingTask
        extends AsyncTaskWithMessageDialog<FolderLoadingTask.Params, String, AsyncTaskResult<Folder, IParapheurTabException>>
{

    public static class Params
            implements Serializable
    {

        public static final long _serialVersionUID = 1L;

        public final String accountIdentity;

        public final String folderIdentity;

        public Params( String accountIdentity, String folderIdentity )
        {
            this.accountIdentity = accountIdentity;
            this.folderIdentity = folderIdentity;
        }

        @Override
        public String toString()
        {
            return "FolderLoadingTask.Params{" + accountIdentity + ", " + folderIdentity + "}";
        }

    }

    private final AccountsRepository accountsRepository;

    private final IParapheurHttpClient iParapheurClient;

    public FolderLoadingTask( FolderActivity context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super( context, "Veuillez patienter" );
        this.accountsRepository = accountsRepository;
        this.iParapheurClient = iParapheurClient;
    }

    @Override
    protected AsyncTaskResult<Folder, IParapheurTabException> doInBackground( Params... parameters )
    {
        publishProgress( "Chargement du dossier" );
        try {

            Params params = parameters[0];
            Account account = accountsRepository.byIdentity( params.accountIdentity );
            Log.d( context, "Will load folder with params: " + params + " using account: " + account );
            Folder folder = iParapheurClient.fetchFolder( account, params.folderIdentity );
            return new AsyncTaskResult<Folder, IParapheurTabException>( folder );

        } catch ( IParapheurHttpException ex ) {

            Log.w( context, "Unable to load folder, will return an error.", ex );
            return new AsyncTaskResult<Folder, IParapheurTabException>( ex );

        }
    }

}
