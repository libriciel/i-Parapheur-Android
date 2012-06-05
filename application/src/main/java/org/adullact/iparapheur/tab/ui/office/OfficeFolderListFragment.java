package org.adullact.iparapheur.tab.ui.office;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import roboguice.fragment.RoboListFragment;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Folder;

public class OfficeFolderListFragment
        extends RoboListFragment
{

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.office_folderlist, container, false );
    }

    /* package */ static final class OfficeFolderListAdapter
            extends BaseAdapter
    {

        private final List<Folder> folderFiles;

        private LayoutInflater inflater;

        /* package */ OfficeFolderListAdapter( Context context, Collection<Folder> folderFiles )
        {
            this.inflater = LayoutInflater.from( context );
            this.folderFiles = new ArrayList<Folder>( folderFiles );
        }

        @Override
        public int getCount()
        {
            return folderFiles.size();
        }

        @Override
        public Object getItem( int position )
        {
            return folderFiles.get( position );
        }

        @Override
        public long getItemId( int position )
        {
            return position;
        }

        /**
         * The system calls this when it's time for the adapter to draw a list
         * item user interface for the first time.
         * 
         * @return The root of the item's layout.
         */
        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {

            ItemDataViewsHolder dataViews;
            if ( convertView == null ) {
                convertView = inflater.inflate( R.layout.office_folderlist_item, null );
                dataViews = new ItemDataViewsHolder();
                dataViews.icon = ( ImageView ) convertView.findViewById( R.id.office_folderlist_item_icon );
                dataViews.title = ( TextView ) convertView.findViewById( R.id.office_folderlist_item_title );
                convertView.setTag( dataViews );
            } else {
                dataViews = ( ItemDataViewsHolder ) convertView.getTag();
            }

            final Folder folder = folderFiles.get( position );

            dataViews.title.setText( folder.getTitle() );
            dataViews.icon.setImageResource( R.drawable.ic_list_document );

            return convertView;
        }

        private static class ItemDataViewsHolder
        {

            private ImageView icon;

            private TextView title;

        }

    }

}
