package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdf.MuPDFCore;
import com.artifex.mupdf.MuPDFPageAdapter;
import com.artifex.mupdf.ReaderView;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.DataChangeListener;
import org.adullact.iparapheur.controller.connectivity.RESTClient;
import org.adullact.iparapheur.controller.utils.FileUtils;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;

import java.io.File;

/**
 * A fragment representing a single Dossier detail screen.
 * This fragment is contained in a {@link DossierListActivity}.
 */
public class DossierDetailFragment extends Fragment implements DataChangeListener {

    public interface DossierDetailListener {
        Dossier getDossier(String id);
    }

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String DOSSIER_ID = "dossier_id";

    /**
     * the bureau where the dossier belongs.
     */
    private String bureauId = "433149e9-a552-4472-90a6-2f08eb046eca";
    /**
     * The Dossier this fragment is presenting.
     */
    private Dossier dossier;
    /**
     * The Document this fragment is displaying in the reader view.
     */
    private Document document;

    private DossierDetailListener listener;

    private boolean isReaderEnabled;

    // MuPDF variables
    private MuPDFCore core;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DossierDetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dossier_detail, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments().containsKey(DOSSIER_ID)) {
            dossier = listener.getDossier(getArguments().getString(DOSSIER_ID));
            if (dossier != null) {
                this.isReaderEnabled = true;
                String state = Environment.getExternalStorageState();
                if (!Environment.MEDIA_MOUNTED.equals(state)) {
                    Toast.makeText(getActivity(), R.string.media_not_mounted, Toast.LENGTH_LONG);
                    this.isReaderEnabled = false;
                }
                // todo : reload if data is too old
                getDossierDetails(false);
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
        updateReader();
    }

    // TODO : aucun apercu disponible
    private void updateReader() {
        // TODO : remove false
        if (/*isReaderEnabled*/false && (document.getPath() != null)) {
            FrameLayout layout = (FrameLayout) getView().findViewById(R.id.fragment_dossier_detail_reader_view);
            ReaderView readerView;
            if ((layout.getChildCount() > 0) && (layout.getChildAt(0) instanceof ReaderView)) {
                readerView = (ReaderView) layout.getChildAt(0);
            }
            else {
                readerView = new ReaderView(getActivity());
            }
            try {
                if (core != null) {
                    core.onDestroy();
                }
                core = new MuPDFCore(document.getPath());
                readerView.setAdapter(new MuPDFPageAdapter(getActivity(), core, null));
                readerView.setDisplayedViewIndex(0);
                layout.invalidate();
                Log.d("debug", "Detail Fragment Data changed loading document done!");
            } catch (Exception e) {
                Log.e("FolderActivity", "Exception catched : " + e);
            }
        }
    }

    private void updateDetails() {
        if (getView() != null) {
            ((TextView) getView().findViewById(R.id.fragment_dossier_detail_title)).setText(dossier.getName());
            ((TextView) getView().findViewById(R.id.fragment_dossier_detail_type)).setText(dossier.getType());
            ((TextView) getView().findViewById(R.id.fragment_dossier_detail_sous_type)).setText(dossier.getSousType());
            ListView circuitView = (ListView) getView().findViewById(R.id.fragment_dossier_detail_circuit);
            circuitView.setAdapter(new CircuitAdapter(getActivity()));
        }
    }

    private class DossierLoadingTask extends LoadingTask {

        public DossierLoadingTask(Activity context, DataChangeListener listener) {
            super(context, listener);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return null;}
            Log.d("debug", "getting dossier details");
            dossier.saveDetails(RESTClient.INSTANCE.getDossier(bureauId, dossier.getId()));
            if (isCancelled()) {return null;}
            dossier.setCircuit(RESTClient.INSTANCE.getCircuit(dossier.getId()));
            if (isCancelled()) {return null;}

            if (isReaderEnabled && (dossier.getMainDocuments().size() > 0)) {
                document = dossier.getMainDocuments().get(0);

                if ((document.getPath() == null) || !(new File(document.getPath()).exists()))
                {
                    File file = FileUtils.getFileForDocument(dossier.getId(), document.getId());
                    String path = file.getAbsolutePath();
                    Log.d("debug", "saving document on disk");
                    String res = RESTClient.downloadFile(dossier.getMainDocuments().get(0).getUrl(), path);
                    if (res != null) {
                        document.setPath(res);
                    }
                }
            }
            return null;
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
                String validation = "le " + etape.getDateValidation() +
                        " " + getResources().getString(R.string.par) + " " +
                        etape.getSignataire();
                ((TextView) view.findViewById(R.id.etape_circuit_validation)).setText(validation);
            }
            return view;
        }
    }
}
