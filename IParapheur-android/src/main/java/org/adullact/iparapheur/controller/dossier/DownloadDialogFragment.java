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

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.database.DatabaseHelper;
import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.utils.BureauUtils;
import org.adullact.iparapheur.utils.DocumentUtils;
import org.adullact.iparapheur.utils.DossierUtils;
import org.adullact.iparapheur.utils.FileUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.SerializableSparseArray;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;


public class DownloadDialogFragment extends DialogFragment {

	public static final String FRAGMENT_TAG = "download_dialog_fragment";

	private static final String ARGUMENT_ACCOUNT = "account";
	private static final String LOG_TAG = "DownloadDialogFragment";

	private ProgressBar mBureauProgressBar;
	private ProgressBar mDossierProgressBar;
	private ProgressBar mDocumentProgressBar;
	private TextView mBureauProgressTextView;
	private TextView mDossierProgressTextView;
	private TextView mDocumentProgressTextView;

	private Account mAccount;
	private DownloadTask mPendingTask;

	public static DownloadDialogFragment newInstance(@NonNull Account account) {

		Gson gson = new Gson();
		String data = gson.toJson(account);

		DownloadDialogFragment fragment = new DownloadDialogFragment();

		Bundle args = new Bundle();
		args.putString(ARGUMENT_ACCOUNT, data);
		fragment.setArguments(args);

		return fragment;
	}

	// <editor-fold desc="LifeCycle">

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {

			Gson gson = new Gson();
			String data = getArguments().getString(ARGUMENT_ACCOUNT);
			mAccount = gson.fromJson(data, Account.class);
		}
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create view

		View view = LayoutInflater.from(getActivity()).inflate(R.layout.action_download, null);

		mBureauProgressBar = (ProgressBar) view.findViewById(R.id.action_download_bureau_progressbar);
		mDossierProgressBar = (ProgressBar) view.findViewById(R.id.action_download_dossier_progressbar);
		mDocumentProgressBar = (ProgressBar) view.findViewById(R.id.action_download_document_progressbar);

		mBureauProgressTextView = (TextView) view.findViewById(R.id.action_download_bureau_progress_textview);
		mDossierProgressTextView = (TextView) view.findViewById(R.id.action_download_dossier_progress_textview);
		mDocumentProgressTextView = (TextView) view.findViewById(R.id.action_download_document_progress_textview);

