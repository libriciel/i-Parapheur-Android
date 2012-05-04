package org.adullact.iparapheur.tab.ui.dashboard;

import java.util.List;
import java.util.Map;

import org.adullact.iparapheur.tab.model.Community;
import org.adullact.iparapheur.tab.model.Office;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.util.AsyncTaskWithMessageDialog;

public class DashboardLoadingTask
        extends AsyncTaskWithMessageDialog<Void, String, Map<Community, List<Office>>>
{

    private final IParapheurHttpClient client;

    public DashboardLoadingTask( DashboardActivity context, IParapheurHttpClient client )
    {
        super( context, "Veuillez patienter" );
        this.client = client;
    }

    @Override
    protected Map<Community, List<Office>> doInBackground( Void... paramss )
    {
        publishProgress( "Chargement des bureaux" );
        return client.getOffices();
    }

}
