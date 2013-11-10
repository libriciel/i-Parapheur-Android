package org.adullact.iparapheur.controller;

/**
 * This interface is used by {@link org.adullact.iparapheur.controller.utils.LoadingTask},
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
