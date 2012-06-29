package org.adullact.iparapheur.tab.ui.office;

import java.text.SimpleDateFormat;
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
import android.widget.Toast;

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
                // System.out.println( folderList + " CONTAINS " + checkbox.getTag() ); // TODO Make it a DEBUG level log
                listChild.setBackgroundResource( R.color.grey );
            } else {
                // System.out.println( folderList + " DOES'NT CONTAIN " + checkbox.getTag() ); // TODO Make it a DEBUG level log
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
                dataViews.checkbox = ( CheckBox ) convertView.findViewById( R.id.office_folderlist_item_checkbox );
                dataViews.icon = ( ImageView ) convertView.findViewById( R.id.office_folderlist_item_icon );
                dataViews.title = ( TextView ) convertView.findViewById( R.id.office_folderlist_item_title );
                dataViews.details = ( TextView ) convertView.findViewById( R.id.office_folderlist_item_details );
                dataViews.date = ( TextView ) convertView.findViewById( R.id.office_folderlist_item_date );
                convertView.setTag( dataViews );
            } else {
                dataViews = ( ItemDataViewsHolder ) convertView.getTag();
            }

            final Folder folder = folderFiles.get( position );

            dataViews.checkbox.setTag( folder );
            dataViews.checkbox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
            {

                public void onCheckedChanged( CompoundButton checkbox, boolean checked )
                {
                    if ( checkbox.getTag() instanceof Folder ) {
                        Folder folder = ( Folder ) checkbox.getTag();
                        if ( checked ) {
                            if ( !folder.requestedActionSupported() ) {
                                // Ensure suported action
                                checkbox.setChecked( false );
                                Toast.makeText( listFragment.getActivity(), "Vous ne pouvez pas séléctionner des dossiers dont les actions demandées ne sont pas supportées.", Toast.LENGTH_SHORT ).show();
                                return;
                            } else if ( !selectedFolders.isEmpty() ) {
                                // Ensure same bulk action
                                if ( selectedFolders.get( 0 ).getRequestedAction() != folder.getRequestedAction() ) {
                                    checkbox.setChecked( false );
                                    Toast.makeText( listFragment.getActivity(), "Vous ne pouvez pas séléctionner des dossiers dont les actions demandées sont différentes.", Toast.LENGTH_SHORT ).show();
                                    return;
                                }
                            }
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
            dataViews.icon.setImageResource( R.drawable.ic_folder );
            dataViews.title.setText( folder.getTitle() );
            StringBuilder details = new StringBuilder();
            details.append( folder.getBusinessType() ).append( " / " ).append( folder.getBusinessSubType() );
            dataViews.details.setText( details );
            if ( folder.getDueDate() == null ) {
                dataViews.date.setText( new SimpleDateFormat( "dd MMM" ).format( folder.getCreationDate() ) );
                dataViews.date.setTextColor( R.color.black );
            } else {
                dataViews.date.setText( new SimpleDateFormat( "dd MMM" ).format( folder.getDueDate() ) );
                dataViews.date.setTextColor( R.color.red );
            }

            return convertView;
        }

        private static class ItemDataViewsHolder
        {

            private CheckBox checkbox;

            private ImageView icon;

            private TextView title;

            private TextView details;

            private TextView date;

        }

    }

}
