package org.adullact.iparapheur.tab.ui.office;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;
import org.adullact.iparapheur.tab.util.AsyncTaskWithMessageDialog;

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
    protected List<Folder> doInBackground( Params... params )
    {
        publishProgress( "Chargement des dossiers" );
        try {
            return iParapheurClient.fetchFolders( accountsRepository.byIdentity( params[0].accountIdentity ),
                                                  params[0].officeIdentity,
                                                  params[0].page,
                                                  params[0].pageSize );
        } catch ( IParapheurHttpException ex ) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

}
