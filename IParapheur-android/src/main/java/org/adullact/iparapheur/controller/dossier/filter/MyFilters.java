package org.adullact.iparapheur.controller.dossier.filter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.adullact.iparapheur.controller.IParapheurApplication;
import org.adullact.iparapheur.model.Filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jmaire on 06/02/2014.
 */
public enum MyFilters implements SharedPreferences.OnSharedPreferenceChangeListener {
    INSTANCE;

    public static final String PREFS_PREFIX = "filtre_";
    public static final String PREFS_NOM_SUFFIX = "_nom";
    public static final String PREFS_TITRE_SUFFIX = "_titre";
    public static final String PREFS_ETAT_SUFFIX = "_etat";
    public static final String PREFS_TYPES_SUFFIX = "_type";
    public static final String PREFS_SOUSTYPES_SUFFIX = "_soustype";
    public static final String PREFS_DATEDEBUT_SUFFIX = "_datedebut";
    public static final String PREFS_DATEFIN_SUFFIX = "_datefin";

    private ArrayList<Filter> filters = null;
    private Filter selectedFilter;

    public List<Filter> getFilters() {
        if (filters == null) {
            filters = new ArrayList<Filter>();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheurApplication.getContext());

            for (String pref : sharedPreferences.getAll().keySet()) {
                if (pref.startsWith(PREFS_PREFIX))
                {
                    String id = pref.substring(pref.indexOf("_") + 1);
                    id = id.substring(0, id.lastIndexOf("_"));
                    Filter filter = new Filter(id);
                    if (!filters.contains(filter)) {
                        filter.setName(sharedPreferences.getString(PREFS_PREFIX + id + PREFS_NOM_SUFFIX, ""));
                        filter.setTitle(sharedPreferences.getString(PREFS_PREFIX + id + PREFS_TITRE_SUFFIX, ""));
                        filter.setState(sharedPreferences.getString(PREFS_PREFIX + id + PREFS_ETAT_SUFFIX, ""));
                        filter.setTypes(new ArrayList<String>(sharedPreferences.getStringSet(PREFS_PREFIX + id + PREFS_TYPES_SUFFIX, new HashSet<String>())));
                        filter.setSubTypes(new ArrayList<String>(sharedPreferences.getStringSet(PREFS_PREFIX + id + PREFS_SOUSTYPES_SUFFIX, new HashSet<String>())));
                        long debut = sharedPreferences.getLong(PREFS_PREFIX + id + PREFS_DATEDEBUT_SUFFIX, 0L);
                        if (debut != 0L) {
                            filter.setBeginDate(debut);
                        }
                        long fin = sharedPreferences.getLong(PREFS_PREFIX + id + PREFS_DATEFIN_SUFFIX, 0L);
                        if (fin != 0L) {
                            filter.setEndDate(fin);
                        }
                        filters.add(filter);
                    }
                }
            }
            if ((selectedFilter != null) && !filters.contains(selectedFilter)) {
                filters.add(selectedFilter);
            }
            filters.add(new Filter()); // Filtre par défaut
        }
        return filters;
    }

    public void save(Filter filter) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheurApplication.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString( PREFS_PREFIX + filter.getId() + PREFS_NOM_SUFFIX, filter.getName())
                .putString( PREFS_PREFIX + filter.getId() + PREFS_TITRE_SUFFIX, filter.getTitle())
                .putString( PREFS_PREFIX + filter.getId() + PREFS_ETAT_SUFFIX, filter.getState())
                .putStringSet(PREFS_PREFIX + filter.getId() + PREFS_TYPES_SUFFIX, new HashSet<String>(filter.getTypes()))
                .putStringSet(PREFS_PREFIX + filter.getId() + PREFS_SOUSTYPES_SUFFIX, new HashSet<String>(filter.getSubTypes()));
        if (filter.getBeginDate() != null) {
            editor.putLong(PREFS_PREFIX + filter.getId() + PREFS_DATEDEBUT_SUFFIX, filter.getBeginDate().getTime());
        }
        if (filter.getEndDate() != null) {
            editor.putLong(PREFS_PREFIX + filter.getId() + PREFS_DATEFIN_SUFFIX, filter.getEndDate().getTime());
        }
        editor.apply();
        filters = null;
        getFilters();
    }

    public void delete(Filter filter) {
        String id = filter.getId();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(IParapheurApplication.getContext());
        Set<String> keySet = sharedPreferences.getAll().keySet();
        if (keySet.contains( PREFS_PREFIX + id + PREFS_NOM_SUFFIX))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit()
                    .remove(PREFS_PREFIX + id + PREFS_NOM_SUFFIX)
                    .remove(PREFS_PREFIX + id + PREFS_TITRE_SUFFIX)
                    .remove(PREFS_PREFIX + id + PREFS_ETAT_SUFFIX)
                    .remove(PREFS_PREFIX + id + PREFS_TYPES_SUFFIX)
                    .remove(PREFS_PREFIX + id + PREFS_SOUSTYPES_SUFFIX)
                    .remove(PREFS_PREFIX + id + PREFS_DATEDEBUT_SUFFIX)
                    .remove(PREFS_PREFIX + id + PREFS_DATEFIN_SUFFIX);
            editor.apply();
        }
        if ((selectedFilter != null) && (selectedFilter.getId().equals(id))) {
            selectedFilter = null;
        }
        filters = null;
        getFilters();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.startsWith(PREFS_PREFIX)) {
            filters = null;
            getFilters();
            // if an Account was previously selected, update it with the new one
            if (selectedFilter != null)
            {
                selectedFilter = getFilter(selectedFilter.getId());
            }
        }
    }

    public Filter getFilter(String id) {
        int index = filters.indexOf(new Filter(id));
        return (index != -1)? filters.get(index) : null;
    }

    public Filter getSelectedFilter() {
        return selectedFilter;
    }

    public void selectFilter(Filter filter) {
        selectedFilter = filter;
    }
}

