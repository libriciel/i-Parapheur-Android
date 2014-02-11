package org.adullact.iparapheur.controller.dossier.filter;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.adullact.iparapheur.model.Filter;

/**
 * Created by jmaire on 06/02/2014.
 */
public class FilterAdapter extends ArrayAdapter<Filter>  {

    public FilterAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
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
            filter.setNom(Filter.EDIT_FILTER_NOM);
        }
        return filter;
    }

    @Override
    public int getPosition(Filter item) {
        int position = MyFilters.INSTANCE.getFilters().indexOf(item);
        return (position == -1)? MyFilters.INSTANCE.getFilters().size() + 1 : position;
    }

}
