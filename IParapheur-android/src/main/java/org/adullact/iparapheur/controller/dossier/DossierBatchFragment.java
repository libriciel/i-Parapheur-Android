package org.adullact.iparapheur.controller.dossier;

import android.app.Fragment;
import android.widget.Toast;

import org.adullact.iparapheur.model.Dossier;

import java.util.HashSet;

/**
 * Created by jmaire on 16/11/2013.
 */
public class DossierBatchFragment extends Fragment {
    public static final String DOSSIER_ID = "dossier_id";
    private HashSet<Dossier> dossiers;

    public void addDossier(Dossier dossier) {
        if (dossiers.contains(dossier)) {
            dossiers.remove(dossier);
        }
        else {
            dossiers.add(dossier);
        }
        updateView();
    }

    private void updateView() {
        Toast.makeText(getActivity(), "Nombre de dossiers : " + dossiers.size(), Toast.LENGTH_LONG);
    }
}
