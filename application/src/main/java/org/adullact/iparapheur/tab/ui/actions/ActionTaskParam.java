package org.adullact.iparapheur.tab.ui.actions;

import java.io.Serializable;

public class ActionTaskParam
        implements Serializable
{

    public static final long _serialVersionUID = 1L;

    public final String accountIdentity;

    public final String pubAnnotation;

    public final String privAnnotation;
    
    public final String officeIdentity;

    public final String[] folderIdentities;

    public ActionTaskParam( String accountIdentity, String pubAnnotation, String privAnnotation, String officeIdentity, String... folderIdentities )
    {
        this.accountIdentity = accountIdentity;
        this.pubAnnotation = pubAnnotation;
        this.privAnnotation = privAnnotation;
        this.folderIdentities = folderIdentities;
        this.officeIdentity = officeIdentity;
    }

}
