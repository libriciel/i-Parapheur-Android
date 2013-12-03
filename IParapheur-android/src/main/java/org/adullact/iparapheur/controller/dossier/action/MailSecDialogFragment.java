package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;

import java.util.ArrayList;

/**
 * Created by jmaire on 03/12/2013.
 */
public class MailSecDialogFragment extends ActionDialogFragment {

    protected TextView annotationPublique;
    protected TextView annotationPrivee;


    public MailSecDialogFragment(ArrayList<Dossier> dossiers, String bureauId) {
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
        return Action.MAILSEC.getTitle();
    }

    @Override
    protected int getViewId() {
        return R.layout.action_dialog;
    }

    @Override
    protected void executeTask() {
        new MailsecTask(getActivity()).execute();
    }

    private class MailsecTask extends LoadingTask {

        public MailsecTask(Activity activity) {
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
                Log.d("debug", "Mailsec sur " + dossier.getName());
                RESTClient.INSTANCE.envoiMailSec(dossier.getId(),
                        null,
                        "",
                        "",
                        false,
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
