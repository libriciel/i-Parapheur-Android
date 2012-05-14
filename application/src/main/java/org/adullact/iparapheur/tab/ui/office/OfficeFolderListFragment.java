package org.adullact.iparapheur.tab.ui.office;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import com.google.inject.Inject;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.AbstractFolderFile;
import org.adullact.iparapheur.tab.model.FolderDocument;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;

public class OfficeFolderListFragment
        extends RoboListFragment
{

    @Inject
    private IParapheurHttpClient client;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        String officeIdentity = getActivity().getIntent().getExtras().getString( OfficeActivity.EXTRA_OFFICE_IDENTITY );
        // TODO
        setListAdapter( new OfficeListAdapter( getActivity(), Collections.<AbstractFolderFile>emptyList() ) );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.office_folderlist, container, false );
    }

    private static final class OfficeListAdapter
            extends BaseAdapter
    {

        private final List<AbstractFolderFile> folderFiles;

        private LayoutInflater inflater;

        private OfficeListAdapter( Context context, Collection<AbstractFolderFile> folderFiles )
        {
            this.inflater = LayoutInflater.from( context );
            this.folderFiles = new ArrayList<AbstractFolderFile>( folderFiles );
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

            final AbstractFolderFile folderFile = folderFiles.get( position );

            dataViews.title.setText( folderFile.getTitle() );
            if ( folderFile instanceof FolderDocument ) {
                convertView.setBackgroundResource( R.color.grey );
                dataViews.icon.setImageResource( R.drawable.ic_list_document );
            } else {
                dataViews.icon.setImageResource( R.drawable.ic_list_annex );
            }

            return convertView;
        }

        private static class ItemDataViewsHolder
        {

            private ImageView icon;

            private TextView title;

        }

    }

}
