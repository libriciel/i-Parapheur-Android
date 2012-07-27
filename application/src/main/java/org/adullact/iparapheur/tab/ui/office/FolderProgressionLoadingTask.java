package org.adullact.iparapheur.tab.ui.office;

import android.content.Context;
import android.os.AsyncTask;
import de.akquinet.android.androlog.Log;
import java.io.Serializable;
import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Progression;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;

public class FolderProgressionLoadingTask
        extends AsyncTask<FolderProgressionLoadingTask.Params, String, AsyncTaskResult<Progression, IParapheurTabException>>
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
            return "FolderProgressionLoadingTask.Params{" + accountIdentity + ", " + folderIdentity + "}";
        }

    }

    private final Context context;

    private final AccountsRepository accountsRepository;

    private final IParapheurHttpClient iParapheurClient;

    public FolderProgressionLoadingTask( Context context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super();
        this.context = context;
        this.accountsRepository = accountsRepository;
        this.iParapheurClient = iParapheurClient;
    }

    @Override
    protected AsyncTaskResult<Progression, IParapheurTabException> doInBackground( Params... parameters )
    {
        try {

            FolderProgressionLoadingTask.Params params = parameters[0];
            Account account = accountsRepository.byIdentity( params.accountIdentity );
            Log.d( context, "Will load folder progression with params: " + params + " using account: " + account );
            Progression progression = iParapheurClient.fetchFolderProgression( account, params.folderIdentity );
            return new AsyncTaskResult<Progression, IParapheurTabException>( progression );

        } catch ( IParapheurHttpException ex ) {

            Log.w( context, "Unable to load folder progression, will return an error.", ex );
            return new AsyncTaskResult<Progression, IParapheurTabException>( ex );

        }
    }

}
