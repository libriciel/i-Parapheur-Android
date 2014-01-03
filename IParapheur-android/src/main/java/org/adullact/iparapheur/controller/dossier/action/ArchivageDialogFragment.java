package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingWithProgressTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;

/**
 * Created by jmaire on 03/12/2013.
 */
public class ArchivageDialogFragment extends ActionDialogFragment {


    public ArchivageDialogFragment(ArrayList<Dossier> dossiers, String bureauId) {
        super(dossiers, bureauId);
    }

    @Override
    protected View createView() {
        View layout = super.createView();

        return layout;
    }

    @Override
    protected int getTitle() {
        return Action.ARCHIVAGE.getTitle();
    }

    @Override
    protected int getViewId() {
        return R.layout.action_dialog;
    }

    @Override
    protected void executeTask() {
        new ArchivageTask(getActivity()).execute();
    }

    private class ArchivageTask extends LoadingWithProgressTask {

        public ArchivageTask(Activity activity) {
            super(activity, listener);
        }

        @Override
        protected void load(String... params) {
            if (isCancelled()) {return;}
            int i = 0;
            int total = dossiers.size();
            publishProgress(i);
            for (Dossier dossier : dossiers) {
                if (isCancelled()) {return;}
                Log.d("debug", "Archivage du dossier " + dossier.getName());
                RESTClient.INSTANCE.archiver(dossier.getId(),
                        dossier.getName() + "pdf",
                        false,
                        bureauId);
                i++;
                publishProgress(i * 100 / total);
            }
        }
    }
}
