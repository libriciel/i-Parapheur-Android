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
package org.adullact.iparapheur.controller.dossier;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.adullact.iparapheur.R;

import java.util.ArrayList;


public class DossierBatchFragment extends Fragment {

	public static final String DOSSIER = "dossier";
	private ArrayList<String> dossiers;
	private ListView listView;

	public DossierBatchFragment() {
		this.dossiers = new ArrayList<String>();
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dossiers_batch_fragment, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		if ((getArguments() != null) && getArguments().containsKey(DOSSIER)) {
			this.dossiers.add(getArguments().getString(DOSSIER));
		}
		listView = (ListView) view.findViewById(R.id.dossiers_list);
		listView.setItemsCanFocus(false);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, this.dossiers));
	}

	public void addDossier(String dossier) {
		dossiers.add(dossier);
		updateView();
	}

	public void removeDossier(String dossier) {
		dossiers.remove(dossier);
		updateView();
	}

	private void updateView() {
		((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
	}
}
