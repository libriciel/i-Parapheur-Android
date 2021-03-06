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
package org.adullact.iparapheur.controller.dossier.action;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.AccountUtils;
import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.LoadingWithProgressTask;

import java.util.ArrayList;


public class MailSecDialogFragment extends ActionDialogFragment {

	protected TextView annotationPublique;
	protected TextView annotationPrivee;

	public MailSecDialogFragment() {}

	public static MailSecDialogFragment newInstance(ArrayList<Dossier> dossiers, String bureauId) {
		MailSecDialogFragment f = new MailSecDialogFragment();

		// Supply parameters as an arguments.
		Bundle args = new Bundle();
		Gson gson = CollectionUtils.buildGsonWithDateParser();
		args.putString("dossiers", gson.toJson(dossiers));
		args.putString("bureauId", bureauId);
		f.setArguments(args);

		return f;
	}

	@Override protected View createView() {
		View layout = super.createView();

		annotationPublique = (TextView) layout.findViewById(R.id.action_import_password);
		annotationPrivee = (TextView) layout.findViewById(R.id.action_dialog_private_annotation);

		return layout;
	}

	@Override protected int getTitle() {
		return Action.MAILSEC.getTitle();
	}

	@Override protected int getViewId() {
		return R.layout.action_dialog;
	}

	@Override protected void executeTask() {
		new MailsecTask(getActivity()).execute();
	}

	private class MailsecTask extends LoadingWithProgressTask {

		private MailsecTask(Activity activity) {
			super(activity, (LoadingTask.DataChangeListener) getActivity());
		}

		@Override protected void load(String... params) throws IParapheurException {
			if (isCancelled()) {return;}
			String annotPub = annotationPublique.getText().toString();
			String annotPriv = annotationPrivee.getText().toString();
			int i = 0;
			int total = mDossiers.size();
			publishProgress(i);
			for (Dossier dossier : mDossiers) {
				if (isCancelled()) {return;}
				//Log.d("debug", "Mailsec sur " + dossier.getName());
				RESTClient.INSTANCE.envoiMailSec(AccountUtils.SELECTED_ACCOUNT, dossier.getId(), null, null, null, "", "", "", false, true, mBureauId);
				i++;
				publishProgress(i * 100 / total);
			}
		}
	}
}
