package org.adullact.iparapheur.controller.dossier;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
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

import com.artifex.customannotations.CustomAnnotation;
import com.artifex.mupdfdemo.MuPDFFragment;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.circuit.CircuitAdapter;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;


/**
 * A fragment representing a single Dossier detail screen.
 * This fragment is contained in a {@link MainActivity}.
 */
public class DossierDetailFragment extends MuPDFFragment implements LoadingTask.DataChangeListener, SeekBar.OnSeekBarChangeListener {

	public static final String LOG_TAG = "DossierDetailFragment";
	public static final String FRAGMENT_TAG = "dossier_details_fragment";
	public static final String DOSSIER = "dossier";
	public static final String BUREAU_ID = "bureau_id";

	private String mBureauId;                // The Bureau where the dossier belongs.
	private Dossier mDossier;                // The Dossier this fragment is presenting.
	private String mDocumentId;              // The Document this fragment is presenting.
	private int mCurrentPage;
	private boolean mShouldReload = false;

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

//		if (getArguments() != null) {
//			mBureauId = getArguments().getString(BUREAU_ID);
//			mDossier = getArguments().getParcelable(DOSSIER);
//		}
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Set listeners

		if (getView() != null) {

			getView().findViewById(R.id.mupdffragment_main_fabbutton).setVisibility(View.GONE);
			getView().findViewById(R.id.mupdffragment_main_fabbutton_annotation).setOnClickListener(
					new View.OnClickListener() {
						@Override public void onClick(View v) {
							((FloatingActionsMenu) getView().findViewById(R.id.mupdffragment_main_fabbutton)).collapse();
							startCreateAnnotationOnNextMove(true);
						}
					}
			);
			getView().findViewById(R.id.mupdffragment_main_fabbutton_validate).setOnClickListener(
					new View.OnClickListener() {
						@Override public void onClick(View v) {
							((FloatingActionsMenu) getView().findViewById(R.id.mupdffragment_main_fabbutton)).collapse();
							((DossierDetailsFragmentListener) getActivity()).onValidateButtonClicked(mDossier, mBureauId);
						}
					}
			);
			;
			getView().findViewById(R.id.mupdffragment_main_fabbutton_cancel).setOnClickListener(
					new View.OnClickListener() {
						@Override public void onClick(View v) {
							((FloatingActionsMenu) getView().findViewById(R.id.mupdffragment_main_fabbutton)).collapse();
							((DossierDetailsFragmentListener) getActivity()).onCancelButtonClicked(mDossier, mBureauId);
						}
					}
			);
		}

		//

//		isReaderEnabled = true;
//		/*this.seekBar = (SeekBar) view.findViewById(R.id.fragment_dossier_detail_seekbar);
//		seekBar.setVisibility(View.INVISIBLE);
//        seekBar.setOnSeekBarChangeListener(this);*/
//		mViewPager = (ViewPager) view.findViewById(R.id.fragment_dossier_detail_pager);
//		mLoadingSpinner = view.findViewById(android.R.id.progress);

		// Reload data after rotation

		if (savedInstanceState != null)
			mShouldReload = true;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

//		String state = Environment.getExternalStorageState();
//		if (!Environment.MEDIA_MOUNTED.equals(state)) {
//			Toast.makeText(getActivity(), R.string.media_not_mounted, Toast.LENGTH_LONG).show();
//			isReaderEnabled = false;
//		}

		if (mDossier != null)
			getDossierDetails(false);

