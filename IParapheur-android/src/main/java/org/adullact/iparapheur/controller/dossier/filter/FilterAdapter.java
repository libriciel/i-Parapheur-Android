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
import android.widget.ArrayAdapter;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.model.Filter;


public class FilterAdapter extends ArrayAdapter<Filter> {

	public FilterAdapter(Context context) {
		super(context, R.layout.activity_dossiers_toolbar_spinner_cell_main);
		setDropDownViewResource(R.layout.activity_dossiers_toolbar_spinner_cell_dropdown);
	}

	@Override public int getCount() {
		return MyFilters.INSTANCE.getFilters().size() + 1;
	}

	@Override public Filter getItem(int position) {
		Filter filter;
		if (position < MyFilters.INSTANCE.getFilters().size()) {
			filter = MyFilters.INSTANCE.getFilters().get(position);
		}
		else {
			filter = new Filter(Filter.EDIT_FILTER_ID);
			filter.setName(getContext().getResources().getString(R.string.action_filtrer));
		}
		return filter;
	}

	@Override public int getPosition(Filter item) {
		int position = MyFilters.INSTANCE.getFilters().indexOf(item);
		return (position == -1) ? MyFilters.INSTANCE.getFilters().size() + 1 : position;
	}

}
