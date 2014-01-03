package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingWithProgressTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;

/**
 * Created by jmaire on 03/12/2013.
 */
public class TdtDialogFragment extends ActionDialogFragment {

    protected TextView annotationPublique;
    protected TextView annotationPrivee;

    public TdtDialogFragment(ArrayList<Dossier> dossiers, String bureauId) {
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
        return Action.TDT.getTitle();
    }

    @Override
    protected int getViewId() {
        return R.layout.action_dialog;
    }

    @Override
    protected void executeTask() {
        new TdtTask(getActivity()).execute();
    }

    private class TdtTask extends LoadingWithProgressTask {

        public TdtTask(Activity activity) {
            super(activity, listener);
        }

        @Override
        protected void load(String... params) {
            if (isCancelled()) {return;}
            String annotPub = annotationPublique.getText().toString();
            String annotPriv = annotationPrivee.getText().toString();
            int i = 0;
            int total = dossiers.size();
            publishProgress(i);
            for (Dossier dossier : dossiers) {
                if (isCancelled()) {return;}
                // TODO : distinguer Actes et Helios
                Log.d("debug", "Mailsec sur " + dossier.getName());
                RESTClient.INSTANCE.envoiTdtActes(dossier.getId(),
                        "",
                        annotPub,
                        annotPriv,
                        bureauId);
                i++;
                publishProgress(i * 100 / total);
            }
        }
    }
}
