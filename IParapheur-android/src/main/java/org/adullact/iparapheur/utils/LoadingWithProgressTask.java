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
        progressBar = (ProgressBar)activity.findViewById(R.id.progressBar);
    }

    @Override
    protected void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
    }

    @Override
    protected void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progressBar.setProgress(values[0]);
    }
}
