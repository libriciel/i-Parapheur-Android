package org.adullact.iparapheur.tab.ui.dashboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.adullact.iparapheur.tab.R;

/**
 * Fluid GridView.
 * 
 * It is a TableLayout using the Adapter pattern.
 * It's not scrollable.
 * It's not meant to be extended.
 * 
 * To support different screen size, use the layout selection mecanism provided
 * by the platform and set the column count in layout files.
 */
public final class FluidGridView
        extends TableLayout
{

    public static interface OnItemClickListener
    {

        public void onItemClick( FluidGridView fluidGridView, View itemView, int position, long id );

    }

    private static final int DEFAULT_NUM_COLUMNS = 2;

    private final int numColumns;

    private ListAdapter adapter;

    private OnItemClickListener listener;

    public FluidGridView( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        setOrientation( LinearLayout.VERTICAL );
        TypedArray attributes = context.obtainStyledAttributes( attrs, R.styleable.FluidGridView );
        this.numColumns = attributes.getInteger( R.styleable.FluidGridView_num_columns, DEFAULT_NUM_COLUMNS );
    }

    public void setAdapter( ListAdapter adapter )
    {
        this.adapter = adapter;
        layout();
    }

    private void layout()
    {
        removeAllViews();
        int rows = adapter.getCount() / numColumns;
        if ( adapter.getCount() % numColumns > 0 ) {
            rows++;
        }
        if ( rows > 0 ) {
            TableRow currentRow = newTableRow();
            for ( int index = 0; index < adapter.getCount(); index++ ) {
                View view = adapter.getView( index, null, this );
                final int position = index;
                view.setOnClickListener( new OnClickListener()
                {

                    public void onClick( View view )
                    {
                        if ( listener != null ) {
                            listener.onItemClick( FluidGridView.this, view, position, view.getId() );
                        }
                    }

                } );
                currentRow.addView( view );
                if ( index != 0 && ( index + 1 ) % numColumns == 0 ) {
                    addRow( currentRow );
                    currentRow = newTableRow();
                }
            }
            if ( currentRow.getChildCount() > 0 ) {
                addRow( currentRow );
            }
        }
    }

    private TableRow newTableRow()
    {
        TableRow tr = new TableRow( getContext() );
        tr.setLayoutParams( new TableRow.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
        return tr;
    }

    private void addRow( TableRow row )
    {
        LayoutParams layoutParams = new TableLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
        layoutParams.setMargins( 0, 0, 0, 32 );
        addView( row, layoutParams );
    }

    public void setOnItemClickListener( OnItemClickListener listener )
    {
        this.listener = listener;
    }

}
