package org.adullact.iparapheur.tab.ui.office;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.adullact.iparapheur.tab.model.Folder;

/* package */ class OfficeData
{

    /* package */ static final OfficeData EMPTY = new OfficeData( new TreeMap<String, List<String>>(), Collections.<Folder>emptyList() );

    private final SortedMap<String, List<String>> typology;

    private final List<Folder> folders;

    /* package */ OfficeData( SortedMap<String, List<String>> typology, List<Folder> folders )
    {
        this.typology = typology;
        this.folders = folders;
    }

    /* package */ SortedMap<String, List<String>> getTypology()
    {
        return typology;
    }

    /* package */ List<Folder> getFolders()
    {
        return folders;
    }

}
