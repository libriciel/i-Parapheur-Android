/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.controller.dossier;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.MainActivity;
import org.adullact.iparapheur.controller.account.MyAccounts;
import org.adullact.iparapheur.controller.circuit.CircuitAdapter;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import coop.adullactprojet.mupdffragment.MuPDFFragment;
import coop.adullactprojet.mupdffragment.stickynotes.StickyNote;
import coop.adullactprojet.mupdffragment.utils.ViewUtils;


/**
 * A fragment representing a single Dossier detail screen.
 * This fragment is contained in a {@link MainActivity}.
 */
public class DossierDetailFragment extends MuPDFFragment implements LoadingTask.DataChangeListener, SeekBar.OnSeekBarChangeListener {

	// public static final String LOG_TAG = "DossierDetailFragment";
	public static final String FRAGMENT_TAG = "dossier_details_fragment";

	private static final String ANNOTATION_PAYLOAD_STEP = "step";
	private static final String ANNOTATION_PAYLOAD_TYPE = "type";
	private static final String ANNOTATION_PAYLOAD_IS_SECRETAIRE = "is_secretaire";

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

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Set listeners

		if (getView() != null) {

			getView().findViewById(R.id.mupdffragment_main_fabbutton).setVisibility(View.GONE);

			getView().findViewById(R.id.mupdffragment_main_fabbutton_annotation).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					((FloatingActionsMenu) getView().findViewById(R.id.mupdffragment_main_fabbutton)).collapse();
					startCreateStickyNoteOnNextMove(true);
				}
			});

			getView().findViewById(R.id.mupdffragment_main_fabbutton_validate).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					((FloatingActionsMenu) getView().findViewById(R.id.mupdffragment_main_fabbutton)).collapse();

					Action positiveAction = getPositiveAction(mDossier);
					if (positiveAction != null)
						((DossierDetailsFragmentListener) getActivity()).onActionButtonClicked(mDossier, mBureauId, positiveAction);
				}
			});

			getView().findViewById(R.id.mupdffragment_main_fabbutton_cancel).setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					((FloatingActionsMenu) getView().findViewById(R.id.mupdffragment_main_fabbutton)).collapse();

					Action negativeAction = getNegativeAction(mDossier);
					if (negativeAction != null)
						((DossierDetailsFragmentListener) getActivity()).onActionButtonClicked(mDossier, mBureauId, negativeAction);
				}
			});
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

		Toolbar actions_toolbar = (Toolbar) getActivity().findViewById(R.id.actions_toolbar);

		if (actions_toolbar != null) {
			actions_toolbar.inflateMenu(R.menu.dossier_details_fragment_icons);
			actions_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
				@Override public boolean onMenuItemClick(MenuItem item) {
					return onOptionsItemSelected(item);
				}
			});
		}
	}

	@Override public void onPrepareOptionsMenu(Menu menu) {

		Toolbar actions_toolbar = (Toolbar) getActivity().findViewById(R.id.actions_toolbar);

		// Info item

		MenuItem infoItem = actions_toolbar.getMenu().findItem(R.id.action_details);
		infoItem.setVisible((mDossier != null) && mDossier.isDetailsAvailable());

		// Document selector

		MenuItem documentSelectorItem = actions_toolbar.getMenu().findItem(R.id.action_document_selection);
		boolean hasMultipleDoc = (mDossier != null) && ((mDossier.getMainDocuments().size() > 1) || (!mDossier.getAnnexes().isEmpty()));
		documentSelectorItem.setVisible(hasMultipleDoc);

		if (hasMultipleDoc) {
			SubMenu docSelectorSubMenu = documentSelectorItem.getSubMenu();
			docSelectorSubMenu.clear();

			for (Document mainDoc : mDossier.getMainDocuments())
				docSelectorSubMenu.add(Menu.NONE, R.id.action_document_selected, Menu.NONE, mainDoc.getName()).setIcon(R.drawable.ic_description_black_24dp);

			for (Document annexe : mDossier.getAnnexes())
				docSelectorSubMenu.add(Menu.NONE, R.id.action_document_selected, Menu.NONE, annexe.getName()).setIcon(R.drawable.ic_attachment_black_24dp);
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
				return getActivity().onOptionsItemSelected(item);
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

	@Override protected void onStickyNoteChanged(@NonNull final StickyNote stickyNote, boolean deleteInvoked) {

		final Annotation newStickyNote = muPdfStickyNoteToParapheurAnnotation(stickyNote);

		if (deleteInvoked)
			new DeleteAnnotationAsyncTask().execute(newStickyNote);
		else if (stickyNote.getId().startsWith("new_"))
			new CreateAnnotationAsyncTask().execute(newStickyNote);
		else
			new UpdateAnnotationAsyncTask().execute(newStickyNote);
	}

	@NonNull @Override protected String getStickyNoteAuthorName() {
		return MyAccounts.INSTANCE.getSelectedAccount().getLogin();
	}

	@NonNull @Override protected String generateNewStickyNoteId() {
		return "new_" + UUID.randomUUID();
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

		updateFab(dossier);
	}

	private void getDossierDetails(boolean forceReload) {

		// To force reload dossier details, just delete its main document path (on local storage).

		if (forceReload)
			mDossier.clearDetails();

		// Download information only if details aren't already available

		new DossierLoadingAsyncTask().execute();
	}

	private void updateReader() {
		//Adrien - TODO - Error messages

		final Document document = Dossier.findCurrentDocument(mDossier, mDocumentId);
		if (document == null)
			return;

		File documentFile = FileUtils.getFileForDocument(getActivity(), mDossier.getId(), document.getId());
		if (!documentFile.exists())
			return;

		openFile(documentFile.getAbsolutePath());

		SparseArray<HashMap<String, StickyNote>> muPdfStickyNotes = parapheurToMuPdfStickyNote(document.getPagesAnnotations());
		updateStickyNotes(muPdfStickyNotes);

		// Set FAB annotation button visibility

		boolean areAnnotationAvailable = document.isMainDocument();
		if (getView() != null)
			getView().findViewById(R.id.mupdffragment_main_fabbutton_annotation).setVisibility(areAnnotationAvailable ? View.VISIBLE : View.GONE);

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

		if (mDossier.getCircuit() != null) // FIXME Why NPE ???
			circuitView.setAdapter(new CircuitAdapter(getActivity(), mDossier.getCircuit().getEtapeCircuitList()));

		((DossierDetailsFragmentListener) getActivity()).lockInfoDrawer(false);
		getActivity().invalidateOptionsMenu();
	}

	private void updateFab(@Nullable Dossier dossier) {

		// Default cases

		if ((dossier == null) || (getView() == null))
			return;

		//

		FloatingActionButton positiveButton = (FloatingActionButton) getView().findViewById(R.id.mupdffragment_main_fabbutton_validate);
		FloatingActionButton negativeButton = (FloatingActionButton) getView().findViewById(R.id.mupdffragment_main_fabbutton_cancel);

		Action positiveAction = getPositiveAction(dossier);
		positiveButton.setVisibility((positiveAction != null) ? View.VISIBLE : View.GONE);
		positiveButton.setTitle(getString((positiveAction != null) ? positiveAction.getTitle() : R.string.action_non_implementee));

		Action negativeAction = getNegativeAction(dossier);
		negativeButton.setVisibility((negativeAction != null) ? View.VISIBLE : View.GONE);
		negativeButton.setTitle(getString((negativeAction != null) ? negativeAction.getTitle() : R.string.action_non_implementee));
	}

	private @NonNull Annotation muPdfStickyNoteToParapheurAnnotation(@NonNull StickyNote muPdfAnnotation) {

		return new Annotation(
				muPdfAnnotation.getId(),
				muPdfAnnotation.getAuthor(),
				getCurrentPage(),
				(boolean) CollectionUtils.opt(muPdfAnnotation.getPayload(), ANNOTATION_PAYLOAD_IS_SECRETAIRE, false),
				StringUtils.serializeToIso8601Date(muPdfAnnotation.getDate()),
				ViewUtils.translateDpiRect(muPdfAnnotation.getRect(), 144, 150),
				muPdfAnnotation.getText(),
				(String) CollectionUtils.opt(muPdfAnnotation.getPayload(), ANNOTATION_PAYLOAD_TYPE, "rect"),
				(int) CollectionUtils.opt(muPdfAnnotation.getPayload(), ANNOTATION_PAYLOAD_STEP, 0)
		);
	}

	private static @NonNull SparseArray<HashMap<String, StickyNote>> parapheurToMuPdfStickyNote(SparseArray<PageAnnotations> parapheurAnnotations) {
		SparseArray<HashMap<String, StickyNote>> result = new SparseArray<>();

		for (int i = 0; i < parapheurAnnotations.size(); i++) {

			HashMap<String, StickyNote> stickyNoteMap = new HashMap<>();
			int pageIndex = parapheurAnnotations.keyAt(i);
			PageAnnotations pageAnnotation = parapheurAnnotations.get(pageIndex);

			for (Annotation annotation : pageAnnotation.getAnnotations()) {

				// Payload, to ease irrelevants MuPdf lib data

				HashMap<String, Object> payload = new HashMap<>();
				payload.put(ANNOTATION_PAYLOAD_STEP, annotation.getStep());

				// Building final StickyNote object

				boolean isLocked = !TextUtils.equals(annotation.getAuthor(), MyAccounts.INSTANCE.getSelectedAccount().getUserName());

				stickyNoteMap.put(annotation.getUuid(), new StickyNote(
						annotation.getUuid(),
						ViewUtils.translateDpiRect(annotation.getRect(), 150, 144),
						annotation.getText(),
						annotation.getAuthor(),
						StringUtils.parseIso8601Date(annotation.getDate()),
						isLocked ? StickyNote.Color.BLUE_GREY : StickyNote.Color.BLUE,
						isLocked,
						payload
				));
			}

			result.put(pageIndex, stickyNoteMap);
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

	@Override public void onDataChanged() {

		if (!DeviceUtils.isDebugOffline())
			updateCircuitInfoDrawerContent();

		updateReader();
	}

	/**
	 * Returns the main negative {@link Action} available, by coherent priority.
	 */
	public static @Nullable Action getPositiveAction(@NonNull Dossier dossier) {

		HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));
		actions.retainAll(dossier.getActions());

		if (dossier.getActionDemandee() != null)
			return dossier.getActionDemandee();

		if (actions.contains(Action.SIGNATURE))
			return Action.SIGNATURE;
		else if (actions.contains(Action.VISA))
			return Action.VISA;
		else if (actions.contains(Action.ARCHIVAGE))
			return Action.ARCHIVAGE;
		else if (actions.contains(Action.MAILSEC))
			return Action.MAILSEC;
		else if (actions.contains(Action.TDT_ACTES))
			return Action.TDT_ACTES;
		else if (actions.contains(Action.TDT_HELIOS))
			return Action.TDT_HELIOS;
		else if (actions.contains(Action.TDT))
			return Action.TDT;

		return null;
	}

	/**
	 * Returns the main negative {@link Action} available, by coherent priority.
	 */
	public static @Nullable Action getNegativeAction(@NonNull Dossier dossier) {

		HashSet<Action> actions = new HashSet<>(Arrays.asList(Action.values()));
		actions.retainAll(dossier.getActions());

		if (actions.contains(Action.REJET))
			return Action.REJET;

		return null;
	}

	// <editor-fold desc="SeekBar Listener">

	@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

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

		void onActionButtonClicked(@NonNull Dossier dossier, @NonNull String bureauId, @NonNull Action action);
	}

	// </editor-fold desc="DossierDetailsFragmentListener">

	private class DossierLoadingAsyncTask extends AsyncTask<Void, Void, Void> {

		private void showSpinnerOnUiThread() {
			getActivity().runOnUiThread(new Runnable() {
				@Override public void run() {
					showProgressLayout();
				}
			});
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
			if (currentDocument == null)
				return null;

			showSpinnerOnUiThread();
			File file = FileUtils.getFileForDocument(getActivity(), mDossier.getId(), currentDocument.getId());
			currentDocument.setPath(file.getAbsolutePath());

			if (!file.exists()) {
				try { RESTClient.INSTANCE.downloadFile(currentDocument.getUrl(), file.getAbsolutePath()); }
				catch (IParapheurException e) { e.printStackTrace(); }
			}

			// Loading user data and annotations

			SparseArray<PageAnnotations> annotations = new SparseArray<>();
			Account currentAccount = MyAccounts.INSTANCE.getSelectedAccount();
			if (TextUtils.isEmpty(currentAccount.getUserName())) {
				try { RESTClient.INSTANCE.updateAccountInformations(currentAccount); }
				catch (IParapheurException e) { e.printStackTrace(); }
			}

			if (currentDocument.isMainDocument()) {
				try { annotations = RESTClient.INSTANCE.getAnnotations(mDossier.getId(), currentDocument.getId()); }
				catch (IParapheurException e) { e.printStackTrace(); }
			}
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

	private class CreateAnnotationAsyncTask extends AsyncTask<Annotation, Void, Boolean> {

		private String mNewId = null;
		private Annotation mCurrentAnnotation = null;

		@Override protected Boolean doInBackground(Annotation... params) {

			if (params.length < 1)
				return null;

			mCurrentAnnotation = params[0];

			try {
				mNewId = RESTClient.INSTANCE.createAnnotation(mDossier.getId(), mDocumentId, mCurrentAnnotation, getCurrentPage());
			}
			catch (IParapheurException e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		@Override protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);

			if ((!success) || TextUtils.isEmpty(mNewId)) {
				Toast.makeText(getActivity(), R.string.error_annotation_update, Toast.LENGTH_LONG).show();
			}
			else {
				updateStickyNoteData(mCurrentAnnotation.getUuid(), mNewId, null, null);
				mCurrentAnnotation.setUuid(mNewId);
			}
		}
	}

	private class UpdateAnnotationAsyncTask extends AsyncTask<Annotation, Void, Boolean> {

		@Override protected Boolean doInBackground(Annotation... params) {

			if (params.length < 1)
				return null;

			Annotation currentAnnotation = params[0];

			try {
				RESTClient.INSTANCE.updateAnnotation(mDossier.getId(), mDocumentId, currentAnnotation, getCurrentPage());
			}
			catch (IParapheurException e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		@Override protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);

			if (!success)
				Toast.makeText(getActivity(), R.string.error_annotation_update, Toast.LENGTH_LONG).show();
		}
	}

	private class DeleteAnnotationAsyncTask extends AsyncTask<Annotation, Void, Boolean> {

		@Override protected Boolean doInBackground(Annotation... params) {

			if (params.length < 1)
				return null;

			Annotation currentAnnotation = params[0];

			try {
				RESTClient.INSTANCE.deleteAnnotation(mDossier.getId(), mDocumentId, currentAnnotation.getUuid(), getCurrentPage());
			}
			catch (IParapheurException e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		@Override protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);

			if (!success)
				Toast.makeText(getActivity(), R.string.error_annotation_delete, Toast.LENGTH_LONG).show();
		}
	}
}
