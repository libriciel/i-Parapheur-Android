package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.document.DocumentReader;
import org.adullact.iparapheur.controller.utils.FileUtils;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * A fragment representing a single Dossier detail screen.
 * This fragment is contained in a {@link DossiersActivity}.
 */
public class DossierDetailFragment extends Fragment implements LoadingTask.DataChangeListener {

    public interface DossierDetailListener {
        Dossier getDossier(String id);
    }

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String DOSSIER_ID = "dossier_id";
    /**
     * The id of the selected bureau.
     */
    public static final String BUREAU_ID = "bureau_id";

    /**
     * the bureau where the dossier belongs.
     */
    private String bureauId;
    /**
     * The Dossier this fragment is presenting.
     */
    private Dossier dossier;

    private DossierDetailListener listener;

    private boolean isReaderEnabled;

    private DocumentReader reader;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DossierDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dossier_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments().containsKey(BUREAU_ID)) {
            bureauId = getArguments().getString(BUREAU_ID);
            if (getArguments().containsKey(DOSSIER_ID)) {
                dossier = listener.getDossier(getArguments().getString(DOSSIER_ID));
                if (dossier != null) {
                    this.isReaderEnabled = true;
                    String state = Environment.getExternalStorageState();
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {
                        Toast.makeText(getActivity(), R.string.media_not_mounted, Toast.LENGTH_LONG).show();
                        this.isReaderEnabled = false;
                    }
                    // todo : reload if data is too old
                    getDossierDetails(false);
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof DossierDetailListener))
        {
            throw new IllegalStateException("Activity must implement DossierDetailListener.");
        }
        listener = (DossierDetailListener) activity;
    }

    private void getDossierDetails(boolean forceReload) {
        // To force reload dossier details, just delete its main document path (on local storage).
        if (forceReload) {
            if ((dossier.getMainDocuments() != null) || !dossier.getMainDocuments().isEmpty()) {
                dossier.getMainDocuments().get(0).setPath(null);
            }
        }
        // Download information only if details aren't already available
        if (!dossier.isDetailsAvailable()) {
            new DossierLoadingTask(getActivity(), this).execute();
        }
        else {
            onDataChanged();
        }
    }

    @Override
    public void onDataChanged() {
        updateDetails();
        // OFFLINE : commenter updateDetails
        updateReader();
    }

    // TODO : aucun apercu disponible
    private void updateReader() {
        Document document = dossier.getMainDocuments().get(0);
        if (isReaderEnabled && (document.getPath() != null)) {
            FrameLayout layout = (FrameLayout) getView().findViewById(R.id.fragment_dossier_detail_reader_view);
            if ((layout.getChildCount() > 0) && (layout.getChildAt(0) instanceof DocumentReader)) {
                reader = (DocumentReader) layout.getChildAt(0);
            }
            else {
                reader = new DocumentReader(getActivity());
                layout.addView(reader);
            }
            reader.setDocument(document);
            layout.invalidate();
        }
    }

    private void updateDetails() {
        if (getView() != null) {
            ((TextView) getView().findViewById(R.id.fragment_dossier_detail_title)).setText(dossier.getName());
            ((TextView) getView().findViewById(R.id.fragment_dossier_detail_typologie)).setText(dossier.getType() + " / " + dossier.getSousType());
            ListView circuitView = (ListView) getView().findViewById(R.id.fragment_dossier_detail_circuit);
            circuitView.setAdapter(new CircuitAdapter(getActivity()));
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dossier_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_details:
                toggleDetails();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleDetails() {
        View details = getView().findViewById(R.id.fragment_dossier_detail_details);
        if (details.getVisibility() == View.VISIBLE) {
            details.setVisibility(View.INVISIBLE);
        }
        else {
            details.setVisibility(View.VISIBLE);
        }
    }

    private class DossierLoadingTask extends LoadingTask {

        public DossierLoadingTask(Activity context, DataChangeListener listener) {
            super(context, listener);
        }

        @Override
        protected void load(String... params)
        {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return;}
            Log.d("debug", "getting dossier details");
            Dossier d = RESTClient.INSTANCE.getDossier(bureauId, dossier.getId());
            if (dossier != null) {
                dossier.saveDetails(d);
            }
            // OFFLINE
            //dossier.addDocument(new Document(
            /*        UUID.randomUUID().toString(),
                    "ducument par defaut",
                    ""));*/
            if (isCancelled()) {return;}
            dossier.setCircuit(RESTClient.INSTANCE.getCircuit(dossier.getId()));
            // OFFLINE : commenter la ligne au dessus
            if (isCancelled()) {return;}

            if (isReaderEnabled && (dossier.getMainDocuments().size() > 0)) {
                Document document = dossier.getMainDocuments().get(0);

                if ((document.getPath() == null) || !(new File(document.getPath()).exists()))
                {
                    File file = FileUtils.getFileForDocument(dossier.getId(), document.getId());
                    String path = file.getAbsolutePath();
                    Log.d("debug", "saving document on disk");
                    if (RESTClient.downloadFile(dossier.getMainDocuments().get(0).getUrl(), path)) {
                        document.setPath(path);
                    }
                    // OFFLINE
                    /*document.setPath(path);
                    if (file.exists()) {
                        Log.d("debug", "Document par defaut trouvé");
                    }
                    else {
                        Log.d("debug", "Document par defaut non trouvé");
                    }*/
                }
            }
        }
    }

    private class CircuitAdapter extends ArrayAdapter<EtapeCircuit>
    {
        public CircuitAdapter(Context context) {
            super(context, R.layout.etape_circuit, R.id.etape_circuit_bureau, dossier.getCircuit());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            EtapeCircuit etape = getItem(position);
            ((ImageView) view.findViewById(R.id.etape_circuit_icon)).setImageResource(etape.getAction().getIcon(etape.isApproved()));
            if (etape.isApproved()) {
                SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                String validation = getResources().getString(R.string.par) + " " +
                        etape.getSignataire() +
                        getResources().getString(R.string.the) + df.format(etape.getDateValidation());
                ((TextView) view.findViewById(R.id.etape_circuit_validation)).setText(validation);
            }
            return view;
        }
    }
}
