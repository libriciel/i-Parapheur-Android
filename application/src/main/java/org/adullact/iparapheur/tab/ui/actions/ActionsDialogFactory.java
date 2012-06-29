package org.adullact.iparapheur.tab.ui.actions;

import java.util.List;

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

@ContextSingleton
public class ActionsDialogFactory
{

    @Inject
    private Activity activity;

    @Inject
    private AccountsRepository accountsRepository;

    @Inject
    private IParapheurHttpClient iParapheurClient;

    public Dialog buildActionDialog( String accountIdentity, List<Folder> folders )
    {
        return buildDialog( accountIdentity, folders, true, null );
    }

    public Dialog buildRejectDialog( String accountIdentity, List<Folder> folders )
    {
        return buildDialog( accountIdentity, folders, false, null );
    }

    public Dialog buildActionDialog( String accountIdentity, List<Folder> folders, Intent successIntent )
    {
        return buildDialog( accountIdentity, folders, true, successIntent );
    }

    public Dialog buildRejectDialog( String accountIdentity, List<Folder> folders, Intent successIntent )
    {
        return buildDialog( accountIdentity, folders, false, successIntent );
    }

    private Dialog buildDialog( final String accountIdentity, final List<Folder> folders, boolean accept, final Intent successIntent )
    {
        if ( folders == null || folders.isEmpty() ) {
            throw new IllegalArgumentException( "Cannot build an Action Dialog without any Folder." );
        }
        boolean single = folders.size() == 1;
        Folder lambda = folders.get( 0 );
        if ( !lambda.requestedActionSupported() ) {
            throw new IllegalArgumentException( "Folder{" + lambda.getIdentity() + "} RequestedAction{" + lambda.getRequestedAction() + "} is UNSUPPORTED" );
        }

        LayoutInflater inflater = ( LayoutInflater ) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.folder_action_dialog, ( ViewGroup ) activity.findViewById( R.id.folder_action_dialog_layout_root ) );

        TextView folderTitle = ( TextView ) layout.findViewById( R.id.folder_action_dialog_title );
        folderTitle.setText( single ? lambda.getTitle() : "Lot de " + folders.size() + " dossiers" );

        final EditText pubAnnotation = ( EditText ) layout.findViewById( R.id.folder_annotation_public );
        final EditText privAnnotation = ( EditText ) layout.findViewById( R.id.folder_annotation_private );

        AlertDialog.Builder builder = new AlertDialog.Builder( activity );
        builder.setView( layout );
        builder.setCancelable( true );
        builder.setNeutralButton( "Annuler", null );
        switch ( lambda.getRequestedAction() ) {
            case SIGNATURE:
                if ( accept ) {
                    builder.setIcon( R.drawable.ic_action_sign );
                    builder.setTitle( "Signature" );
                    builder.setPositiveButton( single ? "Signer" : "Signer le lot", new DialogInterface.OnClickListener()
                    {

                        public void onClick( final DialogInterface dialog, int id )
                        {
                            doSign( dialog, accountIdentity, pubAnnotation.getText().toString(), privAnnotation.getText().toString(), successIntent, identities( folders ) );
                        }

                    } );
                } else {
                    builder.setIcon( R.drawable.ic_action_reject );
                    builder.setTitle( "Rejet de signature" );
                    builder.setPositiveButton( single ? "Rejeter" : "Rejeter le lot", new DialogInterface.OnClickListener()
                    {

                        public void onClick( final DialogInterface dialog, int id )
                        {
                            doReject( dialog, accountIdentity, pubAnnotation.getText().toString(), privAnnotation.getText().toString(), successIntent, identities( folders ) );
                        }

                    } );
                }
                break;
            case VISA:
                if ( accept ) {
                    builder.setIcon( R.drawable.ic_action_sign );
                    builder.setTitle( "Visa" );
                    builder.setPositiveButton( single ? "Viser" : "Viser le lot", new DialogInterface.OnClickListener()
                    {

                        public void onClick( final DialogInterface dialog, int id )
                        {
                            doVisa( dialog, accountIdentity, pubAnnotation.getText().toString(), privAnnotation.getText().toString(), successIntent, identities( folders ) );
                        }

                    } );
                } else {
                    builder.setIcon( R.drawable.ic_action_reject );
                    builder.setTitle( "Rejet de visa" );
                    builder.setPositiveButton( single ? "Rejeter" : "Rejeter le lot", new DialogInterface.OnClickListener()
                    {

                        public void onClick( final DialogInterface dialog, int id )
                        {
                            doReject( dialog, accountIdentity, pubAnnotation.getText().toString(), privAnnotation.getText().toString(), successIntent, identities( folders ) );
                        }

                    } );
                }
                break;
            default:
                throw new IParapheurTabException( "Unknown action '" + lambda.getRequestedAction() + "'. This should not happen." );
        }

