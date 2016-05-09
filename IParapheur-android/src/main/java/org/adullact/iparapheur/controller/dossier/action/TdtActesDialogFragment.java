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

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.LoadingWithProgressTask;

import java.util.ArrayList;


public class TdtActesDialogFragment extends ActionDialogFragment {

	public static final int REQUEST_CODE_ACTES = 103200519;    // Because A-C-T-E-S = 1-03-20-05-19
	public static final String FRAGMENT_TAG = "tdt_actes_dialog_fragment";

	protected TextView annotationPublique;
	protected TextView annotationPrivee;

	public TdtActesDialogFragment() {}

	public static TdtActesDialogFragment newInstance(ArrayList<Dossier> dossiers, String bureauId) {
		TdtActesDialogFragment f = new TdtActesDialogFragment();

		// Supply parameters as an arguments.
		Bundle args = new Bundle();
		args.putParcelableArrayList("dossiers", dossiers);
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
		return Action.TDT_ACTES.getTitle();
	}

	@Override protected int getViewId() {
		return R.layout.action_dialog;
	}

	@Override protected void executeTask() {
		new TdtTask(getActivity()).execute();
	}

	private class TdtTask extends LoadingWithProgressTask {

		public TdtTask(Activity activity) {
			super(activity, (LoadingTask.DataChangeListener) getActivity());
		}

		@Override protected void load(String... params) throws IParapheurException {
			if (isCancelled()) {return;}
			String annotPub = annotationPublique.getText().toString();
			String annotPriv = annotationPrivee.getText().toString();
			int i = 0;
			int total = dossiers.size();
			publishProgress(i);
			for (Dossier dossier : dossiers) {
				if (isCancelled()) {return;}
				// TODO : distinguer Actes et Helios
				//Log.d("debug", "Mailsec sur " + dossier.getName());
				RESTClient.INSTANCE.envoiTdtActes(dossier.getId(), "", "", "", 0L, "", annotPub, annotPriv, bureauId);
				i++;
				publishProgress(i * 100 / total);
			}
		}
	}
}
