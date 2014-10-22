package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.IParapheur;
import org.adullact.iparapheur.controller.circuit.CircuitAdapter;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.controller.document.DocumentPagerAdapter;
import org.adullact.iparapheur.controller.utils.FileUtils;
import org.adullact.iparapheur.controller.utils.LoadingTask;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.controller.utils.IParapheurException;
import org.adullact.iparapheur.model.EtapeCircuit;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * A fragment representing a single Dossier detail screen.
 * This fragment is contained in a {@link DossiersActivity}.
 */
public class DossierDetailFragment extends Fragment implements LoadingTask.DataChangeListener, SeekBar.OnSeekBarChangeListener {

    public interface DossierDetailListener {
        Dossier getDossier(String id);
    }

    public static String TAG = "Dossier_details";
    /**
     * the bureau where the dossier belongs.
     */
    private String bureauId;

    /**
     * The Dossier id this fragment is presenting. Used only in initialisation.
     */
    private String dossierId;

    /**
     * The Dossier this fragment is presenting.
     */
    private Dossier dossier;

    private DossierDetailListener listener;

    private boolean isReaderEnabled;

    //private DocumentReader reader;

    /**
     *  Used to display the document's pages. Each page is managed by a fragment.
     */
    private ViewPager viewPager;
    /**
     * Used to navigate through document's pages.
     */
    private PagerTitleStrip pagerTitleStrip;

    private int currentPage;

