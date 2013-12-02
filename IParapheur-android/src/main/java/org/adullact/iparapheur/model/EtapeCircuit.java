package org.adullact.iparapheur.model;

import java.util.Date;

/**
 * Created by jmaire on 06/11/2013.
 */
public class EtapeCircuit
{

    private final Date dateValidation;

    private final boolean isApproved;

    private final String bureauName;

    private final String signataire;

    private final Action action;

    private final String publicAnnotation;

    public EtapeCircuit(Date dateValidation, boolean isApproved, String bureauName, String signataire, Action action, String publicAnnotation)
    {
        this.dateValidation = dateValidation;
        this.isApproved = isApproved;
        this.bureauName = bureauName;
        this.signataire = signataire == null ? "" : signataire;
        this.action = action;
        this.publicAnnotation = publicAnnotation;
    }

    public Date getDateValidation() {
        return dateValidation;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public String getBureauName() {
        return bureauName;
    }

    public String getSignataire() {
        return signataire;
    }

    public Action getAction() {
        return action;
    }

    public String getPublicAnnotation() {
        return publicAnnotation;
    }

    @Override
    public String toString()
    {
        return bureauName;
    }

}