		setHasOptionsMenu(true);
	}

	@Override public void onStart() {
		super.onStart();

		if (mShouldReload) {
			mShouldReload = false;
			update(mDossier, mBureauId);
		}
	}

	// </editor-fold desc="LifeCycle">

	// <editor-fold desc="ActionBar">

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.dossier_details_menu, menu);
	}

	@Override public void onPrepareOptionsMenu(Menu menu) {

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
				documentSelectorSubMenu.add(
						Menu.NONE, R.id.action_document_selected, 0, mainDocument.getName()
				).setIcon(R.drawable.ic_description_black_24dp);

			for (Document annexe : mDossier.getAnnexes())
				documentSelectorSubMenu.add(
						Menu.NONE, R.id.action_document_selected, 0, annexe.getName()
				).setIcon(R.drawable.ic_attachment_black_24dp);
		}

		//

		super.onPrepareOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {

		// Handle presses on the action bar items

		switch (item.getItemId()) {

			case R.id.action_details:
				((DossierDetailsFragmentListener) getActivity()).toggleInfoDrawer();
				return true;

			case R.id.action_document_selected:
				String name = String.valueOf(item.getTitle());
				String documentId = findDocumentId(mDossier, name);

				if (!TextUtils.isEmpty(documentId))
					if (!TextUtils.equals(mDocumentId, documentId))
						update(mDossier, mBureauId, documentId);

				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// </editor-fold desc="ActionBar">

	// <editor-fold desc="MuPdfFragment">

	@Override public void showProgressLayout() {

		if (getView() != null)
			getView().findViewById(R.id.mupdffragment_main_fabbutton).setVisibility(View.GONE);

		super.showProgressLayout();

	}

	@Override public void showContentLayout() {

		if (getView() != null)
			getView().findViewById(R.id.mupdffragment_main_fabbutton).setVisibility(View.VISIBLE);

		super.showContentLayout();
	}

	@Override public void showErrorLayout() {

		if (getView() != null)
			getView().findViewById(R.id.mupdffragment_main_fabbutton).setVisibility(View.GONE);

		super.showErrorLayout();
	}

	// </editor-fold desc="MuPdfFragment">

	public void update(@Nullable Dossier dossier, @NonNull String bureauId) {
		update(dossier, bureauId, null);
	}

	public void update(@Nullable Dossier dossier, @NonNull String bureauId, @Nullable String documentId) {

		mBureauId = bureauId;
		mDossier = dossier;
		mDocumentId = documentId;

		((DossierDetailsFragmentListener) getActivity()).lockInfoDrawer(true);

		if ((dossier != null) && (!TextUtils.isEmpty(dossier.getId()))) {
			showProgressLayout();
			getDossierDetails(false);
		}
		else {
			updateReader();
		}
	}

	private void getDossierDetails(boolean forceReload) {

		// To force reload dossier details, just delete its main document path (on local storage).

		if (forceReload)
			mDossier.clearDetails();

		// Download information only if details aren't already available

		new DossierLoadingAsyncTask().execute();
	}

	@Override public void onDataChanged() {

		if (!DeviceUtils.isDebugOffline())
			updateCircuitInfoDrawerContent();

		updateReader();
	}

	private void updateReader() {
		//Adrien - TODO - Error messages

		final Document document = Dossier.findCurrentDocument(mDossier, mDocumentId);
		Log.d(LOG_TAG, "document ? " + (document == null));
		if (document == null)
			return;

		File documentFile = FileUtils.getFileForDocument(getActivity(), mDossier.getId(), document.getId());
		Log.d(LOG_TAG, "documentFile ? " + ((documentFile == null) || (!documentFile.exists())));
		if ((documentFile == null) || (!documentFile.exists()))
			return;

		Log.d(LOG_TAG, "path   = " + documentFile.getAbsolutePath());
		openFile(documentFile.getAbsolutePath());

		SparseArray<HashMap<String, CustomAnnotation>> muPdfCustomAnnotations = parapheurToMuPdfAnnotations(document.getPagesAnnotations());
		updateCustomAnnotations(muPdfCustomAnnotations);

		//

//		if (document != null) {
//			if (isReaderEnabled && (document.getPath() != null)) {
//				mViewPager.post(
//						new Runnable() {
//							@Override public void run() {
//								try {
//									((DocumentPagerAdapter) mViewPager.getAdapter()).setDocument(document);
//									mViewPager.setCurrentItem(0, false);
//									ViewUtils.crossfade(getActivity(), mViewPager, mLoadingSpinner);
//								}
//								catch (Exception e) {
//									e.printStackTrace();
//									Toast.makeText(getActivity(), R.string.error_reading_document, Toast.LENGTH_LONG).show();
//								}
//							}
//						}
//				);
//			}
//		}
//		else {
//			mViewPager.post(
//					new Runnable() {
//						@Override public void run() {
//							try {
//								((DocumentPagerAdapter) mViewPager.getAdapter()).setDocument(null);
//								//mViewPager.setCurrentItem(0, false);
//							}
//							catch (Exception e) {
//								//e.printStackTrace();
//								Toast.makeText(getActivity(), R.string.error_reading_document, Toast.LENGTH_LONG).show();
//							}
//						}
//					}
//			);
//		}
	}

	private void updateCircuitInfoDrawerContent() {

		View infoLayout = getActivity().findViewById(R.id.activity_dossiers_right_drawer);

		// Default case (this should not happen !)

		if (infoLayout == null)
			return;

		// Updating info

		ListView circuitView = (ListView) infoLayout.findViewById(R.id.fragment_dossier_detail_circuit);
		TextView title = (TextView) infoLayout.findViewById(R.id.fragment_dossier_detail_title);
		TextView typology = (TextView) infoLayout.findViewById(R.id.fragment_dossier_detail_typologie);

		title.setText(mDossier.getName());
		String typeString = mDossier.getType() + " / " + mDossier.getSousType();
		typology.setText(typeString);
		circuitView.setAdapter(new CircuitAdapter(getActivity(), mDossier.getCircuit().getEtapeCircuitList()));

		((DossierDetailsFragmentListener) getActivity()).lockInfoDrawer(false);
		getActivity().invalidateOptionsMenu();
	}

	private static @NonNull SparseArray<HashMap<String, CustomAnnotation>> parapheurToMuPdfAnnotations(SparseArray<PageAnnotations> parapheurAnnotations) {
		SparseArray<HashMap<String, CustomAnnotation>> result = new SparseArray<>();

		for (int i = 0; i < parapheurAnnotations.size(); i++) {

			HashMap<String, CustomAnnotation> annotationMap = new HashMap<>();
			int pageIndex = parapheurAnnotations.keyAt(i);
			PageAnnotations pageAnnotation = parapheurAnnotations.get(pageIndex);

			for (Annotation annotation : pageAnnotation.getAnnotations()) {
				annotationMap.put(
						annotation.getUuid(), new CustomAnnotation(
								annotation.getUuid(),
								DeviceUtils.translateDpiRect(annotation.getRect(), 150, 144),
								annotation.getText(),
								"",
								annotation.getAuthor()
						)
				);
			}

			result.put(pageIndex, annotationMap);
		}

		return result;
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

	@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

	@Override protected void onAnnotationChanged(@NonNull String annotationId) {
		Log.i("Adrien", "annotation changed " + annotationId);

	}

	// <editor-fold desc="SeekBar Listener">

	@Override public void onStartTrackingTouch(SeekBar seekBar) { }

	@Override public void onStopTrackingTouch(SeekBar seekBar) {
		if (mCurrentPage != seekBar.getProgress()) {
			mCurrentPage = seekBar.getProgress();
			//reader.setDisplayedViewIndex(mCurrentPage);
		}
	}

	// </editor-fold desc="SeekBar Listener">

	// <editor-fold desc="DossierDetailsFragmentListener">

	public interface DossierDetailsFragmentListener {

		void toggleInfoDrawer();

		void lockInfoDrawer(boolean lock);

		void onValidateButtonClicked(@NonNull Dossier dossier, @NonNull String bureauId);

		void onCancelButtonClicked(@NonNull Dossier dossier, @NonNull String bureauId);
	}

	// </editor-fold desc="DossierDetailsFragmentListener">

	private class DossierLoadingAsyncTask extends AsyncTask<Void, Void, Void> {

		private void showSpinnerOnUiThread() {
			getActivity().runOnUiThread(
					new Runnable() {
						@Override public void run() {
							showProgressLayout();
						}
					}
			);
		}

		// TODO : Error messages
		@Override protected Void doInBackground(Void... params) {

			// Default cases

			if (mDossier == null)
				return null;

			// Download the dossier Metadata (if missing, and according to the debug mode)

			if (DeviceUtils.isDebugOffline()) {
				mDossier.addDocument(new Document(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "document par défaut", -1, "", false, true));
			}
			else if (!mDossier.isDetailsAvailable()) {
				showSpinnerOnUiThread();

				try {
					mDossier.saveDetails(RESTClient.INSTANCE.getDossier(mBureauId, mDossier.getId()));
					mDossier.setCircuit(RESTClient.INSTANCE.getCircuit(mDossier.getId()));
				}
				catch (IParapheurException e) { e.printStackTrace(); }
			}

			// Getting metadata

			Document currentDocument = Dossier.findCurrentDocument(mDossier, mDocumentId);
			if (currentDocument == null) {
				Log.e("Adrien", "current document null !?!");
				return null;
			}

			showSpinnerOnUiThread();
			File file = FileUtils.getFileForDocument(getActivity(), mDossier.getId(), currentDocument.getId());
			currentDocument.setPath(file.getAbsolutePath());

			if (!file.exists()) {
				try { RESTClient.INSTANCE.downloadFile(currentDocument.getUrl(), file.getAbsolutePath()); }
				catch (IParapheurException e) { e.printStackTrace(); }
			}

			String dossierId = mDossier.getId();

			SparseArray<PageAnnotations> annotations = new SparseArray<>();
			try { RESTClient.INSTANCE.getAnnotations(dossierId, currentDocument.getId()); }
			catch (IParapheurException e) { e.printStackTrace(); }
			currentDocument.setPagesAnnotations(annotations);

			return null;
		}

		@Override protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			updateReader();
			updateCircuitInfoDrawerContent();
			showContentLayout();
		}
	}

}