        return builder.create();
    }

    private String[] identities( List<Folder> folders )
    {
        String[] identities = new String[ folders.size() ];
        int index = 0;
        for ( Folder folder : folders ) {
            identities[index] = folder.getIdentity();
            index++;
        }
        return identities;
    }

    private void doSign( final DialogInterface rejectDialog, final String accountIdentity, final String pubAnnotation, final String privAnnotation, final Intent successIntent, final String... folderIdentities )
    {
        new SignTask( activity, accountsRepository, iParapheurClient )
        {

            @Override
            protected void beforeDialogDismiss( AsyncTaskResult<Void, IParapheurHttpException> result )
            {
                if ( successIntent == null && activity instanceof Refreshable ) {
                    ( ( Refreshable ) activity ).refresh();
                }
                rejectDialog.dismiss();
            }

            @Override
            protected void afterDialogDismiss( AsyncTaskResult<Void, IParapheurHttpException> result )
            {
                if ( !result.hasError() && successIntent != null ) {
                    activity.startActivity( successIntent );
                    return;
                }
                if ( result.hasError() ) {
                    boolean single = folderIdentities.length == 1;
                    AlertDialog.Builder builder = new AlertDialog.Builder( context );
                    builder.setTitle( single ? "La signature de ce dossier a échoué" : "La signature de ce lot a échoué" ).
                            setMessage( result.buildErrorMessages() ).
                            setCancelable( false );
                    builder.setPositiveButton( "Réessayer", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog, int id )
                        {
                            dialog.dismiss();
                            doSign( rejectDialog, accountIdentity, pubAnnotation, privAnnotation, successIntent, folderIdentities );
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

        }.execute( new ActionTaskParam( accountIdentity, pubAnnotation, privAnnotation, folderIdentities ) );
    }

    private void doVisa( final DialogInterface rejectDialog, final String accountIdentity, final String pubAnnotation, final String privAnnotation, final Intent successIntent, final String... folderIdentities )
    {
        new VisaTask( activity, accountsRepository, iParapheurClient )
        {

            @Override
            protected void beforeDialogDismiss( AsyncTaskResult<Void, IParapheurHttpException> result )
            {
                if ( successIntent == null && activity instanceof Refreshable ) {
                    ( ( Refreshable ) activity ).refresh();
                }
                rejectDialog.dismiss();
            }

            @Override
            protected void afterDialogDismiss( AsyncTaskResult<Void, IParapheurHttpException> result )
            {
                if ( !result.hasError() && successIntent != null ) {
                    activity.startActivity( successIntent );
                    return;
                }
                if ( result.hasError() ) {
                    boolean single = folderIdentities.length == 1;
                    AlertDialog.Builder builder = new AlertDialog.Builder( context );
                    builder.setTitle( single ? "Le visa de ce dossier a échoué" : "Le visa de ce lot a échoué" ).
                            setMessage( result.buildErrorMessages() ).
                            setCancelable( false );
                    builder.setPositiveButton( "Réessayer", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog, int id )
                        {
                            dialog.dismiss();
                            doVisa( rejectDialog, accountIdentity, pubAnnotation, privAnnotation, successIntent, folderIdentities );
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

        }.execute( new ActionTaskParam( accountIdentity, pubAnnotation, privAnnotation, folderIdentities ) );
    }

    private void doReject( final DialogInterface rejectDialog, final String accountIdentity, final String pubAnnotation, final String privAnnotation, final Intent successIntent, final String... folderIdentities )
    {
        new RejectTask( activity, accountsRepository, iParapheurClient )
        {

            @Override
            protected void beforeDialogDismiss( AsyncTaskResult<Void, IParapheurHttpException> result )
            {
                if ( successIntent == null && activity instanceof Refreshable ) {
                    ( ( Refreshable ) activity ).refresh();
                }
                rejectDialog.dismiss();
            }

            @Override
            protected void afterDialogDismiss( AsyncTaskResult<Void, IParapheurHttpException> result )
            {
                if ( !result.hasError() && successIntent != null ) {
                    activity.startActivity( successIntent );
                    return;
                }
                if ( result.hasError() ) {
                    boolean single = folderIdentities.length == 1;
                    AlertDialog.Builder builder = new AlertDialog.Builder( context );
                    builder.setTitle( single ? "Le rejet de ce dossier a échoué" : "Le rejet de ce lot a échoué" ).
                            setMessage( result.buildErrorMessages() ).
                            setCancelable( false );
                    builder.setPositiveButton( "Réessayer", new DialogInterface.OnClickListener()
                    {

                        public void onClick( DialogInterface dialog, int id )
                        {
                            dialog.dismiss();
                            doReject( rejectDialog, accountIdentity, pubAnnotation, privAnnotation, successIntent, folderIdentities );
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

        }.execute( new ActionTaskParam( accountIdentity, pubAnnotation, privAnnotation, folderIdentities ) );
    }

}
