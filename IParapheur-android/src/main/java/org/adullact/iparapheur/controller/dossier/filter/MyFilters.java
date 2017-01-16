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
package org.adullact.iparapheur.controller.dossier.filter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.adullact.iparapheur.model.Filter;
import org.adullact.iparapheur.model.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

	public List<Filter> getFilters(@NonNull Context context) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return getFilters(sharedPreferences);
	}

	public List<Filter> getFilters(@NonNull SharedPreferences sharedPreferences) {

		if (filters == null) {
			filters = new ArrayList<>();

			for (String pref : sharedPreferences.getAll().keySet()) {
				if (pref.startsWith(PREFS_PREFIX)) {
					String id = pref.substring(pref.indexOf("_") + 1);
					id = id.substring(0, id.lastIndexOf("_"));
					Filter filter = new Filter(id);
					if (!filters.contains(filter)) {
						filter.setName(sharedPreferences.getString(PREFS_PREFIX + id + PREFS_NOM_SUFFIX, ""));
						filter.setTitle(sharedPreferences.getString(PREFS_PREFIX + id + PREFS_TITRE_SUFFIX, ""));
						filter.setState(State.fromServerValue(sharedPreferences.getString(PREFS_PREFIX + id + PREFS_ETAT_SUFFIX,
																						  State.A_TRAITER.getServerValue()
						)));
						filter.setTypeList(new ArrayList<>(sharedPreferences.getStringSet(PREFS_PREFIX + id + PREFS_TYPES_SUFFIX, new HashSet<String>())));
						filter.setSubTypeList(new ArrayList<>(sharedPreferences.getStringSet(PREFS_PREFIX + id + PREFS_SOUSTYPES_SUFFIX,
																							 new HashSet<String>()
						)));

						long debut = sharedPreferences.getLong(PREFS_PREFIX + id + PREFS_DATEDEBUT_SUFFIX, 0L);
						if (debut != 0L)
							filter.setBeginDate(debut);

						long fin = sharedPreferences.getLong(PREFS_PREFIX + id + PREFS_DATEFIN_SUFFIX, 0L);
						if (fin != 0L)
							filter.setEndDate(fin);

						filters.add(filter);
					}
				}
			}

			if ((selectedFilter != null) && !filters.contains(selectedFilter))
				filters.add(selectedFilter);
		}
		return filters;
	}

	public void save(@NonNull Context context, @NonNull Filter filter) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(PREFS_PREFIX + filter.getId() + PREFS_NOM_SUFFIX, filter.getName());
		editor.putString(PREFS_PREFIX + filter.getId() + PREFS_TITRE_SUFFIX, filter.getTitle());
		editor.putString(PREFS_PREFIX + filter.getId() + PREFS_ETAT_SUFFIX, filter.getState().getServerValue());
		editor.putStringSet(PREFS_PREFIX + filter.getId() + PREFS_TYPES_SUFFIX, new HashSet<>(filter.getTypeList()));
		editor.putStringSet(PREFS_PREFIX + filter.getId() + PREFS_SOUSTYPES_SUFFIX, new HashSet<>(filter.getSubTypeList()));

		if (filter.getBeginDate() != null)
			editor.putLong(PREFS_PREFIX + filter.getId() + PREFS_DATEDEBUT_SUFFIX, filter.getBeginDate().getTime());

		if (filter.getEndDate() != null)
			editor.putLong(PREFS_PREFIX + filter.getId() + PREFS_DATEFIN_SUFFIX, filter.getEndDate().getTime());

		editor.apply();
		filters = null;
		getFilters(sharedPreferences);
	}

	public void delete(@NonNull Context context, Filter filter) {

		String id = filter.getId();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> keySet = sharedPreferences.getAll().keySet();

		if (keySet.contains(PREFS_PREFIX + id + PREFS_NOM_SUFFIX)) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.remove(PREFS_PREFIX + id + PREFS_NOM_SUFFIX);
			editor.remove(PREFS_PREFIX + id + PREFS_TITRE_SUFFIX);
			editor.remove(PREFS_PREFIX + id + PREFS_ETAT_SUFFIX);
			editor.remove(PREFS_PREFIX + id + PREFS_TYPES_SUFFIX);
			editor.remove(PREFS_PREFIX + id + PREFS_SOUSTYPES_SUFFIX);
			editor.remove(PREFS_PREFIX + id + PREFS_DATEDEBUT_SUFFIX);
			editor.remove(PREFS_PREFIX + id + PREFS_DATEFIN_SUFFIX);
			editor.apply();
		}
		if ((selectedFilter != null) && (selectedFilter.getId().equals(id))) {
			selectedFilter = null;
		}
		filters = null;
		getFilters(sharedPreferences);
	}

	@Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		if (s.startsWith(PREFS_PREFIX)) {
			filters = null;
			getFilters(sharedPreferences);

			// if an Account was previously selected, update it with the new one
			if (selectedFilter != null)
				selectedFilter = getFilter(selectedFilter.getId());
		}
	}

	public Filter getFilter(String id) {
		int index = filters.indexOf(new Filter(id));
		return (index != -1) ? filters.get(index) : null;
	}

	public Filter getSelectedFilter() {
		return selectedFilter;
	}

	public void selectFilter(@Nullable Filter filter) {
		selectedFilter = filter;
	}
}

