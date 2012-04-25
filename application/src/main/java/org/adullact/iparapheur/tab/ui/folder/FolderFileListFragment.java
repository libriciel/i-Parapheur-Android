package org.adullact.iparapheur.tab.ui.folder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.AbstractFolderFile;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.model.FolderAnnex;
import org.adullact.iparapheur.tab.model.FolderDocument;
import org.adullact.iparapheur.tab.model.FolderRequestedAction;

public class FolderFileListFragment
        extends ListFragment
{

    /**
     * The system calls this when creating the fragment.
     * 
     * Initialize essential components of the fragment that will be kept when
     * the fragment is paused or stopped, then resumed.
     */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Folder folder = new Folder( "S�ance ordinaire du conseil municipal", FolderRequestedAction.SIGNATURE, "Achat", "Mat�riel" );
        folder.addAllDocuments( new FolderDocument[]{
                    new FolderDocument( "S�ance ordinaire du conseil municipal", "file:///android_asset/index.html" )
                } );
        folder.addAllAnnexes( new FolderAnnex[]{
                    new FolderAnnex( "Projet de r�habilitation", "file:///android_asset/index.html" ),
                    new FolderAnnex( "Transcript", "file:///android_asset/index.html" )
                } );
        setListAdapter( new FolderListAdapter( getActivity(), folder.getAllFiles() ) );
    }

    /**
     * The system calls this when it's time for the fragment to draw its user
     * interface for the first time.
     * 
     * @return The root of the fragment's layout.
     */
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.folder_filelist, container, false );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );
    }

    /**
     * The system calls this method as the first indication that the user is
     * leaving the fragment (though it does not always mean the fragment is
     * being destroyed).
     * 
     * This is usually where you should commit any changes that should be
     * persisted beyond the current user session (because the user might not
     * come back).
     */
    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        System.out.println( "ListItemClicked: " + position );
        // TODO Handle properly
    }

    private static final class FolderListAdapter
            extends BaseAdapter
    {

        private final List<AbstractFolderFile> folderFiles;

        private LayoutInflater inflater;

        private FolderListAdapter( Context context, Collection<AbstractFolderFile> folderFiles )
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
