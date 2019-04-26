/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.controller.dossier;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.ViewSwitcher;

import com.j256.ormlite.dao.Dao;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.MainActivity;
import org.adullact.iparapheur.controller.circuit.CircuitAdapter;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.database.DatabaseHelper;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.utils.AccountUtils;
import org.adullact.iparapheur.utils.ActionUtils;
import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.DeviceUtils;
import org.adullact.iparapheur.utils.DocumentUtils;
import org.adullact.iparapheur.utils.DossierUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.SerializableSparseArray;
import org.adullact.iparapheur.utils.StringUtils;
import org.adullact.iparapheur.utils.ViewUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import coop.adullactprojet.mupdffragment.MuPDFFragment;
import coop.adullactprojet.mupdffragment.stickynotes.StickyNote;


/**
 * A fragment representing a single Dossier detail screen.
 * This fragment is contained in a {@link MainActivity}.
 */
public class DossierDetailFragment extends MuPDFFragment implements LoadingTask.DataChangeListener, SeekBar.OnSeekBarChangeListener {

    public static final String FRAGMENT_TAG = "dossier_details_fragment";
    public static final String LOG_TAG = "DossierDetailFragment";

    private static final int FAB_ANIM_DELAY_IN_MS = 75;
    private static final String ANNOTATION_PAYLOAD_STEP = "step";
    private static final String ANNOTATION_PAYLOAD_TYPE = "type";
    private static final String ANNOTATION_PAYLOAD_IS_SECRETAIRE = "is_secretaire";

    private View mFabWhiteBackground;
    private ViewSwitcher mMainButtonViewSwitcher;
    private FloatingActionButton mMainMenuFab;
    private FloatingActionButton mCancelFab;
    private FloatingActionButton mAnnotationFab;
    private TextView mValidateLabelTextView;
    private TextView mCancelLabelTextView;
    private View mValidateLabel;
    private View mCancelLabel;
    private View mAnnotationLabel;

    private String mBureauId;                // The Bureau where the dossier belongs.
    private Dossier mDossier;                // The Dossier this fragment is presenting.
    private String mDocumentId;              // The Document this fragment is presenting.
    private boolean mIsAnnotable;
    private int mCurrentPage;
    private boolean mShouldReload = false;

    // <editor-fold desc="LifeCycle">

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve views

        mMainButtonViewSwitcher = (ViewSwitcher) view.findViewById(R.id.mupdf_main_fab_viewswitcher);
        mMainMenuFab = (FloatingActionButton) view.findViewById(R.id.mupdf_main_menu_fabbutton);
        mCancelFab = (FloatingActionButton) view.findViewById(R.id.mupdf_main_cancel_fabbutton);
        mAnnotationFab = (FloatingActionButton) view.findViewById(R.id.mupdf_main_annotation_fabbutton);
        mValidateLabelTextView = (TextView) view.findViewById(R.id.mupdf_main_fab_viewswitcher_label_textview);
        mCancelLabelTextView = (TextView) view.findViewById(R.id.mupdf_main_cancel_fabbutton_label_textview);
        mValidateLabel = view.findViewById(R.id.mupdf_main_fab_viewswitcher_label);
        mCancelLabel = view.findViewById(R.id.mupdf_main_cancel_fabbutton_label);
        mAnnotationLabel = view.findViewById(R.id.mupdf_main_annotation_fabbutton_label);
        mFabWhiteBackground = view.findViewById(R.id.mupdf_main_fabbutton_white_background);
        FloatingActionButton validateFab = (FloatingActionButton) view.findViewById(R.id.mupdf_main_validate_fabbutton);

        // Default state

        mCancelFab.setVisibility(View.GONE);
        mAnnotationFab.setVisibility(View.GONE);
        mValidateLabel.setVisibility(View.GONE);
        mCancelLabel.setVisibility(View.GONE);
        mAnnotationLabel.setVisibility(View.GONE);

        // Set listeners

