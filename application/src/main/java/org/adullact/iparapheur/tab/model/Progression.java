package org.adullact.iparapheur.tab.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.adullact.iparapheur.tab.model.Progression.Step;
import org.codeartisans.java.toolbox.Strings;

public class Progression
        extends ArrayList<Step>
        implements Serializable
{

    public static final long _serialVersionUID = 1L;

    private final String folderIdentity;

    private final String privAnnotation;

    public Progression( String folderIdentity, String privAnnotation )
    {
        super();
        this.folderIdentity = folderIdentity;
        this.privAnnotation = privAnnotation == null ? Strings.EMPTY : privAnnotation;
    }

    public String getFolderIdentity()
    {
        return folderIdentity;
    }

    public String getPrivateAnnotation()
    {
        return privAnnotation;
    }

    @Override
    public String toString()
    {
        return "Progression{" + "folderIdentity=" + folderIdentity + ", privAnnotation=" + privAnnotation + ", steps=" + Arrays.toString( toArray() ) + '}';
    }

    public static class Step
    {

        private final Date validationDate;

        private final boolean approved;

        private final String officeName;

        private final FolderRequestedAction action;

        private final String publicAnnotation;

        public Step( Date validationDate, boolean approved, String officeName, FolderRequestedAction action, String publicAnnotation )
        {
            this.validationDate = validationDate;
            this.approved = approved;
            this.officeName = officeName;
            this.action = action;
            this.publicAnnotation = publicAnnotation;
        }

        public FolderRequestedAction getAction()
        {
            return action;
        }

        public boolean isApproved()
        {
            return approved;
        }

        public String getOfficeName()
        {
            return officeName;
        }

        public String getPublicAnnotation()
        {
            return publicAnnotation;
        }

        public Date getValidationDate()
        {
            return validationDate;
        }

        @Override
        public String toString()
        {
            return "Step{" + "validationDate=" + validationDate + ", approved=" + approved + ", officeName=" + officeName + ", action=" + action + ", publicAnnotation=" + publicAnnotation + '}';
        }

    }

}
