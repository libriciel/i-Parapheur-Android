package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;

/**
 * Created by jmaire on 03/12/2013.
 */
public class RejetDialogFragment extends ActionDialogFragment {

    protected TextView annotationPublique;
    protected TextView annotationPrivee;

    public RejetDialogFragment(ArrayList<Dossier> dossiers, String bureauId) {
        super(dossiers, bureauId);
    }

    @Override
    protected View createView() {
        View layout = super.createView();

        annotationPublique = (TextView) layout.findViewById(R.id.action_dialog_annotation_publique);
        annotationPrivee = (TextView) layout.findViewById(R.id.action_dialog_annotation_privee);

        return layout;
    }

    @Override
    protected int getTitle() {
        return Action.REJET.getTitle();
    }

    @Override
    protected int getViewId() {
        return R.layout.action_dialog;
    }

    @Override
    protected void executeTask() {
        String motif = annotationPublique.getText().toString();
        if (!motif.trim().isEmpty()) {
            new RejetTask(getActivity()).execute();
        }
        else {
            Toast.makeText(getActivity(), R.string.justify_reject, Toast.LENGTH_SHORT).show();
        }
    }

    private class RejetTask extends LoadingTask {

        public RejetTask(Activity activity) {
            super(activity, listener);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (isCancelled()) {return null;}
            String annotPub = annotationPublique.getText().toString();
            String annotPriv = annotationPrivee.getText().toString();
            int i = 0;
            int total = dossiers.size();
            publishProgress(i);
            for (Dossier dossier : dossiers) {
                if (isCancelled()) {return null;}
                Log.d("debug", "Rejet sur " + dossier.getName() + "avec le motif " + annotPub);
                RESTClient.INSTANCE.rejeter(dossier.getId(),
                        annotPub,
                        annotPriv,
                        bureauId);
                i++;
                publishProgress(i * 100 / total);
            }

            return null;
        }
    }
}
