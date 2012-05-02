package org.adullact.iparapheur.tab.ui.folder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

import de.akquinet.android.androlog.Log;

import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;

import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;
import org.adullact.iparapheur.tab.ui.splashscreen.SplashScreenActivity;

public class FolderActivity
        extends RoboFragmentActivity
{

    private static final String TAG = FolderActivity.class.getSimpleName();

    private static final int SIGN_DIALOG_ID = 101;

    private static final int REJECT_DIALOG_ID = 202;

    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;

    @Inject
    private ActionBarActivityObserver actionBarObserver;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Log.i( TAG, "onCreate" );
        super.onCreate( savedInstanceState );
        setTitle( "La Gironde > Arcachon > SŽance ordinaire du conseil municipal" );
        setContentView( R.layout.folder );

        // Find views
        TextView topSummaryLeft = ( TextView ) findViewById( R.id.folder_top_summary_left );
        TextView topSummaryRight = ( TextView ) findViewById( R.id.folder_top_summary_right );
        Button buttonSign = ( Button ) findViewById( R.id.folder_button_sign );
        Button buttonReject = ( Button ) findViewById( R.id.folder_button_reject );
        WebView fileWebView = ( WebView ) findViewById( R.id.folder_details_webview );

        // Set HTML to views from ressources
        topSummaryLeft.setText( Html.fromHtml( getString( R.string.demo_summary_left ) ) );
        topSummaryRight.setText( Html.fromHtml( getString( R.string.demo_summary_right ) ) );

        // Load document in WebView
        fileWebView.loadUrl( "file:///android_asset/index.html" );

        // Handle buttons
        buttonSign.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                showDialog( SIGN_DIALOG_ID );
            }

        } );
        buttonReject.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View view )
            {
                showDialog( REJECT_DIALOG_ID );
            }

        } );
    }

    @Override
    protected Dialog onCreateDialog( int id )
    {
        Dialog dialog;
        LayoutInflater inflater = ( LayoutInflater ) getSystemService( LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.folder_sign_dialog, ( ViewGroup ) findViewById( R.id.folder_sign_dialog_layout_root ) );
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setView( layout );
        builder.setCancelable( true );
        builder.setNeutralButton( "Annuler", null );

        switch ( id ) {
            case REJECT_DIALOG_ID:

                builder.setIcon( R.drawable.ic_action_reject );
                builder.setTitle( "Refus d'un dossier" );
                builder.setPositiveButton( "Refuser", new DialogInterface.OnClickListener()
                {

                    public void onClick( DialogInterface di, int i )
                    {
                        startActivity( new Intent( FolderActivity.this, SplashScreenActivity.class ) );
                    }

                } );
                dialog = builder.create();
                break;

            case SIGN_DIALOG_ID:

                builder.setIcon( R.drawable.ic_action_sign );
                builder.setTitle( "Signature d'un dossier" );
                builder.setPositiveButton( "Signer", new DialogInterface.OnClickListener()
                {

                    public void onClick( DialogInterface di, int i )
                    {
                        startActivity( new Intent( FolderActivity.this, SplashScreenActivity.class ) );
                    }

                } );
                dialog = builder.create();
                break;

            default:
                dialog = null;
        }
        return dialog;
    }

}
