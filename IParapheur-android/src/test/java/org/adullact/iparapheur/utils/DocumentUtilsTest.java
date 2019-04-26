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

import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;


@RunWith(RobolectricTestRunner.class)
public class DocumentUtilsTest {


    @Test public void generateContentUrl() {

        Document doc01 = new Document("id_01", null, 0, false, false);
        Document doc02 = new Document("id_02", null, 0, false, true);
        Document doc03 = new Document(null, null, 0, false, false);

        // Checks

        Assert.assertEquals(DocumentUtils.generateContentUrl(doc01), "/api/node/workspace/SpacesStore/id_01/content");
        Assert.assertEquals(DocumentUtils.generateContentUrl(doc02), "/api/node/workspace/SpacesStore/id_02/content;ph:visuel-pdf");
        Assert.assertNull(DocumentUtils.generateContentUrl(doc03));
    }


    @Test public void isMainDocument() {

        Document doc01 = new Document("id_01", null, 0, false, false);
        Document doc02 = new Document("id_02", null, 0, true, false);
        Document doc03 = new Document("id_03", null, 0, false, false);

        ArrayList<Document> documentList01 = new ArrayList<>();
        documentList01.add(doc01);
        documentList01.add(doc02);

        Dossier dossier01 = new Dossier(null, null, null, null, null, null, null, null, false);
        dossier01.setDocumentList(documentList01);

        ArrayList<Document> documentList02 = new ArrayList<>();
        documentList02.add(doc01);
        documentList02.add(doc03);

        Dossier dossier02 = new Dossier(null, null, null, null, null, null, null, null, false);
        dossier02.setDocumentList(documentList02);

        Dossier dossier03 = new Dossier(null, null, null, null, null, null, null, null, false);

        // Checks

        Assert.assertFalse(DocumentUtils.isMainDocument(dossier01, doc01));
        Assert.assertTrue(DocumentUtils.isMainDocument(dossier01, doc02));

        Assert.assertTrue(DocumentUtils.isMainDocument(dossier02, doc01));
        Assert.assertFalse(DocumentUtils.isMainDocument(dossier02, doc02));

        Assert.assertFalse(DocumentUtils.isMainDocument(dossier03, doc01));
    }

}