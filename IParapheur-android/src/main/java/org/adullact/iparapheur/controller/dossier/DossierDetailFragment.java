package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import org.adullact.iparapheur.controller.circuit.CircuitAdapter;
import org.adullact.iparapheur.controller.document.DocumentPagerAdapter;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;

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

	private String mBureauId;                // The Bureau where the dossier belongs.
	private Dossier mDossier;                // The Dossier this fragment is presenting.
	private boolean isReaderEnabled;
	private ViewPager mViewPager;            // Used to display the document's pages. Each page is managed by a fragment.
	private int mCurrentPage;
	private boolean mShouldReload = false;

	public DossierDetailFragment() { }

	// <editor-fold desc="LifeCycle">

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		if (getArguments() != null) {
			mBureauId = getArguments().getString(BUREAU_ID);
			mDossier = getArguments().getParcelable(DOSSIER);
		}
		mCurrentPage = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_dossier_detail, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		isReaderEnabled = true;
		/*this.seekBar = (SeekBar) view.findViewById(R.id.fragment_dossier_detail_seekbar);
		seekBar.setVisibility(View.INVISIBLE);
        seekBar.setOnSeekBarChangeListener(this);*/
		mViewPager = (ViewPager) view.findViewById(R.id.fragment_dossier_detail_pager);

		// Reload data after rotation

		if (savedInstanceState != null)
			mShouldReload = true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mViewPager.post(new Runnable() {
			public void run() {
				mViewPager.setAdapter(new DocumentPagerAdapter(getActivity(), getActivity().getSupportFragmentManager()));
			}
		});
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Toast.makeText(getActivity(), R.string.media_not_mounted, Toast.LENGTH_LONG).show();
			isReaderEnabled = false;
		}
		if (mDossier != null) {
			getDossierDetails(false);
		}
		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mShouldReload) {
			mShouldReload = false;
			update(mDossier, mBureauId);
		}
	}

	// </editor-fold desc="LifeCycle">

	// <editor-fold desc="ActionBar">

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.dossier_details_menu, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.action_details);
		item.setVisible((mDossier != null) && mDossier.isDetailsAvailable());
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

	public void update(@Nullable Dossier dossier, @NonNull String bureauId) {
		mBureauId = bureauId;
		mDossier = dossier;

		closeDetails();

		if ((dossier != null) && dossier.getId() != null)
			getDossierDetails(false);
		else
			updateReader();
	}

	private void getDossierDetails(boolean forceReload) {

		// To force reload dossier details, just delete its main document path (on local storage).
		if (forceReload)
			mDossier.clearDetails();

		// Download information only if details aren't already available
		if (!mDossier.isDetailsAvailable())
			new DossierLoadingTask(getActivity(), this).execute();
		else
			onDataChanged();
	}

	@Override
	public void onDataChanged() {

		if (!DeviceUtils.isDebugOffline(getActivity()))
			updateDetails();

		updateReader();
	}

	// TODO : aucun apercu disponible
	private void updateReader() {
		if (mDossier != null && !mDossier.getMainDocuments().isEmpty()) {
			final Document document = mDossier.getMainDocuments().get(0);
			if (isReaderEnabled && (document.getPath() != null)) {
				mViewPager.post(new Runnable() {
					@Override
					public void run() {
						try {
							((DocumentPagerAdapter) mViewPager.getAdapter()).setDocument(document);
							mViewPager.setCurrentItem(0, false);
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
			mViewPager.post(new Runnable() {
				@Override
				public void run() {
					try {
						((DocumentPagerAdapter) mViewPager.getAdapter()).setDocument(null);
						//mViewPager.setCurrentItem(0, false);
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
			((TextView) getView().findViewById(R.id.fragment_dossier_detail_title)).setText(mDossier.getName());
			((TextView) getView().findViewById(R.id.fragment_dossier_detail_typologie)).setText(mDossier.getType() + " / " + mDossier.getSousType());
			ListView circuitView = (ListView) getView().findViewById(R.id.fragment_dossier_detail_circuit);
			circuitView.setAdapter(new CircuitAdapter(getActivity(), mDossier.getCircuit()));
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
		else if ((mDossier != null) && mDossier.isDetailsAvailable()) {
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
		if (mCurrentPage != seekBar.getProgress()) {
			mCurrentPage = seekBar.getProgress();
			//reader.setDisplayedViewIndex(mCurrentPage);
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
			if (!DeviceUtils.isDebugOffline(getActivity())) {
				if (mDossier != null) {
					Dossier d = RESTClient.INSTANCE.getDossier(mBureauId, mDossier.getId());
					mDossier.saveDetails(d);
				}
			}
			else {
				mDossier.addDocument(new Document(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "document par défaut", -1, ""));
			}
			if (isCancelled()) {
				return;
			}
			if (!DeviceUtils.isDebugOffline(getActivity()) && (mDossier != null)) {
				mDossier.setCircuit(RESTClient.INSTANCE.getCircuit(mDossier.getId()));
			}
			if (isCancelled()) {
				return;
			}

			if (isReaderEnabled && (mDossier != null) && (mDossier.getMainDocuments() != null) && (mDossier.getMainDocuments().size() > 0)) {
				Document document = mDossier.getMainDocuments().get(0);

				if ((document.getPath() == null) || !(new File(document.getPath()).exists())) {
					File file = FileUtils.getFileForDocument(getActivity(), mDossier.getId(), document.getId());
					String path = file.getAbsolutePath();
					//Log.d("debug", "saving document on disk");
					if (!DeviceUtils.isDebugOffline(getActivity())) {
						if (RESTClient.INSTANCE.downloadFile(mDossier.getMainDocuments().get(0).getUrl(), path)) {
							document.setPath(path);
							document.setPagesAnnotations(RESTClient.INSTANCE.getAnnotations(mDossier != null ? mDossier.getId() : null));
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