		// Build Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Main_Dialog);
		builder.setTitle(getString(R.string.Offline_content));
		builder.setView(view);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				onCancelButtonClicked();
			}
		});

		return builder.create();
	}

	@Override public void onResume() {
		super.onResume();

		mPendingTask = new DownloadTask();
		mPendingTask.execute(mAccount);
	}

	// </editor-fold desc="LifeCycle">

	private void onCancelButtonClicked() {

		if (mPendingTask != null)
			mPendingTask.cancel(false);

		dismiss();
	}

	private class DownloadTask extends AsyncTask<Account, Long, IParapheurException> {

		private final Long STEP_BUREAUX_METADATA = 0L;
		private final Long STEP_DOSSIERS_METADATA = 1L;
		private final Long STEP_DOCUMENT_FILES = 2L;

		@Override protected IParapheurException doInBackground(Account... accounts) {

			final DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
			Account selectedAccount = null;
			final List<Bureau> bureauxList = new ArrayList<>();

			// yes, this method does a little bit of Thread pausing.
			// It may feel weird, but it bring a way better feeling on download,
			// and this AsyncTask is not on the UI thread anyway.
			//
			// If we're dealing with a fast connection, and an almost empty Parapheur,
			// The popup will flash for a fraction of second.
			// Those delays are indeed loosing 1.5 second per thread,
			// but make the UI way more smooth.
			//
			// Trust me, I'm an engineer, keep those.

			// UI tuning
			publishProgress(STEP_BUREAUX_METADATA, 0L, 100L);
			try { Thread.sleep(500); } catch (InterruptedException e) { /* not used */ }

			// Refresh DB Account

			if (accounts == null || accounts.length == 0)
				return null;

			try {
				Account deserializedAccount = accounts[0];

				Dao<Account, Integer> accountDao = dbHelper.getAccountDao();
				List<Account> fetchedAccountList = accountDao.queryBuilder().where().eq(Account.DB_FIELD_ID, deserializedAccount.getId()).query();

				if (fetchedAccountList.isEmpty())
					return null;

				selectedAccount = fetchedAccountList.get(0);
			}
			catch (SQLException e) { e.printStackTrace(); }

			if (selectedAccount == null)
				return null;

			// Updating Bureaux

			try { bureauxList.addAll(RESTClient.INSTANCE.getBureaux(selectedAccount)); }
			catch (final IParapheurException exception) { return exception; }

			final ArrayList<Dossier> dossierList = new ArrayList<>();
			List<Dossier> incompleteDossierList = new ArrayList<>();
			final List<Document> finalDocumentList = new ArrayList<>();

			Long totalBureauxMetadataSize = (long) bureauxList.size();
			Long progressBureauxMetadataSize = 0L;

			for (Bureau bureau : bureauxList) {
				Bureau parent = BureauUtils.findInList(bureauxList, bureau.getId());

				try {
					List<Dossier> incompleteDossierTempList = RESTClient.INSTANCE.getDossiers(bureau.getId());
					for (Dossier dossier : incompleteDossierTempList)
						dossier.setParent(parent);

					incompleteDossierList.addAll(incompleteDossierTempList);
				}
				catch (IParapheurException e) { e.printStackTrace(); }

				progressBureauxMetadataSize++;
				publishProgress(STEP_BUREAUX_METADATA, progressBureauxMetadataSize, totalBureauxMetadataSize);

				if (isCancelled())
					return new IParapheurException(-1, "Annulation");
			}

			// UI tuning
			publishProgress(STEP_BUREAUX_METADATA, 100L, 100L);
			publishProgress(STEP_DOSSIERS_METADATA, 0L, 100L);
			try { Thread.sleep(250); } catch (InterruptedException e) { /* not used */ }

			// Updating Dossiers

			Long totalDossiersMetadataSize = (long) incompleteDossierList.size();
			Long progressDossiersMetadataSize = 0L;

			for (Dossier incompleteDossier : incompleteDossierList) {

				try {
					Dossier fullDossier = RESTClient.INSTANCE.getDossier(incompleteDossier.getParent().getId(), incompleteDossier.getId());

					try { fullDossier.setCircuit(RESTClient.INSTANCE.getCircuit(incompleteDossier.getId())); }
					catch (IParapheurException e) { e.printStackTrace(); }

					fullDossier.setParent(BureauUtils.findInList(bureauxList, incompleteDossier.getParent().getId()));
					dossierList.add(fullDossier);

					for (Document document : fullDossier.getDocumentList()) {
						document.setParent(fullDossier);

						if (DocumentUtils.isMainDocument(fullDossier, document)) {
							SerializableSparseArray<PageAnnotations> annotations;
							annotations = RESTClient.INSTANCE.getAnnotations(fullDossier.getId(), document.getId());
							document.setPagesAnnotations(annotations);
						}

						finalDocumentList.add(document);

						if (isCancelled())
							return new IParapheurException(-1, "Annulation");
					}
				}
				catch (IParapheurException e) { e.printStackTrace(); }

				progressDossiersMetadataSize++;
				publishProgress(STEP_DOSSIERS_METADATA, progressDossiersMetadataSize, totalDossiersMetadataSize);
			}

			// Cleanup and save in database

			try {

				final Dao<Bureau, Integer> bureauDao = dbHelper.getBureauDao();
				final Dao<Dossier, Integer> dossierDao = dbHelper.getDossierDao();
				final Dao<Document, Integer> documentDao = dbHelper.getDocumentDao();

				// Retrieve cascade deletable content

				final List<Bureau> bureauToDeleteList = BureauUtils.getDeletableBureauList(selectedAccount, bureauxList);

				final List<Dossier> dossierToDeleteList = new ArrayList<>();
				dossierToDeleteList.addAll(DossierUtils.getAllChildrenFrom(bureauToDeleteList));
				dossierToDeleteList.addAll(DossierUtils.getDeletableDossierList(bureauxList, dossierList));

				final List<Document> documentToDeleteList = new ArrayList<>();
				documentToDeleteList.addAll(DocumentUtils.getAllChildrenFrom(dossierToDeleteList));
				documentToDeleteList.addAll(DocumentUtils.getDeletableDossierList(dossierList, finalDocumentList));

				// Delete

				Log.d("DownloadTask", "delete Bureaux   : " + bureauToDeleteList);
				Log.d("DownloadTask", "delete Dossiers  : " + dossierToDeleteList);
				Log.d("DownloadTask", "delete Documents : " + documentToDeleteList);

				dbHelper.getDossierDao().callBatchTasks(new Callable<Void>() {
					@Override public Void call() throws Exception {

						documentDao.delete(documentToDeleteList);
						dossierDao.delete(dossierToDeleteList);
						bureauDao.delete(bureauToDeleteList);

						// Create

						for (Bureau bureau : bureauxList) {
							bureau.setSyncDate(new Date());
							bureauDao.createOrUpdate(bureau);
						}

						for (Dossier dossier : dossierList) {
							dossier.setSyncDate(new Date());
							dossierDao.createOrUpdate(dossier);
						}

						for (Document document : finalDocumentList) {
							document.setSyncDate(new Date());
							documentDao.createOrUpdate(document);
						}

						return null;
					}
				});

				// Cleanup files

				for (Document documentToDelete : documentToDeleteList)
					//noinspection ResultOfMethodCallIgnored
					DocumentUtils.getFile(getActivity(), documentToDelete.getParent(), documentToDelete).delete();

				for (Dossier dossierToDelete : dossierToDeleteList)
					//noinspection ResultOfMethodCallIgnored
					FileUtils.getDirectoryForDossier(getActivity(), dossierToDelete).delete();
			}
			catch (Exception e) { return new IParapheurException(-1, "DB error"); }

			// UI tuning
			publishProgress(STEP_DOSSIERS_METADATA, 100L, 100L);
			publishProgress(STEP_DOCUMENT_FILES, 0L, 100L);
			try { Thread.sleep(250); } catch (InterruptedException e) { /* not used */ }

			// Downloading files

			List<Document> documentsToDld = new ArrayList<>();
			for (Dossier dossier : dossierList) {
				documentsToDld.addAll(dossier.getDocumentList());
			}

			Long totalDocumentFileSize = 0L;
			Long progressDocumentFileSize = 0L;
			for (Document document : documentsToDld)
				if (document.getSize() > 0)
					totalDocumentFileSize += document.getSize();

			if (totalDocumentFileSize > FileUtils.getFreeSpace(getActivity()))
				return new IParapheurException(0, "Téléchargement impossible, espace insuffisant");

			for (Document document : documentsToDld) {
				String downloadUrl = DocumentUtils.generateContentUrl(document);

				if (!TextUtils.isEmpty(downloadUrl)) {
					File documentFile = DocumentUtils.getFile(getActivity(), document.getParent(), document);

					try { RESTClient.INSTANCE.downloadFile(downloadUrl, documentFile.getAbsolutePath()); }
					catch (IParapheurException e) { e.printStackTrace(); }

					if (document.getSize() > 0)
						progressDocumentFileSize += document.getSize();

					publishProgress(STEP_DOCUMENT_FILES, progressDocumentFileSize, totalDocumentFileSize);

					if (isCancelled())
						return new IParapheurException(-1, "Annulation");
				}
			}

			// UI tuning
			publishProgress(STEP_DOCUMENT_FILES, 100L, 100L);
			try { Thread.sleep(500); } catch (InterruptedException e) { /* not used */ }

			return null;
		}

		@Override protected void onProgressUpdate(Long... values) {
			super.onProgressUpdate(values);

			if ((values.length == 3) && (values[2] != 0)) {

				int progressPercent = (int) (values[1] * 100 / values[2]);

				if (values[0].equals(STEP_BUREAUX_METADATA)) {
					mBureauProgressBar.setProgress(progressPercent);
					mBureauProgressTextView.setText("" + progressPercent + "%");
				}
				else if (values[0].equals(STEP_DOSSIERS_METADATA)) {
					mDossierProgressBar.setProgress(progressPercent);
					mDossierProgressTextView.setText("" + progressPercent + "%");
				}
				else if (values[0].equals(STEP_DOCUMENT_FILES)) {
					mDocumentProgressBar.setProgress(progressPercent);
					mDocumentProgressTextView.setText("" + progressPercent + "%");
				}
			}
		}

		@Override protected void onPostExecute(IParapheurException e) {
			super.onPostExecute(e);

			if (e != null)
				Log.e("", "" + e.getComplement());
			else
				dismiss();
		}
	}

}
