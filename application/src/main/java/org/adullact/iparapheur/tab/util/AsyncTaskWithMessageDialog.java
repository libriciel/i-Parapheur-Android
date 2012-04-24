package org.adullact.iparapheur.tab.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class AsyncTaskWithMessageDialog<Params extends Object, Progress extends Object, Result extends Object>
        extends AsyncTask<Params, Progress, Result>
{

    private final Context context;

    private final String message;

    private ProgressDialog wait = null;

    public AsyncTaskWithMessageDialog( Context context, String message )
    {
        this.context = context;
        this.message = message;
    }

    @Override
    protected final void onPreExecute()
    {
        wait = ProgressDialog.show( context, "", message, true, false );
        super.onPreExecute();
    }

    @Override
    protected final void onPostExecute( Result result )
    {
        super.onPostExecute( result );
        beforeDialogDismiss( result );
        wait.dismiss();
        afterDialogDismiss( result );
    }

    /**
     * This method is called on the UI thread.
     */
    protected void beforeDialogDismiss( Result result )
    {
    }

    /**
     * This method is called on the UI thread.
     */
    protected void afterDialogDismiss( Result result )
    {
    }

}
