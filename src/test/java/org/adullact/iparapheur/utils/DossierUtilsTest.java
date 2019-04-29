/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.utils;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Circuit;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.adullact.iparapheur.model.Action.EMAIL;
import static org.adullact.iparapheur.model.Action.ENREGISTRER;
import static org.adullact.iparapheur.model.Action.JOURNAL;
import static org.adullact.iparapheur.model.Action.REJET;
import static org.adullact.iparapheur.model.Action.VISA;


@RunWith(RobolectricTestRunner.class)
public class DossierUtilsTest {


    @Test public void areDetailsAvailable() {

        List<EtapeCircuit> etapes = new ArrayList<>();
        etapes.add(new EtapeCircuit(null, true, false, "Bureau 01", "Signataire 01", Action.SIGNATURE.toString(), null));
        etapes.add(new EtapeCircuit(null, false, true, null, null, Action.VISA.toString(), null));
        Circuit circuit = new Circuit(etapes, "PKCS#7\\/single", true);

        ArrayList<Document> documentList = new ArrayList<>();
        documentList.add(new Document("id_01", null, 0, false, false));
        documentList.add(new Document("id_02", null, 0, true, false));

        Dossier dossier01 = new Dossier(null, null, null, null, null, null, null, null, false);
        dossier01.setCircuit(circuit);
        dossier01.setDocumentList(documentList);

        Dossier dossier02 = new Dossier(null, null, null, null, null, null, null, null, false);
        dossier02.setCircuit(circuit);

        Dossier dossier03 = new Dossier(null, null, null, null, null, null, null, null, false);
        dossier03.setDocumentList(documentList);

        // Checks

        Assert.assertTrue(DossierUtils.areDetailsAvailable(dossier01));
        Assert.assertFalse(DossierUtils.areDetailsAvailable(dossier02));
        Assert.assertFalse(DossierUtils.areDetailsAvailable(dossier03));
    }


    @SuppressWarnings("ConstantConditions") @Test public void findCurrentDocument() {

        ArrayList<Document> documentList = new ArrayList<>();
        documentList.add(new Document("id_01", null, 0, false, false));
        documentList.add(new Document("id_02", null, 0, true, false));

        Dossier dossier = new Dossier(null, null, null, null, null, null, null, null, false);
        dossier.setDocumentList(documentList);

        // Checks

        Assert.assertNull(DossierUtils.findCurrentDocument(null, "id_01"));
        Assert.assertEquals(DossierUtils.findCurrentDocument(dossier, null).getId(), "id_01");
        Assert.assertEquals(DossierUtils.findCurrentDocument(dossier, "id_01").getId(), "id_01");
        Assert.assertEquals(DossierUtils.findCurrentDocument(dossier, "id_02").getId(), "id_02");
    }


    @Test public void haveActions() {

        Dossier dossier01 = new Dossier(null, null, null, new HashSet<>(Arrays.asList(EMAIL, JOURNAL, ENREGISTRER)), null, null, null, null, false);
        Dossier dossier02 = new Dossier(null, null, null, new HashSet<>(Arrays.asList(VISA, EMAIL, JOURNAL, ENREGISTRER)), null, null, null, null, false);
        Dossier dossier03 = new Dossier(null, null, null, new HashSet<>(Arrays.asList(REJET, EMAIL, ENREGISTRER)), null, null, null, null, false);
        Dossier dossier04 = new Dossier(null, null, null, null, null, null, null, null, false);

        // Checks

        Assert.assertFalse(DossierUtils.haveActions(dossier01));
        Assert.assertTrue(DossierUtils.haveActions(dossier02));
        Assert.assertTrue(DossierUtils.haveActions(dossier03));
        Assert.assertFalse(DossierUtils.haveActions(dossier04));
    }


    @Test public void getMainDocumentsAndAnnexes() {

        Dossier emptyDossier = new Dossier(null, null, null, null, null, null, null, null, false);
        emptyDossier.setDocumentList(new ArrayList<Document>());

        ArrayList<Document> documentList = new ArrayList<>();
        documentList.add(new Document("id_01", null, 0, true, false));
        documentList.add(new Document("id_02", null, 0, false, false));
        documentList.add(new Document("id_03", null, 0, false, false));
        documentList.add(new Document("id_04", null, 0, true, false));

        Dossier dossier = new Dossier(null, null, null, null, null, null, null, null, false);
        dossier.setDocumentList(documentList);

        List<Document> mainDocumentList = DossierUtils.getMainDocuments(dossier);
        List<Document> annexesList = DossierUtils.getAnnexes(dossier);

        // Checks

        Assert.assertEquals(DossierUtils.getMainDocuments(emptyDossier), new ArrayList<Document>());
        Assert.assertEquals(DossierUtils.getAnnexes(emptyDossier), new ArrayList<Document>());

        Assert.assertEquals(mainDocumentList.size(), 2);
        Assert.assertEquals(mainDocumentList.get(0).getId(), "id_01");
        Assert.assertEquals(mainDocumentList.get(1).getId(), "id_04");

        Assert.assertEquals(annexesList.size(), 2);
        Assert.assertEquals(annexesList.get(0).getId(), "id_02");
        Assert.assertEquals(annexesList.get(1).getId(), "id_03");
    }


    @Test public void buildCreationDateComparator() {

        Dossier dossier01 = new Dossier("dossier01", null, null, null, null, null, new Date(50000L), null, false);
        Dossier dossier02 = new Dossier("dossier02", null, null, null, null, null, new Date(60000L), null, false);
        Dossier dossier03 = new Dossier("dossier03", null, null, null, null, null, new Date(70000L), null, false);
        Dossier dossier04 = new Dossier("dossier04", null, null, null, null, null, new Date(80000L), null, false);

        List<Dossier> dossierList = new ArrayList<>();
        dossierList.add(dossier03);
        dossierList.add(null);
        dossierList.add(dossier04);
        dossierList.add(dossier02);
        dossierList.add(dossier01);

        Collections.sort(dossierList, DossierUtils.buildCreationDateComparator());

        Assert.assertNull(dossierList.get(0));
        Assert.assertEquals(dossierList.get(1), dossier01);
        Assert.assertEquals(dossierList.get(2), dossier02);
        Assert.assertEquals(dossierList.get(3), dossier03);
        Assert.assertEquals(dossierList.get(4), dossier04);
    }

}