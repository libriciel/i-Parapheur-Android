package org.adullact.iparapheur.controller.dossier;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
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
import org.adullact.iparapheur.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
	private String mDocumentId;              // The Document this fragment is presenting.
	private boolean isReaderEnabled;
	private int mCurrentPage;
	private boolean mShouldReload = false;

	private ViewPager mViewPager;            // Used to display the document's pages. Each page is managed by a fragment.
	private View mLoadingSpinner;

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
		mLoadingSpinner = view.findViewById(android.R.id.progress);

		// Reload data after rotation

		if (savedInstanceState != null)
			mShouldReload = true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mViewPager.post(
				new Runnable() {
					public void run() {
						mViewPager.setAdapter(new DocumentPagerAdapter(getActivity(), getActivity().getSupportFragmentManager()));
					}
				}
		);

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

		// Info item

		MenuItem infoItem = menu.findItem(R.id.action_details);
		infoItem.setVisible((mDossier != null) && mDossier.isDetailsAvailable());

		// Document selector

		MenuItem documentSelectorItem = menu.findItem(R.id.action_document_selection);
		boolean hasMultipleDoc = (mDossier != null) && ((mDossier.getMainDocuments().size() > 1) || (!mDossier.getAnnexes().isEmpty()));
		documentSelectorItem.setVisible(hasMultipleDoc);

		if (hasMultipleDoc) {
			SubMenu documentSelectorSubMenu = documentSelectorItem.getSubMenu();
			documentSelectorSubMenu.clear();

			for (Document mainDocument : mDossier.getMainDocuments())
				documentSelectorSubMenu.add(Menu.NONE, R.id.action_document_selected, 0, mainDocument.getName());

			for (Document annexe : mDossier.getAnnexes())
				documentSelectorSubMenu.add(Menu.NONE, R.id.action_document_selected, 0, annexe.getName());
		}

		//

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle presses on the action bar items

		switch (item.getItemId()) {

			case R.id.action_details:
				toggleDetails();
				return true;

			case R.id.action_document_selected:
				String name = String.valueOf(item.getTitle());
				String documentId = findDocumentId(mDossier, name);

				if (!TextUtils.isEmpty(documentId))
					if (!TextUtils.equals(mDocumentId, documentId))
						update(mDossier, documentId);

				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// </editor-fold desc="ActionBar">

	public void update(@Nullable Dossier dossier, @NonNull String bureauId) {
		update(dossier, bureauId, null);
	}

	public void update(@Nullable Dossier dossier, @NonNull String bureauId, @Nullable String documentId) {

		mBureauId = bureauId;
		mDossier = dossier;
		mDocumentId = documentId;

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

		Log.v("Adrien", "before : " + mDossier.getMainDocuments().size() + " " + mDossier.getAnnexes().size());

		// Download information only if details aren't already available
		if (!mDossier.isDetailsAvailable())
			new DossierLoadingTask(getActivity(), this).execute();
		else
			onDataChanged();
	}

	private @Nullable String findDocumentId(@Nullable Dossier dossier, @Nullable String documentName) {

		if (dossier == null)
			return null;

		for (Document mainDocument : mDossier.getMainDocuments())
			if (TextUtils.equals(documentName, mainDocument.getName()))
				return mainDocument.getId();

		for (Document annexes : mDossier.getAnnexes())
			if (TextUtils.equals(documentName, annexes.getName()))
				return annexes.getId();

		return null;
	}

	@Override
	public void onDataChanged() {

		if (!DeviceUtils.isDebugOffline())
			updateDetails();

		updateReader();
	}

	// TODO : aucun apercu disponible
	private void updateReader() {

		if (mDossier != null && !mDossier.getMainDocuments().isEmpty()) {
			final Document document = mDossier.getMainDocuments().get(0);
			if (isReaderEnabled && (document.getPath() != null)) {
				mViewPager.post(
						new Runnable() {
							@Override
							public void run() {
								try {
									((DocumentPagerAdapter) mViewPager.getAdapter()).setDocument(document);
									mViewPager.setCurrentItem(0, false);
									ViewUtils.crossfade(getActivity(), mViewPager, mLoadingSpinner);
								}
								catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(getActivity(), R.string.error_reading_document, Toast.LENGTH_LONG).show();
								}
							}
						}
				);
			}
		}
		else {
			mViewPager.post(
					new Runnable() {
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
					}
			);
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

	private @Nullable Document findCurrentDocument(@NonNull Dossier dossier, @Nullable String documentId) {

		List<Document> documents = new ArrayList<>();
		documents.addAll(dossier.getMainDocuments());
		documents.addAll(dossier.getAnnexes());

		if (!TextUtils.isEmpty(documentId))
			for (Document document : documents)
				if (TextUtils.equals(document.getId(), documentId))
					return document;

		return dossier.getMainDocuments().get(0);
	}

	public void showSpinner() {
		ViewUtils.crossfade(getActivity(), mLoadingSpinner, mViewPager);
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

			// Default cases

			if (mDossier == null)
				return;

			if (isCancelled())
				return;

			//

			if (!DeviceUtils.isDebugOffline())
				mDossier.saveDetails(RESTClient.INSTANCE.getDossier(mBureauId, mDossier.getId()));
			else
				mDossier.addDocument(new Document(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "document par défaut", -1, "", false, true));

			if (isCancelled())
				return;

			if (!DeviceUtils.isDebugOffline())
				mDossier.setCircuit(RESTClient.INSTANCE.getCircuit(mDossier.getId()));

			if (isCancelled())
				return;

			Document currentDocument = findCurrentDocument(mDossier, mDocumentId);

			if (isReaderEnabled && (currentDocument != null)) {

				if ((currentDocument.getPath() == null) || !(new File(currentDocument.getPath()).exists())) {
					File file = FileUtils.getFileForDocument(getActivity(), mDossier.getId(), currentDocument.getId());
					String path = file.getAbsolutePath();

					if (!DeviceUtils.isDebugOffline()) {
						if (RESTClient.INSTANCE.downloadFile(mDossier.getMainDocuments().get(0).getUrl(), path)) {
							currentDocument.setPath(path);

							List<Document> documents = mDossier.getMainDocuments();
							String documentId = "";
							if ((documents != null) && (!documents.isEmpty()) && (documents.get(0) != null))
								documentId = mDossier.getMainDocuments().get(0).getId();

							String dossierId = mDossier.getId();
							currentDocument.setPagesAnnotations(RESTClient.INSTANCE.getAnnotations(dossierId, documentId));
						}
					}
					else {
						currentDocument.setPath(path);
						Log.d("debug", file.exists() ? "Document par defaut trouvé" : "Document par defaut non trouvé");
					}
				}
			}

		}
	}

}
