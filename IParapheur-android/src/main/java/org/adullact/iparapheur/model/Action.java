package org.adullact.iparapheur.model;

import org.adullact.iparapheur.R;

/**
* Created by jmaire on 02/12/2013.
*/
public enum Action
{
    // TODO : all possible actions (secretariat, supprimer, ...)
    VISA(R.string.action_viser,
            R.id.action_visa,
            R.drawable.iw_visa,
            R.drawable.ip_visa),

    SIGNATURE(R.string.action_signer,
            R.id.action_signature,
            R.drawable.iw_signature,
            R.drawable.ip_signature),

    TDT(R.string.action_tdt,
            R.id.action_tdt,
            R.drawable.iw_visa,
            R.drawable.ip_visa),

    ARCHIVAGE(R.string.action_archiver,
            R.id.action_archivage,
            R.drawable.iw_archivage,
            R.drawable.iw_archivage),

    MAILSEC(R.string.action_mailsec,
            R.id.action_mailsec,
            R.drawable.iw_visa,
            R.drawable.ip_visa),

    REJET(R.string.action_rejeter,
            R.id.action_rejet,
            R.drawable.ic_action_reject,
            R.drawable.ic_action_reject),

    SECRETARIAT(R.string.action_secretariat,R.id.action_secretariat,-1, -1),
    REMORD(R.string.action_remord,R.id.action_remord,-1, -1),
    AVIS_COMPLEMENTAIRE(R.string.action_avis,R.id.action_avis,-1, -1),
    TRANSFERT_SIGNATURE(R.string.action_transfertsign,R.id.action_transfert_signature,-1, -1),
    AJOUT_SIGNATURE(R.string.action_ajoutsign,R.id.action_ajout_signature,-1, -1),
    MAIL(R.string.action_mail,R.id.action_mail,-1, -1),
    ENREGISTRER(R.string.action_enregistrer,R.id.action_enregistrer,-1, -1),
    SUPPRIMER(R.string.action_supprimer,R.id.action_supprimer,-1, -1);



    private final int icon;
    private final int approvedIcon;
    private final int title;
    private final int menuItemId;

    Action(int title, int menuItemId, int icon, int approvedIcon) {
        this.title = title;
        this.menuItemId = menuItemId;
        this.icon = icon;
        this.approvedIcon = approvedIcon;
    }

    public int getIcon(boolean approved) {
        return approved? approvedIcon : icon;
    }

    public int getTitle() {
        return title;
    }

    public int getMenuItemId() {
        return menuItemId;
    }
}
