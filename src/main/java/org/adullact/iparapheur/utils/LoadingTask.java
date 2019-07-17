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
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.adullact.iparapheur.R;

import io.sentry.Sentry;


/**
 * Simple AsyncTask that automatically show a loader in the action bar.
 * If a {@link org.adullact.iparapheur.utils.LoadingTask.DataChangeListener} is defined, this
 * listener is notified when the task finishes.
 * It automatically manage exceptions and feed back the user with a Toast.
 * Subclasses must Override
 * Created by jmaire on 04/11/2013.
 */
public abstract class LoadingTask extends AsyncTask<String, Integer, String> {

    protected Activity activity;
    private DataChangeListener dataListener;

    public LoadingTask(Activity activity, DataChangeListener listener) {
        this.activity = activity;
        this.dataListener = listener;
    }

    protected abstract void load(String... params) throws IParapheurException;

    @Override protected void onPreExecute() {
        if (DeviceUtils.isDebugOffline()) {
            Toast.makeText(activity, "Attention : Mode Hors Ligne.", Toast.LENGTH_SHORT).show();
            showProgress();
        } else {
            ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                this.cancel(true);
                Toast.makeText(activity, R.string.network_unreachable, Toast.LENGTH_LONG).show();
            } else {
                showProgress();
            }
        }
    }

    @Override protected String doInBackground(String... params) {
        String error = null;
        try {
            load(params);
        } catch (IParapheurException e) {
            error = activity.getResources().getString(e.getResId(), e.getComplement());
            Sentry.capture(e);
        }
        return error;
    }

    @Override protected void onPostExecute(String error) {
        hideProgress();
        if (error != null) {
            Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
        } else if (dataListener != null) {
            dataListener.onDataChanged();
        }
    }

    @Override protected void onCancelled() {
        hideProgress();
    }

    /**
     * Show the indeterminate progress wheel
     * Protected so subclasses can override it (ex. for determinate progressBar)
     */
    protected void showProgress() {
        activity.setProgressBarIndeterminateVisibility(true);
    }

    /**
     * Hide the indeterminate progress wheel
     * Protected so subclasses can override it (ex. for determinate progressBar)
     */
    protected void hideProgress() {
        activity.setProgressBarIndeterminateVisibility(false);
    }

    /**
     * This interface is used by {@link LoadingTask},
     * when the task has finished loading its data, the component which holds the data is notified
     * with the call of the function onDataChanged().
     * Created by jmaire on 04/11/2013.
     */
    public interface DataChangeListener {

        /**
         * Used to notify a class that its data has changed. The class should reload the UI in case
         * of a Fragment or an Activity.
         */
        void onDataChanged();

    }

}
