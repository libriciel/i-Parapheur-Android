package org.adullact.iparapheur.tab.ui.office;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.os.AsyncTaskResult;
import org.codeartisans.android.toolbox.os.AsyncTaskWithMessageDialog;

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.model.OfficeFacet;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;

public class OfficeLoadingTask
        extends AsyncTaskWithMessageDialog<OfficeLoadingTask.Params, String, AsyncTaskResult<List<Folder>, IParapheurTabException>>
{

    public static class Params
            implements Serializable
    {

        public static final long _serialVersionUID = 1L;

        public final String accountIdentity;

        public final String officeIdentity;

        public final Map<OfficeFacet, Collection<String>> facetSelection;

        public final int page;

        public final int pageSize;

        public Params( String accountIdentity, String officeIdentity, Map<OfficeFacet, Collection<String>> facetSelection, int page, int pageSize )
        {
            this.accountIdentity = accountIdentity;
            this.officeIdentity = officeIdentity;
            this.facetSelection = facetSelection;
            this.page = page;
            this.pageSize = pageSize;
        }

        @Override
        public String toString()
        {
            return "OfficeLoadingTask.Params[" + accountIdentity + ", " + officeIdentity + ", " + facetSelection + ", " + page + ", " + pageSize + "]";
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
    protected AsyncTaskResult<List<Folder>, IParapheurTabException> doInBackground( Params... parameters )
    {
        publishProgress( "Chargement des dossiers" );
        try {

            Params params = parameters[0];
            Account account = accountsRepository.byIdentity( params.accountIdentity );
            if ( account == null ) {
                return new AsyncTaskResult<List<Folder>, IParapheurTabException>( Collections.<Folder>emptyList(), new IParapheurTabException( "Le compte concerné n'existe plus." ) );
            }
            if ( !account.validates() ) {
                return new AsyncTaskResult<List<Folder>, IParapheurTabException>( Collections.<Folder>emptyList(), new IParapheurTabException( "Le compte concerné n'est plus valide, veuillez le compléter." ) );
            }
            Log.d( context, "Will load folders with params: " + params + " using account: " + account );
            List<Folder> folders = iParapheurClient.fetchFolders( account,
                                                                  params.officeIdentity,
                                                                  params.facetSelection,
                                                                  params.page,
                                                                  params.pageSize );
            return new AsyncTaskResult<List<Folder>, IParapheurTabException>( folders );

        } catch ( IParapheurHttpException ex ) {

            Log.w( context, "Unable to load folders", ex );
            return new AsyncTaskResult<List<Folder>, IParapheurTabException>( Collections.<Folder>emptyList(), ex );

        }
    }

}
