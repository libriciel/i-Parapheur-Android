package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
public class TdtHeliosDialogFragment extends ActionDialogFragment {

    protected TextView annotationPublique;
    protected TextView annotationPrivee;

    public TdtHeliosDialogFragment() {}

    public static TdtHeliosDialogFragment newInstance(ArrayList<Dossier> dossiers, String bureauId) {
        TdtHeliosDialogFragment f = new TdtHeliosDialogFragment();

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

        annotationPublique = (TextView) layout.findViewById(R.id.action_dialog_annotation_publique);
        annotationPrivee = (TextView) layout.findViewById(R.id.action_dialog_annotation_privee);

        return layout;
    }

    @Override
    protected int getTitle() {
        return Action.TDT_HELIOS.getTitle();
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
        protected void load(String... params) throws IParapheurException {
            if (isCancelled()) {return;}
            String annotPub = annotationPublique.getText().toString();
            String annotPriv = annotationPrivee.getText().toString();
            int i = 0;
            int total = dossiers.size();
            publishProgress(i);
            for (Dossier dossier : dossiers) {
                if (isCancelled()) {return;}
                // TODO : distinguer Actes et Helios
                //Log.d("debug", "Mailsec sur " + dossier.getName());
                RESTClient.INSTANCE.envoiTdtActes(dossier.getId(),
                        "", "", "", 0l, "",
                        annotPub,
                        annotPriv,
                        bureauId);
                i++;
                publishProgress(i * 100 / total);
            }
        }
    }
}
