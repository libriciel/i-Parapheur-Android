package org.adullact.iparapheur.tab.ui.actions;

import android.app.Activity;

import org.codeartisans.android.toolbox.os.AsyncTaskResult;
import org.codeartisans.android.toolbox.os.AsyncTaskWithMessageDialog;

import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;

/* package */ abstract class ActionTask
        extends AsyncTaskWithMessageDialog<ActionTaskParam, String, AsyncTaskResult<Void, IParapheurHttpException>>
{

    protected final AccountsRepository accountsRepository;

    protected final IParapheurHttpClient iParapheurClient;

    public ActionTask( Activity context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super( context, "Veuillez patienter" );
        this.accountsRepository = accountsRepository;
        this.iParapheurClient = iParapheurClient;
    }

    protected final void sleep( int seconds )
    {
        try {
            Thread.sleep( seconds * 1000 );
        } catch ( InterruptedException ignored ) {
        }
    }

}
