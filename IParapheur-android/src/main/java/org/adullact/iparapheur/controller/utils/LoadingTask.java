package org.adullact.iparapheur.controller.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.adullact.iparapheur.R;

/**
 * Simple AsyncTask that automatically show a loader in the action bar.
 * If a {@link org.adullact.iparapheur.controller.utils.LoadingTask.DataChangeListener} is defined, this
 * listener is notified when the task finishes.
 * Created by jmaire on 04/11/2013.
 */
public abstract class LoadingTask extends AsyncTask<String, Void, Void> {

    private Activity context;
    private DataChangeListener dataListener;

    public LoadingTask(Activity context, DataChangeListener listener) {
        this.context = context;
        this.dataListener = listener;
    }

    protected void onPreExecute() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            this.cancel(true);
            Toast.makeText(context, R.string.network_unreachable, Toast.LENGTH_LONG).show();
        }
        else {
            context.setProgressBarIndeterminateVisibility(true);
        }
    }

    protected void onPostExecute(Void result) {
        context.setProgressBarIndeterminateVisibility(false);
        if (dataListener != null) {
            dataListener.onDataChanged();
        }
    }

    protected void onCancelled() {
        context.setProgressBarIndeterminateVisibility(false);
    }

    /**
     * This interface is used by {@link LoadingTask},
     * when the task has finished loading its data, the component which holds the data is notified
     * with the call of the function onDataChanged().
     * Created by jmaire on 04/11/2013.
     */
    public static interface DataChangeListener {
        /**
         * Used to notify a class that its data has changed. The class should reload the UI in case
         * of a Fragment or an Activity.
         */
        void onDataChanged();
    }
}
