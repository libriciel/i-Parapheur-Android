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

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.RESTClient;
import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.utils.IParapheurException;
import org.adullact.iparapheur.utils.LoadingTask;
import org.adullact.iparapheur.utils.LoadingWithProgressTask;

import java.util.ArrayList;


public class ArchivageDialogFragment extends ActionDialogFragment {

	public ArchivageDialogFragment() {}

	public static ArchivageDialogFragment newInstance(ArrayList<Dossier> dossiers, String bureauId) {
		ArchivageDialogFragment f = new ArchivageDialogFragment();

		// Supply parameters as an arguments.
		Bundle args = new Bundle();
		args.putParcelableArrayList("dossiers", dossiers);
		args.putString("bureauId", bureauId);
		f.setArguments(args);

		return f;
	}

	@Override protected int getTitle() {
		return Action.ARCHIVAGE.getTitle();
	}

	@Override protected int getViewId() {
		return R.layout.action_dialog;
	}

	@Override protected void executeTask() {
		new ArchivageTask(getActivity()).execute();
	}

	private class ArchivageTask extends LoadingWithProgressTask {

		public ArchivageTask(Activity activity) {
			super(activity, (LoadingTask.DataChangeListener) getActivity());
		}

		@Override protected void load(String... params) throws IParapheurException {
			if (isCancelled()) {return;}
			int i = 0;
			int total = dossiers.size();
			publishProgress(i);
			for (Dossier dossier : dossiers) {
				if (isCancelled()) {return;}
				//Log.d("debug", "Archivage du dossier " + dossier.getName());
				RESTClient.INSTANCE.archiver(dossier.getId(), dossier.getName() + "pdf", false, bureauId);
				i++;
				publishProgress(i * 100 / total);
			}
		}
	}
}