    //private SeekBar seekBar;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DossierDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            bureauId = getArguments().getString(DossiersActivity.BUREAU_ID);
            dossierId = getArguments().getString(DossiersActivity.DOSSIER_ID);
        }
        this.currentPage = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dossier_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.isReaderEnabled = true;
        /*this.seekBar = (SeekBar) view.findViewById(R.id.fragment_dossier_detail_seekbar);
        seekBar.setVisibility(View.INVISIBLE);
        seekBar.setOnSeekBarChangeListener(this);*/
        this.viewPager = (ViewPager) view.findViewById(R.id.fragment_dossier_detail_pager);
        this.pagerTitleStrip = (PagerTitleStrip) view.findViewById(R.id.fragment_dossier_detail_title_strip);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.viewPager.post(new Runnable() {
            public void run() {
                viewPager.setAdapter(new DocumentPagerAdapter(getActivity(), getActivity().getSupportFragmentManager()));
            }
        });
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(getActivity(), R.string.media_not_mounted, Toast.LENGTH_LONG).show();
            this.isReaderEnabled = false;
        }
        if (dossierId != null) {
            getDossierDetails(false);
        }
        setHasOptionsMenu(true);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof DossierDetailListener))
        {
            throw new IllegalStateException("Activity must implement DossierDetailListener.");
        }
        listener = (DossierDetailListener) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if (reader != null) {
            reader.clean();
        }*/
    }

    /*@Override
    public void onPageChange(int numPage) {
        currentPage = numPage;*/
        /*if (seekBar != null) {
            seekBar.setProgress(currentPage);
        }*/
    //}

    public void update(String bureauId, String dossierId) {
        /*if (reader != null) {
            reader.clean();
        }
        ((FrameLayout) getView().findViewById(R.id.fragment_dossier_detail_reader_view)).removeAllViews();*/
        this.bureauId = bureauId;
        this.dossierId = dossierId;
        this.dossier = null;
        closeDetails();
        if (dossierId != null) {
            getDossierDetails(false);
        }
        else {
            updateReader();
        }
    }

    private void getDossierDetails(boolean forceReload) {
        if (dossier == null) {
            dossier = listener.getDossier(dossierId);
        }
        // To force reload dossier details, just delete its main document path (on local storage).
        if (forceReload) {
            dossier.clearDetails();
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
        if (!IParapheur.OFFLINE) {
            updateDetails();
        }
        updateReader();
    }

    // TODO : aucun apercu disponible
    private void updateReader() {
        if (dossier != null && !dossier.getMainDocuments().isEmpty()) {
            final Document document = dossier.getMainDocuments().get(0);
            if (isReaderEnabled && (document.getPath() != null)) {
                this.viewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ((DocumentPagerAdapter) viewPager.getAdapter()).setDocument(document);
                            viewPager.setCurrentItem(0, false );
                        } catch (Exception e) {
                            //e.printStackTrace();
                            Toast.makeText(getActivity(), R.string.error_reading_document, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                //reader.setDisplayedViewIndex(currentPage);
                /*int pagesCount = reader.getPagesCount();
                seekBar.setMax((pagesCount > 0)? pagesCount - 1 :0);
                seekBar.setProgress(0);
                seekBar.setVisibility(View.VISIBLE);*/
                //layout.invalidate();
            }
        }
        else {
            this.viewPager.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        ((DocumentPagerAdapter) viewPager.getAdapter()).setDocument(null);
                        //viewPager.setCurrentItem(0, false);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.error_reading_document, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void updateDetails() {
        if (getView() != null) {
            ((TextView) getView().findViewById(R.id.fragment_dossier_detail_title)).setText(dossier.getName());
            ((TextView) getView().findViewById(R.id.fragment_dossier_detail_typologie)).setText(dossier.getType() + " / " + dossier.getSousType());
            ListView circuitView = (ListView) getView().findViewById(R.id.fragment_dossier_detail_circuit);
            circuitView.setAdapter(new CircuitAdapter(getActivity(), dossier.getCircuit()));
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dossier_details_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu (Menu menu) {
        MenuItem item = menu.findItem(R.id.action_details);
        item.setVisible((dossier != null) && dossier.isDetailsAvailable());
        super.onPrepareOptionsMenu(menu);

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
        else if ((dossier != null) && dossier.isDetailsAvailable()){
            details.setVisibility(View.VISIBLE);
        }
    }

    private void closeDetails() {
        View details = getView().findViewById(R.id.fragment_dossier_detail_details);
        if ((details != null) && (details.getVisibility() == View.VISIBLE)) {
            details.setVisibility(View.INVISIBLE);
        }
    }

    /* SeekBar Listener implementation */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (currentPage != seekBar.getProgress()) {
            currentPage = seekBar.getProgress();
            //reader.setDisplayedViewIndex(currentPage);
        }
    }

    private class DossierLoadingTask extends LoadingTask {

        public DossierLoadingTask(Activity context, DataChangeListener listener) {
            super(context, listener);
        }

        @Override
        protected void load(String... params) throws IParapheurException
        {
            // Check if this task is cancelled as often as possible.
            if (isCancelled()) {return;}
            //Log.d("debug", "getting dossier details");
            if (!IParapheur.OFFLINE) {
                Dossier d = RESTClient.INSTANCE.getDossier(bureauId, dossier.getId());
                if (dossier != null) {
                    dossier.saveDetails(d);
                }
            }
            else {
                dossier.addDocument(new Document(
                        UUID.randomUUID().toString(),
                        "ducument par defaut",
                        -1, ""));
            }
            if (isCancelled()) {return;}
            if (!IParapheur.OFFLINE) {
                dossier.setCircuit(RESTClient.INSTANCE.getCircuit(dossier.getId()));
            }
            if (isCancelled()) {return;}

            if (isReaderEnabled && (dossier.getMainDocuments().size() > 0)) {
                Document document = dossier.getMainDocuments().get(0);

                if ((document.getPath() == null) || !(new File(document.getPath()).exists()))
                {
                    File file = FileUtils.getFileForDocument(dossier.getId(), document.getId());
                    String path = file.getAbsolutePath();
                    //Log.d("debug", "saving document on disk");
                    if (!IParapheur.OFFLINE) {
                        if (RESTClient.INSTANCE.downloadFile(dossier.getMainDocuments().get(0).getUrl(), path)) {
                            document.setPath(path);
                        }
                    }
                    else {
                        document.setPath(path);
                        if (file.exists()) {
                            //Log.d("debug", "Document par defaut trouvé");
                        }
                        else {
                            //Log.d("debug", "Document par defaut non trouvé");
                        }
                    }
                }
            }
        }
    }

}
