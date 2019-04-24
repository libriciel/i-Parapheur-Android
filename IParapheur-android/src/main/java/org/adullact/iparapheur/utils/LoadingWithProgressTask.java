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
package org.adullact.iparapheur.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;

import org.adullact.iparapheur.R;


/**
 * Simple AsyncTask that automatically show a loader in the action bar.
 * If a {@link org.adullact.iparapheur.utils.LoadingTask.DataChangeListener} is defined, this
 * listener is notified when the task finishes.
 * Created by jmaire on 04/11/2013.
 */
public abstract class LoadingWithProgressTask extends LoadingTask {

	private ProgressBar progressBar;

	public LoadingWithProgressTask(Activity activity, LoadingTask.DataChangeListener listener) {
		super(activity, listener);
		progressBar = (ProgressBar) activity.findViewById(R.id.progressBar);
	}

	@Override protected void showProgress() {
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress(0);
	}

	@Override protected void hideProgress() {
		progressBar.setVisibility(View.INVISIBLE);
	}

	@Override protected void onProgressUpdate(Integer... values) {
		progressBar.setProgress(values[0]);
	}
}
