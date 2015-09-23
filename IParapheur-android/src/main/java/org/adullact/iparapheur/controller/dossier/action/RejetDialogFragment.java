package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.utils.LoadingWithProgressTask;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.IParapheurException;

import java.util.ArrayList;

/**
 * Created by jmaire on 03/12/2013.
 */
public class RejetDialogFragment extends ActionDialogFragment {

    protected TextView annotationPublique;
    protected TextView annotationPrivee;

    public RejetDialogFragment() {}

    public static RejetDialogFragment newInstance(ArrayList<Dossier> dossiers, String bureauId) {
        RejetDialogFragment f = new RejetDialogFragment();

        // Supply parameters as an arguments.
        Bundle args = new Bundle();
        args.putParcelableArrayList("dossiers", dossiers);
        args.putString("bureauId", bureauId);
        f.setArguments(args);

        return f;
    }

    @Override
    protected View createView() {
        View layout = super.createView();

        annotationPublique = (TextView) layout.findViewById(R.id.action_dialog_public_annotation);
        annotationPrivee = (TextView) layout.findViewById(R.id.action_dialog_private_annotation);

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

    private class RejetTask extends LoadingWithProgressTask {

        public RejetTask(Activity activity) {
            super(activity, listener);
        }

        @Override
        protected void load(String... params) throws IParapheurException {
            if (isCancelled()) {return;}
            String annotPub = annotationPublique.getText().toString();
            String annotPriv = annotationPrivee.getText().toString();
            int i = 0;
            int total = dossiers.size();
            publishProgress(i);
            for (Dossier dossier : dossiers) {
                if (isCancelled()) {return;}
                //Log.d("debug", "Rejet sur " + dossier.getName() + "avec le motif " + annotPub);
                RESTClient.INSTANCE.rejeter(dossier.getId(),
                        annotPub,
                        annotPriv,
                        bureauId);
                i++;
                publishProgress(i * 100 / total);
            }
        }
    }
}
