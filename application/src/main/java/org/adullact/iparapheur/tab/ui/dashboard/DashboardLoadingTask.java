package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.Collections;
import java.util.List;

import android.content.Context;

import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.util.AsyncTaskWithMessageDialog;

public class DashboardLoadingTask
        extends AsyncTaskWithMessageDialog<Void, Void, List<Office>>
{

    public DashboardLoadingTask( Context context, String message )
    {
        super( context, message );
    }

    @Override
    protected List<Office> doInBackground( Void... paramss )
    {
        try {
            Thread.sleep( 1000 );
        } catch ( InterruptedException ex ) {
            // Ignored
        }
        return Collections.emptyList();
    }

}
