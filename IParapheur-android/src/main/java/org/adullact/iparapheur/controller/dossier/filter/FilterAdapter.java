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

	@Override
	public int getCount() {
		return MyFilters.INSTANCE.getFilters().size() + 1;
	}

	@Override
	public Filter getItem(int position) {
		Filter filter;
		if (position < MyFilters.INSTANCE.getFilters().size()) {
			filter = MyFilters.INSTANCE.getFilters().get(position);
		}
		else {
			filter = new Filter(Filter.EDIT_FILTER_ID);
			filter.setNom(getContext().getResources().getString(R.string.action_filtrer));
		}
		return filter;
	}

	@Override
	public int getPosition(Filter item) {
		int position = MyFilters.INSTANCE.getFilters().indexOf(item);
		return (position == -1) ? MyFilters.INSTANCE.getFilters().size() + 1 : position;
	}

}
