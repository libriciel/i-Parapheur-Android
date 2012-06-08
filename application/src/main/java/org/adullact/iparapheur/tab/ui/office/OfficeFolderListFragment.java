package org.adullact.iparapheur.tab.ui.office;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import roboguice.fragment.RoboListFragment;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Folder;

public class OfficeFolderListFragment
        extends RoboListFragment
{

    public static interface OnFolderDisplayRequestListener
    {

        void onFolderDisplayRequest( Folder folder );

    }

    public static interface OnFolderSelectionChange
    {

        void onFolderSelectionChange( List<Folder> selectedFolders );

    }

    private OnFolderDisplayRequestListener onFolderDisplayRequestListener;

    private OnFolderSelectionChange onFolderSelectionChangeListener;

    public void setOnFolderDisplayRequestListener( OnFolderDisplayRequestListener listener )
    {
        this.onFolderDisplayRequestListener = listener;
    }

    public void setOnFolderSelectionChangeListener( OnFolderSelectionChange onFolderSelectionChangeListener )
    {
        this.onFolderSelectionChangeListener = onFolderSelectionChangeListener;
    }

    public void shadeFolder( Folder... folders )
    {
        List<Folder> folderList = Arrays.asList( folders );
        for ( int index = 0; index < getListView().getChildCount(); index++ ) {
            View listChild = getListView().getChildAt( index );
            View checkbox = listChild.findViewById( R.id.office_folderlist_item_checkbox );
            if ( folderList.contains( ( Folder ) checkbox.getTag() ) ) {
                System.out.println( folderList + " CONTAINS " + checkbox.getTag() );
                listChild.setBackgroundResource( R.color.grey );
            } else {
                System.out.println( folderList + " DOES'NT CONTAIN " + checkbox.getTag() );
                listChild.setBackgroundResource( R.color.white );
            }
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.office_folderlist, container, false );
    }

    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );
        getListView().setOnItemClickListener( new AdapterView.OnItemClickListener()
        {

            public void onItemClick( AdapterView<?> av, View view, int i, long l )
            {
                if ( onFolderDisplayRequestListener != null ) {
                    CheckBox checkbox = ( CheckBox ) view.findViewById( R.id.office_folderlist_item_checkbox );
                    Folder folder = ( Folder ) checkbox.getTag();
                    onFolderDisplayRequestListener.onFolderDisplayRequest( folder );
                }
            }

        } );
    }

    /* package */ static final class OfficeFolderListAdapter
            extends BaseAdapter
    {

        private final OfficeFolderListFragment listFragment;

        private final LayoutInflater inflater;

        private final List<Folder> folderFiles;

        private final List<Folder> selectedFolders = new ArrayList<Folder>();


        /* package */ OfficeFolderListAdapter( OfficeFolderListFragment listFragment, Collection<Folder> folderFiles )
        {
            this.listFragment = listFragment;
            this.inflater = LayoutInflater.from( listFragment.getActivity() );
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

            final ItemDataViewsHolder dataViews;
            if ( convertView == null ) {
                convertView = inflater.inflate( R.layout.office_folderlist_item, null );
                dataViews = new ItemDataViewsHolder();
                dataViews.icon = ( ImageView ) convertView.findViewById( R.id.office_folderlist_item_icon );
                dataViews.title = ( TextView ) convertView.findViewById( R.id.office_folderlist_item_title );
                dataViews.checkbox = ( CheckBox ) convertView.findViewById( R.id.office_folderlist_item_checkbox );
                convertView.setTag( dataViews );
            } else {
                dataViews = ( ItemDataViewsHolder ) convertView.getTag();
            }

            final Folder folder = folderFiles.get( position );

            dataViews.title.setText( folder.getTitle() );
            dataViews.icon.setImageResource( R.drawable.ic_folder );
            dataViews.checkbox.setTag( folder );
            dataViews.checkbox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
            {

                public void onCheckedChanged( CompoundButton checkbox, boolean checked )
                {
                    if ( checkbox.getTag() instanceof Folder ) {
                        Folder folder = ( Folder ) checkbox.getTag();
                        if ( checked ) {
                            selectedFolders.add( folder );
                        } else {
                            selectedFolders.remove( folder );
                        }
                    }
                    if ( listFragment.onFolderSelectionChangeListener != null ) {
                        listFragment.onFolderSelectionChangeListener.onFolderSelectionChange( selectedFolders );
                    }
                }

            } );

            return convertView;
        }

        private static class ItemDataViewsHolder
        {

            private ImageView icon;

            private TextView title;

            private CheckBox checkbox;

        }

    }

}
