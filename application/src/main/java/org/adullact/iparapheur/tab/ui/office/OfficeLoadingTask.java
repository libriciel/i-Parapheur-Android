package org.adullact.iparapheur.tab.ui.office;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import de.akquinet.android.androlog.Log;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;
import org.codeartisans.android.toolbox.os.AsyncTaskWithMessageDialog;

public class OfficeLoadingTask
        extends AsyncTaskWithMessageDialog<OfficeLoadingTask.Params, String, List<Folder>>
{

    public static class Params
            implements Serializable
    {

        public static final long _serialVersionUID = 1L;

        public final String accountIdentity;

        public final String officeIdentity;

        public final int page;

        public final int pageSize;

        public Params( String accountIdentity, String officeIdentity, int page, int pageSize )
        {
            this.accountIdentity = accountIdentity;
            this.officeIdentity = officeIdentity;
            this.page = page;
            this.pageSize = pageSize;
        }

        @Override
        public String toString()
        {
            return "OfficeLoadingTask.Params[" + accountIdentity + ", " + officeIdentity + ", " + page + ", " + pageSize + "]";
        }

    }

    private final AccountsRepository accountsRepository;

    private final IParapheurHttpClient iParapheurClient;

    public OfficeLoadingTask( OfficeActivity context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super( context, "Veuillez patienter" );
        this.accountsRepository = accountsRepository;
        this.iParapheurClient = iParapheurClient;
    }

    @Override
    protected List<Folder> doInBackground( Params... parameters )
    {
        publishProgress( "Chargement des dossiers" );
        try {
            Params params = parameters[0];
            Account account = accountsRepository.byIdentity( params.accountIdentity );
            Log.d( context, "Will load folders with params: " + params + " using account: " + account );
            return iParapheurClient.fetchFolders( account,
                                                  params.officeIdentity,
                                                  params.page,
                                                  params.pageSize );
        } catch ( IParapheurHttpException ex ) {
            Log.w( context, "Unable to load folders, will return an empty list", ex );
            return Collections.emptyList();
        }
    }

}
