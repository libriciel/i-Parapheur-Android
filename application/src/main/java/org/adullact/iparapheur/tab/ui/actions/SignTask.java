package org.adullact.iparapheur.tab.ui.actions;

import android.app.Activity;
import android.security.KeyChain;
import de.akquinet.android.androlog.Log;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.SignInfo;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.services.IParapheurHttpException;
import org.adullact.iparapheur.tab.util.SignUtils;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;

import java.security.PrivateKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

public class SignTask extends ActionTask {

    public SignTask( Activity context, AccountsRepository accountsRepository, IParapheurHttpClient iParapheurClient )
    {
        super( context, accountsRepository, iParapheurClient );
    }

    @Override
    protected AsyncTaskResult<Void, IParapheurHttpException> doInBackground( ActionTaskParam... parameters )
    {
        publishProgress( context.getResources().getString( R.string.actions_sign_in_progress ) );
        try {

            ActionTaskParam params = parameters[0];
            Account account = accountsRepository.byIdentity( params.accountIdentity );
            Log.d( context, "Will sign folder with params: " + params + " using account: " + account );
            if (params.keyAlias == null) {
                return new AsyncTaskResult<Void, IParapheurHttpException>( new IParapheurHttpException("The certificate used to sign is null"));
            }
            PrivateKey privateKey = KeyChain.getPrivateKey(context, params.keyAlias);
            // TODO : verify certificate (self-signed, CRL, expiration, ...)
            if (privateKey == null) {
                Log.w( "debug", "Private key for " + params.keyAlias + " is null");
                return new AsyncTaskResult<Void, IParapheurHttpException>( new IParapheurHttpException("Le certificat sélectionné ne permet pas la signature électronique.") );
            }
            Log.i("debug", "[SignTask] private key used : " + params.keyAlias);
            Log.i("debug", "[SignTask] Signature algorithms availiable : " + Security.getAlgorithms("Signature"));
            List<SignInfo> FoldersSignInfo = iParapheurClient.getSignaturesInfo(account, params.folderIdentities);
            List<String> signatures = new ArrayList<String>();
            for (SignInfo folderSignInfo : FoldersSignInfo) {
                // TODO : other signature formats with switch (Xades, ...)
                if (folderSignInfo.getFormat() == SignInfo.Format.CMS) {
                    signatures.add(SignUtils.CMSSign(folderSignInfo.getHash(), privateKey));
                    Log.i("debug", "Signature of " + folderSignInfo.getDossierRef() + " ok");
                }
            }
            // TODO : use params certificate to get signatures
            iParapheurClient.sign(account, params.pubAnnotation, params.privAnnotation, params.officeIdentity, signatures, params.folderIdentities);
            sleep(1);
            return new AsyncTaskResult<Void, IParapheurHttpException>( ( Void ) null );

        } catch ( Exception ex ) {
            Log.w( context, "Unable to sign folder, will return an error.", ex );
            return new AsyncTaskResult<Void, IParapheurHttpException>( new IParapheurHttpException(ex) );

        }
    }

}
