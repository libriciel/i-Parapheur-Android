package org.adullact.iparapheur.tab.ui.actions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.ui.splashscreen.SplashScreenActivity;

@ContextSingleton
public class ActionsDialogFactory
{

    @Inject
    private Activity activity;

    public Dialog buildActionDialog( Folder folder )
    {
        return buildDialog( folder, true );
    }

    public Dialog buildRejectDialog( Folder folder )
    {
        return buildDialog( folder, false );
    }

    private Dialog buildDialog( final Folder folder, boolean accept )
    {
        LayoutInflater inflater = ( LayoutInflater ) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.folder_sign_dialog, ( ViewGroup ) activity.findViewById( R.id.folder_sign_dialog_layout_root ) );
        TextView folderTitle = ( TextView ) layout.findViewById( R.id.folder_sign_dialog_title );
        folderTitle.setText( folder.getTitle() );
        AlertDialog.Builder builder = new AlertDialog.Builder( activity );
        builder.setView( layout );
        builder.setCancelable( true );
        builder.setNeutralButton( "Annuler", null );
        switch ( folder.getRequestedAction() ) {
            case SIGNATURE:
                if ( accept ) {
                    builder.setIcon( R.drawable.ic_action_sign );
                    builder.setTitle( "Signature" );
                    builder.setPositiveButton( "Signer", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface di, int i )
                        {
                            activity.startActivity( new Intent( activity, SplashScreenActivity.class ) );
                        }

                    } );
                } else {
                    builder.setIcon( R.drawable.ic_action_reject );
                    builder.setTitle( "Rejet de demande de signature" );
                    builder.setPositiveButton( "Rejeter", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface di, int i )
                        {
                            activity.startActivity( new Intent( activity, SplashScreenActivity.class ) );
                        }

                    } );
                }
                break;
            case VISA:
                if ( accept ) {
                    builder.setIcon( R.drawable.ic_action_sign );
                    builder.setTitle( "Visa" );
                    builder.setPositiveButton( "Viser", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface di, int i )
                        {
                            activity.startActivity( new Intent( activity, SplashScreenActivity.class ) );
                        }

                    } );
                } else {
                    builder.setIcon( R.drawable.ic_action_reject );
                    builder.setTitle( "Rejet de demande de visa" );
                    builder.setPositiveButton( "Rejeter", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface di, int i )
                        {
                            activity.startActivity( new Intent( activity, SplashScreenActivity.class ) );
                        }

                    } );
                }
                break;
            default:
                throw new IParapheurTabException( "Unknown action '" + folder.getRequestedAction() + "'. This should not happen." );
        }

        return builder.create();
    }

}
