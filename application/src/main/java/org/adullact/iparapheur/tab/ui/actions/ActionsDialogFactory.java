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
import android.widget.EditText;
import android.widget.TextView;

import roboguice.inject.ContextSingleton;

import com.google.inject.Inject;

import org.codeartisans.android.toolbox.os.AsyncTaskResult;

import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.splashscreen.SplashScreenActivity;

@ContextSingleton
public class ActionsDialogFactory
{

    @Inject
    private Activity activity;

    @Inject
    private AccountsRepository accountsRepository;

    @Inject
    private IParapheurHttpClient iParapheurClient;

    public Dialog buildActionDialog( String accountIdentity, Folder folder )
    {
        return buildDialog( accountIdentity, folder, true );
    }

    public Dialog buildRejectDialog( String accountIdentity, Folder folder )
    {
        return buildDialog( accountIdentity, folder, false );
    }

    private Dialog buildDialog( final String accountIdentity, final Folder folder, boolean accept )
    {
        if ( !folder.requestedActionSupported() ) {
            throw new IllegalArgumentException( "Folder{" + folder.getIdentity() + "} RequestedAction{" + folder.getRequestedAction() + "} is UNSUPPORTED" );
        }

        LayoutInflater inflater = ( LayoutInflater ) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.folder_sign_dialog, ( ViewGroup ) activity.findViewById( R.id.folder_sign_dialog_layout_root ) );

        TextView folderTitle = ( TextView ) layout.findViewById( R.id.folder_sign_dialog_title );
        folderTitle.setText( folder.getTitle() );

        final EditText pubAnnotation = ( EditText ) layout.findViewById( R.id.folder_annotation_public );
        final EditText privAnnotation = ( EditText ) layout.findViewById( R.id.folder_annotation_private );

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

                        public void onClick( final DialogInterface dialog, int id )
                        {
                            activity.startActivity( new Intent( activity, SplashScreenActivity.class ) );
                        }

                    } );
                } else {
                    builder.setIcon( R.drawable.ic_action_reject );
                    builder.setTitle( "Rejet de signature" );
                    builder.setPositiveButton( "Rejeter", new DialogInterface.OnClickListener()
                    {

                        public void onClick( final DialogInterface dialog, int id )
                        {
                            doReject( dialog, accountIdentity, pubAnnotation.getText().toString(), privAnnotation.getText().toString(), folder.getIdentity() );
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

                        public void onClick( final DialogInterface dialog, int id )
                        {
                            activity.startActivity( new Intent( activity, SplashScreenActivity.class ) );
                        }

                    } );
                } else {
                    builder.setIcon( R.drawable.ic_action_reject );
                    builder.setTitle( "Rejet de visa" );
                    builder.setPositiveButton( "Rejeter", new DialogInterface.OnClickListener()
                    {

                        public void onClick( final DialogInterface dialog, int id )
                        {
                            doReject( dialog, accountIdentity, pubAnnotation.getText().toString(), privAnnotation.getText().toString(), folder.getIdentity() );
                        }

                    } );
                }
                break;
            default:
                throw new IParapheurTabException( "Unknown action '" + folder.getRequestedAction() + "'. This should not happen." );
        }

        return builder.create();
    }

    private void doReject( final DialogInterface rejectDialog, final String accountIdentity, final String pubAnnotation, final String privAnnotation, final String folderIdentity )
    {
        new RejectTask( activity, accountsRepository, iParapheurClient )
        {

            @Override
            protected void beforeDialogDismiss( AsyncTaskResult<Void, IParapheurHttpException> result )
            {
                if ( activity instanceof Refreshable ) {
                    ( ( Refreshable ) activity ).refresh();
                }
                rejectDialog.dismiss();
            }

            @Override
            protected void afterDialogDismiss( AsyncTaskResult<Void, IParapheurHttpException> result )
            {
                if ( result.hasError() ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder( context );
                    builder.setTitle( "Le rejet de ce dossier a échoué" ).
                            setMessage( result.buildErrorMessages() ).
                            setCancelable( false );
                    builder.setPositiveButton( "Réessayer", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog, int id )
                        {
                            dialog.dismiss();
                            doReject( rejectDialog, accountIdentity, pubAnnotation, privAnnotation, folderIdentity );
                        }

                    } );
                    builder.setNegativeButton( "Annuler", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog, int id )
                        {
                            if ( activity instanceof Refreshable ) {
                                ( ( Refreshable ) activity ).refresh();
                            }
                            dialog.dismiss();
                            rejectDialog.dismiss();
                        }

                    } );
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }

        }.execute( new ActionTaskParam( accountIdentity, pubAnnotation, privAnnotation, folderIdentity ) );
    }

}
