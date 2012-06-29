package org.adullact.iparapheur.tab.ui.folder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import roboguice.fragment.RoboListFragment;

import de.akquinet.android.androlog.Log;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.AbstractFolderFile;
import org.adullact.iparapheur.tab.model.FolderDocument;

public class FolderFileListFragment
        extends RoboListFragment
{

    public static interface OnFileDisplayRequestListener
    {

        void onFileDisplayRequest( AbstractFolderFile file );

    }

    private OnFileDisplayRequestListener onFileDisplayRequestListener;

    public void setOnFileDisplayRequestListener( OnFileDisplayRequestListener onFileDisplayRequestListener )
    {
        this.onFileDisplayRequestListener = onFileDisplayRequestListener;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.folder_filelist, container, false );
    }

    @Override
    public void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        if ( onFileDisplayRequestListener != null ) {
            onFileDisplayRequestListener.onFileDisplayRequest( ( AbstractFolderFile ) getListAdapter().getItem( position ) );
        }
    }

    public void shadeFiles( AbstractFolderFile... files )
    {
        List<AbstractFolderFile> fileList = Arrays.asList( files );
        for ( int index = 0; index < getListView().getChildCount(); index++ ) {
            View listChild = getListView().getChildAt( index );
            View titleView = listChild.findViewById( R.id.folder_filelist_item_title );
            if ( fileList.contains( ( AbstractFolderFile ) titleView.getTag() ) ) {
                listChild.setBackgroundResource( R.color.grey );
            } else {
                listChild.setBackgroundResource( R.color.white );
            }
        }
    }

    /* package */ static final class FolderListAdapter
            extends BaseAdapter
    {

        private final List<AbstractFolderFile> folderFiles;

        private LayoutInflater inflater;

        /* package */ FolderListAdapter( Context context, Collection<AbstractFolderFile> folderFiles )
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
                convertView = inflater.inflate( R.layout.folder_filelist_item, null );
                dataViews = new ItemDataViewsHolder();
                dataViews.icon = ( ImageView ) convertView.findViewById( R.id.folder_filelist_item_icon );
                dataViews.title = ( TextView ) convertView.findViewById( R.id.folder_filelist_item_title );
                convertView.setTag( dataViews );
            } else {
                dataViews = ( ItemDataViewsHolder ) convertView.getTag();
            }

            final AbstractFolderFile folderFile = folderFiles.get( position );

            dataViews.title.setText( folderFile.getTitle() );
            dataViews.title.setTag( folderFile );
            if ( folderFile instanceof FolderDocument ) {
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