        mFabWhiteBackground.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onWhiteBackgroundClicked();
            }
        });

        mMainMenuFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toggleFabMenuVisibility();
            }
        });

        mAnnotationFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onAnnotationSelected();
            }
        });
        mAnnotationLabel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onAnnotationSelected();
            }
        });

        validateFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onValidateSelected();
            }
        });
        mValidateLabel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onValidateSelected();
            }
        });

        mCancelFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onCancelSelected();
            }
        });
        mCancelLabel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onCancelSelected();
            }
        });

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
            update(mDossier, mBureauId, null);
        }
    }

    /**
     * Called manually from parent Activity.
     *
     * @return true if the event was consumed.
     */
    public final boolean onBackPressed() {

        if (mMainButtonViewSwitcher.getDisplayedChild() == 1) {
            collapseFabMenu();
            return true;
        }

        return false;
    }

    // </editor-fold desc="LifeCycle">

    // <editor-fold desc="ActionBar">

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        Toolbar actionsToolbar = (Toolbar) getActivity().findViewById(R.id.actions_toolbar);

        if (actionsToolbar != null) {
            actionsToolbar.inflateMenu(R.menu.dossier_details_fragment_icons);
            actionsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
        infoItem.setVisible((mDossier != null) && DossierUtils.areDetailsAvailable(mDossier));

        // Document selector

        MenuItem documentSelectorItem = actions_toolbar.getMenu().findItem(R.id.action_document_selection);
        boolean hasMultipleDoc = (mDossier != null) && ((mDossier.getDocumentList().size() > 1));
        documentSelectorItem.setVisible(hasMultipleDoc);

        if (hasMultipleDoc) {
            SubMenu docSelectorSubMenu = documentSelectorItem.getSubMenu();
            docSelectorSubMenu.clear();

            for (Document mainDoc : DossierUtils.getMainDocuments(mDossier))
                docSelectorSubMenu.add(Menu.NONE, R.id.action_document_selected, Menu.NONE, mainDoc.getName()).setIcon(R.drawable.ic_description_black_24dp);

            for (Document annexe : DossierUtils.getAnnexes(mDossier))
                docSelectorSubMenu.add(Menu.NONE, R.id.action_document_selected, Menu.NONE, annexe.getName()).setIcon(R.drawable.ic_attachment_black_24dp);
        }

        // Share

        MenuItem shareItem = actions_toolbar.getMenu().findItem(R.id.action_share);
        shareItem.setVisible((mDossier != null));

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

            case R.id.action_share:
                startShareIntent();
                return true;

            default:
                return getActivity().onOptionsItemSelected(item);
        }
    }

    // </editor-fold desc="ActionBar">

    // <editor-fold desc="MuPdfFragment">

    @Override protected boolean areGesturesLocked() {
        return ((DossierDetailsFragmentListener) getActivity()).isAnyDrawerOpened();
    }

    @Override public void showProgressLayout() {
        mMainMenuFab.hide();
        super.showProgressLayout();

    }

    @Override public void showContentLayout() {
        mMainButtonViewSwitcher.setVisibility(View.VISIBLE);
        mMainMenuFab.show();
        super.showContentLayout();
    }

    @Override public void showErrorLayout() {
        mMainMenuFab.hide();
        super.showErrorLayout();
    }

    @Override protected void onStickyNoteChanged(@NonNull final StickyNote stickyNote, boolean deleteInvoked) {

        if (!DeviceUtils.isConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.Action_unavailable_offline, Toast.LENGTH_LONG).show();
            return;
        }

        final Annotation newStickyNote = muPdfStickyNoteToParapheurAnnotation(stickyNote);

        if (deleteInvoked)
            new DeleteAnnotationAsyncTask().execute(newStickyNote);
        else if (stickyNote.getId().startsWith("new_"))
            new CreateAnnotationAsyncTask().execute(newStickyNote);
        else
            new UpdateAnnotationAsyncTask().execute(newStickyNote);
    }

    @Override protected @NonNull String getStickyNoteAuthorName() {
        return AccountUtils.SELECTED_ACCOUNT.getLogin();
    }

    @Override protected @NonNull String generateNewStickyNoteId() {
        return "new_" + UUID.randomUUID();
    }

    // </editor-fold desc="MuPdfFragment">

    // <editor-fold desc="FloatingActionButtons">

    private void onWhiteBackgroundClicked() {
        onBackPressed();
    }

    private void onValidateSelected() {
        collapseFabMenu();

        Action positiveAction = ActionUtils.getPositiveAction(mDossier);
        if (positiveAction != null)
            ((DossierDetailsFragmentListener) getActivity()).onActionButtonClicked(mDossier, mBureauId, positiveAction);
    }

    private void onCancelSelected() {
        collapseFabMenu();

        Action negativeAction = ActionUtils.getNegativeAction(mDossier);
        if (negativeAction != null)
            ((DossierDetailsFragmentListener) getActivity()).onActionButtonClicked(mDossier, mBureauId, negativeAction);
    }

    private void onAnnotationSelected() {
        collapseFabMenu();

        if (!DeviceUtils.isConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.Action_unavailable_offline, Toast.LENGTH_LONG).show();
            return;
        }

        startCreateStickyNoteOnNextMove(true);
    }

    private void toggleFabMenuVisibility() {

        if (mDossier == null)
            return;

        if (mMainButtonViewSwitcher.getDisplayedChild() == 0)
            expanseFabMenu();
        else
            collapseFabMenu();
    }

    private void expanseFabMenu() {

        mMainButtonViewSwitcher.setInAnimation(getActivity(), R.anim.rotation_clockwise_in);
        mMainButtonViewSwitcher.setOutAnimation(getActivity(), R.anim.rotation_clockwise_out);
        mMainButtonViewSwitcher.setDisplayedChild(1);

        // Compute values

        boolean hasNegativeAction = (ActionUtils.getNegativeAction(mDossier) != null);
        int mainRank = 0;
        int cancelRank = 1;
        int annotationRank = 1 + (hasNegativeAction ? 1 : 0);

        // Animate

        ViewUtils.showAfterDelay(mFabWhiteBackground, 0);
        ViewUtils.showAfterDelay(mValidateLabel, mainRank * FAB_ANIM_DELAY_IN_MS);

        if (hasNegativeAction) {
            ViewUtils.showAfterDelay(mCancelFab, cancelRank * FAB_ANIM_DELAY_IN_MS);
            ViewUtils.showAfterDelay(mCancelLabel, cancelRank * FAB_ANIM_DELAY_IN_MS);
        }

        ViewUtils.showAfterDelay(mAnnotationFab, annotationRank * FAB_ANIM_DELAY_IN_MS);
        ViewUtils.showAfterDelay(mAnnotationLabel, annotationRank * FAB_ANIM_DELAY_IN_MS);
    }

    private void collapseFabMenu() {

        mMainButtonViewSwitcher.setInAnimation(getActivity(), R.anim.rotation_anticlockwise_in);
        mMainButtonViewSwitcher.setOutAnimation(getActivity(), R.anim.rotation_anticlockwise_out);
        mMainButtonViewSwitcher.setDisplayedChild(0);

        // Compute values

        boolean hasNegativeAction = (ActionUtils.getNegativeAction(mDossier) != null);
        int maxRank = (hasNegativeAction ? 1 : 0) + (mIsAnnotable ? 1 : 0);
        int cancelReverseRank = maxRank - (mIsAnnotable ? 1 : 0);
        int annotationReverseRank = 0;

        // Animate

        ViewUtils.hideAfterDelay(mFabWhiteBackground, 0);
        ViewUtils.hideAfterDelay(mValidateLabel, maxRank * FAB_ANIM_DELAY_IN_MS);

        if (hasNegativeAction) {
            ViewUtils.hideAfterDelay(mCancelFab, cancelReverseRank * FAB_ANIM_DELAY_IN_MS);
            ViewUtils.hideAfterDelay(mCancelLabel, cancelReverseRank * FAB_ANIM_DELAY_IN_MS);
        }

        ViewUtils.hideAfterDelay(mAnnotationFab, annotationReverseRank * FAB_ANIM_DELAY_IN_MS);
        ViewUtils.hideAfterDelay(mAnnotationLabel, annotationReverseRank * FAB_ANIM_DELAY_IN_MS);
    }

    private void updateFab(@Nullable Dossier dossier) {

        // Default cases

        if ((dossier == null) || (getView() == null))
            return;

        //

        Action positiveAction = ActionUtils.getPositiveAction(dossier);
        mValidateLabelTextView.setText((positiveAction != null) ? positiveAction.getTitle() : R.string.action_non_implementee);

        Action negativeAction = ActionUtils.getNegativeAction(dossier);
        mCancelLabelTextView.setText((negativeAction != null) ? negativeAction.getTitle() : R.string.action_non_implementee);
    }

    // </editor-fold desc="FloatingActionButtons">

    public void update(@Nullable Dossier dossier, @NonNull String bureauId, @Nullable String documentId) {

        mBureauId = bureauId;
        mDossier = dossier;
        mDocumentId = documentId;

        ((DossierDetailsFragmentListener) getActivity()).lockInfoDrawer(true);

        if ((dossier != null) && (!TextUtils.isEmpty(dossier.getId()))) {
            showProgressLayout();
            getDossierDetails(false);
        } else {
            updateReader();
        }

        updateFab(dossier);
    }

    private void getDossierDetails(boolean forceReload) {

        // To force reload dossier details, just delete its main document path (on local storage).

        if (forceReload) {
            mDossier.getDocumentList().clear();
            mDossier.setCircuit(null);
        }

        // Download information only if details aren't already available

        new DossierLoadingAsyncTask().execute();
    }

    private void updateReader() {
        //Adrien - TODO - Error messages

        final Document document = DossierUtils.findCurrentDocument(mDossier, mDocumentId);
        if (document == null)
            return;

        File documentFile = DocumentUtils.getFile(getActivity(), mDossier, document);
        if (!documentFile.exists())
            return;

        openFile(documentFile.getAbsolutePath());

        SparseArray<HashMap<String, StickyNote>> muPdfStickyNotes = parapheurToMuPdfStickyNote(document.getPagesAnnotations());
        updateStickyNotes(muPdfStickyNotes);

        mIsAnnotable = DocumentUtils.isMainDocument(mDossier, document);
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

    private static @NonNull SparseArray<HashMap<String, StickyNote>> parapheurToMuPdfStickyNote(@Nullable SparseArray<PageAnnotations> parapheurAnnotations) {

        // Default case

        SparseArray<HashMap<String, StickyNote>> result = new SparseArray<>();

        if (parapheurAnnotations == null)
            return result;

        //

        for (int i = 0; i < parapheurAnnotations.size(); i++) {

            HashMap<String, StickyNote> stickyNoteMap = new HashMap<>();
            int pageIndex = parapheurAnnotations.keyAt(i);
            PageAnnotations pageAnnotation = parapheurAnnotations.get(pageIndex);

            for (Annotation annotation : pageAnnotation.getAnnotations()) {

                // Payload, to ease irrelevants MuPdf lib data

                HashMap<String, Object> payload = new HashMap<>();
                payload.put(ANNOTATION_PAYLOAD_STEP, annotation.getStep());

                // Building final StickyNote object

                boolean isLocked = !TextUtils.equals(annotation.getAuthor(), AccountUtils.SELECTED_ACCOUNT.getUserFullName());

                stickyNoteMap.put(annotation.getUuid(), new StickyNote(
                        annotation.getUuid(),
                        ViewUtils.translateDpiRect(annotation.getRect(), 150, 144),
                        StickyNote.Color.BLUE,
                        annotation.getText(),
                        annotation.getAuthor(),
                        null,
                        StringUtils.parseIso8601Date(annotation.getDate()),
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

        for (Document mainDocument : DossierUtils.getMainDocuments(mDossier))
            if (TextUtils.equals(documentName, mainDocument.getName()))
                return mainDocument.getId();

        for (Document annexes : DossierUtils.getAnnexes(mDossier))
            if (TextUtils.equals(documentName, annexes.getName()))
                return annexes.getId();

        return null;
    }

    @Override public void onDataChanged() {

        if (!DeviceUtils.isDebugOffline())
            updateCircuitInfoDrawerContent();

        updateReader();
    }

    private void startShareIntent() {

        Document document = DossierUtils.findCurrentDocument(mDossier, mDocumentId);

        // Default cases

        if (document == null)
            return;

        File documentFile = DocumentUtils.getFile(getActivity(), mDossier, document);
        if (!documentFile.exists())
            return;

        // Start share

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        intentShareFile.setType("application/pdf");
        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(documentFile));
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.action_share_subject), mDossier.getName()));
        // FIXME : uncomment this when Dropbox will fix its API.
        // FIXME : dropboxforum.com/hc/en-us/community/posts/203352359-Dropbox-should-respond-to-Android-Intent-ACTION-SEND
        // intentShareFile.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.action_share_text), document.getName(), dossier.getName()));
        startActivity(Intent.createChooser(intentShareFile, getString(R.string.Choose_an_app)));
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

        boolean isAnyDrawerOpened();

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

            Account currentAccount = AccountUtils.SELECTED_ACCOUNT;

            final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());

            // Default case

            if (mDossier == null)
                return null;

            // Download the dossier Metadata

            if (DeviceUtils.isConnected(getActivity())) {
                if (!DossierUtils.areDetailsAvailable(mDossier)) {
                    showSpinnerOnUiThread();

                    try {
                        Dossier retrievedDossier = RESTClient.INSTANCE.getDossier(currentAccount, mBureauId, mDossier.getId());
                        final List<Document> documentList = retrievedDossier.getDocumentList();
                        mDossier.setDocumentList(documentList);

                        mDossier.setCircuit(RESTClient.INSTANCE.getCircuit(currentAccount, mDossier.getId()));

                        // Retrieve Bureau

                        Bureau curentBureau = null;
                        try {
                            Dao<Bureau, Integer> bureauDao = dbHelper.getBureauDao();
                            List<Bureau> fetchedBureauList = bureauDao.queryBuilder().where().eq(Bureau.DB_FIELD_ID, mBureauId).query();

                            if (fetchedBureauList.isEmpty())
                                return null;

                            curentBureau = fetchedBureauList.get(0);
                        } catch (SQLException e) { e.printStackTrace(); }

                        if (curentBureau == null)
                            return null;

                        final Bureau finalCurrentBureau = curentBureau;

                        // Save in Database

                        final List<Document> documentToDeleteList = new ArrayList<>();
                        documentToDeleteList.addAll(DocumentUtils.getDeletableDossierList(Collections.singletonList(mDossier), documentList));

                        final Dao<Dossier, Integer> dossierDao = dbHelper.getDossierDao();
                        final Dao<Document, Integer> documentDao = dbHelper.getDocumentDao();
                        dossierDao.callBatchTasks(new Callable<Void>() {
                            @Override public Void call() throws Exception {

                                documentDao.delete(documentToDeleteList);

                                mDossier.setParent(finalCurrentBureau);
                                mDossier.setSyncDate(new Date());
                                dossierDao.createOrUpdate(mDossier);

                                for (Document document : documentList) {
                                    document.setParent(mDossier);
                                    document.setSyncDate(new Date());
                                    documentDao.createOrUpdate(document);
                                }

                                return null;
                            }
                        });
                    } catch (IParapheurException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        new IParapheurException(-1, "DB error").printStackTrace();
                    }
                }
            } else { // Offline backup

                try {
                    List<Dossier> dbRequestResult = dbHelper.getDossierDao().queryBuilder().where().eq(Dossier.DB_FIELD_ID, mDossier.getId()).query();

                    if (dbRequestResult.isEmpty())
                        return null;

                    mDossier = dbRequestResult.get(0);
                    List<Document> documents = new ArrayList<>();
                    CollectionUtils.safeAddAll(documents, mDossier.getChildrenDocuments());
                    mDossier.setDocumentList(documents);
                } catch (SQLException e) { e.printStackTrace(); }
            }

            // Getting metadata

            Document currentDocument = DossierUtils.findCurrentDocument(mDossier, mDocumentId);
            if (currentDocument == null)
                return null;

            showSpinnerOnUiThread();
            File file = DocumentUtils.getFile(getActivity(), mDossier, currentDocument);
            Log.v(LOG_TAG, file.exists() ? "Dossier loaded from cache" : "Downloading dossier...");

            if (!file.exists() && DeviceUtils.isConnected(getActivity())) {
                String downloadUrl = DocumentUtils.generateContentUrl(currentDocument);
                if (downloadUrl != null) {
                    try { RESTClient.INSTANCE.downloadFile(currentAccount, downloadUrl, file.getAbsolutePath()); } catch (IParapheurException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Loading user data and annotations

            if (DeviceUtils.isConnected(getActivity())) {
                SerializableSparseArray<PageAnnotations> annotations = new SerializableSparseArray<>();

                if (TextUtils.isEmpty(currentAccount.getUserFullName())) {
                    try { RESTClient.INSTANCE.updateAccountInformations(currentAccount); } catch (IParapheurException e) { e.printStackTrace(); }
                }

                if (DocumentUtils.isMainDocument(mDossier, currentDocument)) {
                    try {
                        annotations = RESTClient.INSTANCE.getAnnotations(currentAccount, mDossier.getId(), currentDocument.getId());
                    } catch (IParapheurException e) { e.printStackTrace(); }
                }
                currentDocument.setPagesAnnotations(annotations);
            }

            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mDocumentId == null)
                if (!DossierUtils.getMainDocuments(mDossier).isEmpty())
                    mDocumentId = DossierUtils.getMainDocuments(mDossier).get(0).getId();

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
            Account currentAccount = AccountUtils.SELECTED_ACCOUNT;

            try {
                mNewId = RESTClient.INSTANCE.createAnnotation(currentAccount, mDossier.getId(), mDocumentId, mCurrentAnnotation, getCurrentPage());
            } catch (IParapheurException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if ((!success) || TextUtils.isEmpty(mNewId)) {
                Toast.makeText(getActivity(), R.string.Error_on_annotation_update, Toast.LENGTH_LONG).show();
            } else {
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
            Account currentAccount = AccountUtils.SELECTED_ACCOUNT;

            try {
                RESTClient.INSTANCE.updateAnnotation(currentAccount, mDossier.getId(), mDocumentId, currentAnnotation, getCurrentPage());
            } catch (IParapheurException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (!success)
                Toast.makeText(getActivity(), R.string.Error_on_annotation_update, Toast.LENGTH_LONG).show();
        }

    }

    private class DeleteAnnotationAsyncTask extends AsyncTask<Annotation, Void, Boolean> {

        @Override protected Boolean doInBackground(Annotation... params) {

            Account currentAccount = AccountUtils.SELECTED_ACCOUNT;

            if (params.length < 1)
                return null;

            Annotation currentAnnotation = params[0];

            try {
                RESTClient.INSTANCE.deleteAnnotation(currentAccount, mDossier.getId(), mDocumentId, currentAnnotation.getUuid(), getCurrentPage());
            } catch (IParapheurException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (!success)
                Toast.makeText(getActivity(), R.string.Error_on_annotation_delete, Toast.LENGTH_LONG).show();
        }

    }

}
