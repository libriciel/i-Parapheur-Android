package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
import org.adullact.iparapheur.controller.IParapheurApplication;
import org.adullact.iparapheur.controller.circuit.CircuitAdapter;
import org.adullact.iparapheur.controller.document.DocumentPagerAdapter;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.StringUtils;

import java.io.File;
import java.util.UUID;

/**
 * A fragment representing a single Dossier detail screen.
 * This fragment is contained in a {@link MainActivity}.
 */
public class DossierDetailFragment extends Fragment implements LoadingTask.DataChangeListener, SeekBar.OnSeekBarChangeListener {

	public static final String TAG = "Dossier_details";
	public static final String DOSSIER = "dossier";
	public static final String BUREAU_ID = "bureau_id";

	private String bureauId; // The Bureau where the dossier belongs.
	private Dossier dossier; // The Dossier this fragment is presenting.
	private boolean isReaderEnabled;
	private ViewPager viewPager; // Used to display the document's pages. Each page is managed by a fragment.
	private int currentPage;
	private boolean shouldReload = false;

	public DossierDetailFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		if (getArguments() != null) {
			bureauId = getArguments().getString(BUREAU_ID);
			dossier = getArguments().getParcelable(DOSSIER);
		}
		currentPage = 0;
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

		// Reload data after rotation

		if (savedInstanceState != null)
			shouldReload = true;
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
		if (dossier != null) {
			getDossierDetails(false);
		}
		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (shouldReload) {
			shouldReload = false;
			update(dossier, bureauId);
		}
	}

	// <editor-fold desc="ActionBar">

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.dossier_details_menu, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
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

	// </editor-fold desc="ActionBar">

	public void update(Dossier dossier, String bureauId) {
		this.bureauId = bureauId;
		this.dossier = dossier;

		closeDetails();

		if (dossier != null && dossier.getId() != null)
			getDossierDetails(false);
		else
			updateReader();
	}

	private void getDossierDetails(boolean forceReload) {

		// To force reload dossier details, just delete its main document path (on local storage).
		if (forceReload)
			dossier.clearDetails();

		// Download information only if details aren't already available
		if (!dossier.isDetailsAvailable())
			new DossierLoadingTask(getActivity(), this).execute();
		else
			onDataChanged();
	}

	@Override
	public void onDataChanged() {

		if (!IParapheurApplication.OFFLINE)
			updateDetails();

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
							viewPager.setCurrentItem(0, false);
						}
						catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(getActivity(), R.string.error_reading_document, Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		}
		else {
			this.viewPager.post(new Runnable() {
				@Override
				public void run() {
					try {
						((DocumentPagerAdapter) viewPager.getAdapter()).setDocument(null);
						//viewPager.setCurrentItem(0, false);
					}
					catch (Exception e) {
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

	private void toggleDetails() {
		if (getView() == null)
			return;

		View details = getView().findViewById(R.id.fragment_dossier_detail_details);

		if (details.getVisibility() == View.VISIBLE) {
			details.setVisibility(View.INVISIBLE);
		}
		else if ((dossier != null) && dossier.isDetailsAvailable()) {
			details.setVisibility(View.VISIBLE);
		}
	}

	private void closeDetails() {
		if (getView() == null)
			return;

		View details = getView().findViewById(R.id.fragment_dossier_detail_details);

		if ((details != null) && (details.getVisibility() == View.VISIBLE)) {
			details.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

	// <editor-fold desc="SeekBar Listener">

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (currentPage != seekBar.getProgress()) {
			currentPage = seekBar.getProgress();
			//reader.setDisplayedViewIndex(currentPage);
		}
	}

	// </editor-fold desc="SeekBar Listener">

	private class DossierLoadingTask extends LoadingTask {

		public DossierLoadingTask(Activity context, DataChangeListener listener) {
			super(context, listener);
		}

		@Override
		protected void load(String... params) throws IParapheurException {
			// Check if this task is cancelled as often as possible.
			if (isCancelled())
				return;

			//Log.d("debug", "getting dossier details");
			if (!IParapheurApplication.OFFLINE) {
				Dossier d = RESTClient.INSTANCE.getDossier(bureauId, dossier.getId());
				if (dossier != null) {
					dossier.saveDetails(d);
				}
			}
			else {
				dossier.addDocument(new Document(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "document par défaut", -1, ""));
			}
			if (isCancelled()) {
				return;
			}
			if (!IParapheurApplication.OFFLINE) {
				dossier.setCircuit(RESTClient.INSTANCE.getCircuit(dossier.getId()));
			}
			if (isCancelled()) {
				return;
			}

			if (isReaderEnabled && (dossier.getMainDocuments().size() > 0)) {
				Document document = dossier.getMainDocuments().get(0);

				if ((document.getPath() == null) || !(new File(document.getPath()).exists())) {
					File file = FileUtils.getFileForDocument(getActivity(), dossier.getId(), document.getId());
					String path = file.getAbsolutePath();
					//Log.d("debug", "saving document on disk");
					if (!IParapheurApplication.OFFLINE) {
						if (RESTClient.INSTANCE.downloadFile(dossier.getMainDocuments().get(0).getUrl(), path)) {
							document.setPath(path);
							document.setPagesAnnotations(RESTClient.INSTANCE.getAnnotations(dossier != null ? dossier.getId() : null));
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
