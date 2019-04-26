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
package org.adullact.iparapheur.model;

import com.google.gson.Gson;

import org.adullact.iparapheur.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.adullact.iparapheur.model.Action.AVIS_COMPLEMENTAIRE;
import static org.adullact.iparapheur.model.Action.EMAIL;
import static org.adullact.iparapheur.model.Action.ENREGISTRER;
import static org.adullact.iparapheur.model.Action.JOURNAL;
import static org.adullact.iparapheur.model.Action.REJET;
import static org.adullact.iparapheur.model.Action.SECRETARIAT;
import static org.adullact.iparapheur.model.Action.SIGNATURE;
import static org.adullact.iparapheur.model.Action.TRANSFERT_SIGNATURE;
import static org.adullact.iparapheur.model.Action.VISA;


@RunWith(RobolectricTestRunner.class)
public class DossierTest {

    private static Gson sGson = CollectionUtils.buildGsonWithDateParser();


    @Test public void fromJsonArray() {

        // Parsed data

        String incorrectArrayJsonString = "[[{]   \"id\": \"Value 01\" , \"collectivite\": [\"Value 01-01\"  ]]";
        String correctArrayJsonString = "[{" +
                "    \"total\": 2," +
                "    \"protocol\": \"ACTES\"," +
                "    \"actionDemandee\": \"SIGNATURE\"," +
                "    \"isSent\": true," +
                "    \"type\": \"Type 01\"," +
                "    \"bureauName\": \"Bureau 01\"," +
                "    \"creator\": \"Creator 01\"," +
                "    \"id\": \"id_01\"," +
                "    \"title\": \"Title 01\"," +
                "    \"pendingFile\": 0," +
                "    \"banetteName\": \"Dossiers à traiter\"," +
                "    \"skipped\": 1," +
                "    \"sousType\": \"Subtype 01\"," +
                "    \"isSignPapier\": true," +
                "    \"isXemEnabled\": true," +
                "    \"hasRead\": true," +
                "    \"readingMandatory\": true," +
                "    \"documentPrincipal\": {" +
                "        \"id\": \"id_doc_01\"," +
                "        \"name\": \"Document 01\"" +
                "    }," +
                "    \"locked\": true," +
                "    \"actions\": [\"ENREGISTRER\", \"EMAIL\", \"JOURNAL\", \"SECRETARIAT\", \"REJET\", \"VISA\", \"SIGNATURE\", \"TRANSFERT_SIGNATURE\", \"AVIS_COMPLEMENTAIRE\"]," +
                "    \"isRead\": true," +
                "    \"dateEmission\": 1392829477205," +
                "    \"includeAnnexes\": true" +
                "}, {" +
                "    \"total\": 2," +
                "    \"type\": \"Type 02\"," +
                "    \"id\": \"id_02\"," +
                "    \"title\": \"Title 02\"," +
                "    \"sousType\": \"Subtype 02\"" +
                "}]";

        List<Dossier> incorrectArrayParsed = Dossier.fromJsonArray(incorrectArrayJsonString, sGson);
        List<Dossier> correctArrayParsed = Dossier.fromJsonArray(correctArrayJsonString, sGson);

        // Valid types

        HashSet<Action> dossier01ActionsSet = new HashSet<>(Arrays.asList(ENREGISTRER,
                EMAIL,
                JOURNAL,
                SECRETARIAT,
                REJET,
                SIGNATURE,
                TRANSFERT_SIGNATURE,
                AVIS_COMPLEMENTAIRE
        ));
        HashSet<Action> dossier02ActionsSet = new HashSet<>(Arrays.asList(VISA, SIGNATURE));

        Dossier dossier01 = new Dossier("id_01", "Title 01", SIGNATURE, dossier01ActionsSet, "Type 01", "Subtype 01", new Date(1392829477205L), null, true);
        Dossier dossier02 = new Dossier("id_02", "Title 02", VISA, dossier02ActionsSet, "Type 02", "Subtype 02", null, null, false);

        dossier01.setSyncDate(null);
        dossier02.setSyncDate(null);
        dossier01.setParent(null);
        dossier02.setParent(null);
        dossier01.setChildrenDocuments(null);
        dossier02.setChildrenDocuments(null);

        // Checks

        Assert.assertNull(incorrectArrayParsed);
        Assert.assertNotNull(correctArrayParsed);

        Assert.assertEquals(correctArrayParsed.get(0).toString(), dossier01.toString());
        Assert.assertEquals(correctArrayParsed.get(0).getId(), dossier01.getId());
        Assert.assertEquals(correctArrayParsed.get(0).getName(), dossier01.getName());
        Assert.assertEquals(correctArrayParsed.get(0).getActionDemandee(), dossier01.getActionDemandee());
        Assert.assertEquals(correctArrayParsed.get(0).getType(), dossier01.getType());
        Assert.assertEquals(correctArrayParsed.get(0).getSousType(), dossier01.getSousType());
        Assert.assertEquals(correctArrayParsed.get(0).getActions(), dossier01.getActions());
        Assert.assertEquals(correctArrayParsed.get(0).isSignPapier(), dossier01.isSignPapier());
        Assert.assertEquals(correctArrayParsed.get(0).getDocumentList(), dossier01.getDocumentList());
        Assert.assertEquals(correctArrayParsed.get(0).getDateLimite(), dossier01.getDateLimite());
        Assert.assertEquals(correctArrayParsed.get(0).getDateCreation(), dossier01.getDateCreation());
        Assert.assertEquals(correctArrayParsed.get(0).getSyncDate(), dossier01.getSyncDate());
        Assert.assertEquals(correctArrayParsed.get(0).getParent(), dossier01.getParent());
        Assert.assertEquals(correctArrayParsed.get(0).getChildrenDocuments(), dossier01.getChildrenDocuments());

        Assert.assertEquals(correctArrayParsed.get(1).toString(), dossier02.toString());
        Assert.assertEquals(correctArrayParsed.get(1).getId(), dossier02.getId());
        Assert.assertEquals(correctArrayParsed.get(1).getName(), dossier02.getName());
        Assert.assertEquals(correctArrayParsed.get(1).getActionDemandee(), dossier02.getActionDemandee());
        Assert.assertEquals(correctArrayParsed.get(1).getType(), dossier02.getType());
        Assert.assertEquals(correctArrayParsed.get(1).getSousType(), dossier02.getSousType());
        Assert.assertEquals(correctArrayParsed.get(1).getActions(), dossier02.getActions());
        Assert.assertEquals(correctArrayParsed.get(1).isSignPapier(), dossier02.isSignPapier());
        Assert.assertEquals(correctArrayParsed.get(1).getDocumentList(), dossier02.getDocumentList());
        Assert.assertEquals(correctArrayParsed.get(1).getDateLimite(), dossier02.getDateLimite());
        Assert.assertEquals(correctArrayParsed.get(1).getDateCreation(), dossier02.getDateCreation());
        Assert.assertEquals(correctArrayParsed.get(1).getSyncDate(), dossier02.getSyncDate());
        Assert.assertEquals(correctArrayParsed.get(1).getParent(), dossier02.getParent());
        Assert.assertEquals(correctArrayParsed.get(1).getChildrenDocuments(), dossier02.getChildrenDocuments());
    }


    @Test public void fromJsonObject() {

        // Parsed data

        String incorrectJsonString = "[[{]   \"id\": \"Value 01\" , \"collectivite\": [\"Value 01-01\"  ]]";
        String correctJsonString = "{" +
                "    \"title\": \"Dossier 01\"," +
                "    \"nomTdT\": \"pas de TdT\"," +
                "    \"includeAnnexes\": true," +
                "    \"locked\": null," +
                "    \"readingMandatory\": true," +
                "    \"acteursVariables\": []," +
                "    \"dateEmission\": 1455553363700," +
                "    \"visibility\": \"public\"," +
                "    \"isRead\": true," +
                "    \"actionDemandee\": \"VISA\"," +
                "    \"status\": null," +
                "    \"documents\": [{" +
                "        \"size\": 224260," +
                "        \"visuelPdf\": false," +
                "        \"isMainDocument\": true," +
                "        \"pageCount\": 1," +
                "        \"attestState\": 0," +
                "        \"id\": \"0ad04448-4424-416a-8e10-36a160b0cb9d\"," +
                "        \"name\": \"aaef91b4-d135-40ce-80dd-cb4ef1e0ffbc.pdf\"," +
                "        \"canDelete\": false," +
                "        \"isLocked\": false" +
                "    }, {" +
                "        \"id\": \"5321b0e1-e055-4cff-a69c-5cd358da12e1\"," +
                "        \"isLocked\": false," +
                "        \"attestState\": 0," +
                "        \"visuelPdf\": false," +
                "        \"size\": 16153," +
                "        \"canDelete\": false," +
                "        \"name\": \"20160205_1540_texte_reponse.odt\"," +
                "        \"isMainDocument\": false" +
                "    }]," +
                "    \"id\": \"id_01\"," +
                "    \"isSignPapier\": true," +
                "    \"dateLimite\": 1455553363700," +
                "    \"hasRead\": true," +
                "    \"isXemEnabled\": true," +
                "    \"actions\": [\"ENREGISTRER\", \"EMAIL\", \"JOURNAL\", \"REJET\", \"VISA\", \"TRANSFERT_ACTION\", \"AVIS_COMPLEMENTAIRE\", \"GET_ATTEST\"]," +
                "    \"banetteName\": \"Dossiers à traiter\"," +
                "    \"type\": \"Type 01\"," +
                "    \"canAdd\": true," +
                "    \"protocole\": \"aucun\"," +
                "    \"metadatas\": {" +
                "        \"cu:Canton\": {" +
                "            \"values\": [\"Castries\", \"Grabels\", \"Jacou\", \"Juvignac\", \"Mauguio\"]," +
                "            \"default\": \"Castries\"," +
                "            \"mandatory\": \"false\"," +
                "            \"value\": \"\"," +
                "            \"realName\": \"Canton concerné\"," +
                "            \"type\": \"STRING\"," +
                "            \"editable\": \"false\"" +
                "        }" +
                "    }," +
                "    \"xPathSignature\": null," +
                "    \"sousType\": \"SubType 01\"," +
                "    \"bureauName\": \"Bureau 01\"," +
                "    \"isSent\": true" +
                "}";

        Dossier incorrectObjectParsed = Dossier.fromJsonObject(incorrectJsonString, sGson);
        Dossier correctObjectParsed = Dossier.fromJsonObject(correctJsonString, sGson);

        // Checks

        Assert.assertNull(incorrectObjectParsed);
        Assert.assertNotNull(correctObjectParsed);

        Assert.assertEquals(correctObjectParsed.getId(), "id_01");
        Assert.assertEquals(correctObjectParsed.getName(), "Dossier 01");
        Assert.assertEquals(correctObjectParsed.getActionDemandee(), Action.VISA);
        Assert.assertEquals(correctObjectParsed.getType(), "Type 01");
        Assert.assertEquals(correctObjectParsed.getSousType(), "SubType 01");
        Assert.assertEquals(correctObjectParsed.getDateCreation(), new Date(1455553363700L));
        Assert.assertEquals(correctObjectParsed.getDateLimite(), new Date(1455553363700L));
        Assert.assertEquals(correctObjectParsed.getActions().size(), 9);
        Assert.assertTrue(correctObjectParsed.isSignPapier());
        Assert.assertEquals(correctObjectParsed.getDocumentList().size(), 2);
    }


    @SuppressWarnings({"ObjectEqualsNull"})
    @Test public void dossierEquals() {

        Dossier dossier01 = new Dossier("id_01", null, null, null, null, null, null, null, false);
        Dossier dossier01bis = new Dossier("id_01", null, null, null, null, null, null, null, false);
        Dossier dossier02 = new Dossier(null, "id_02", null, null, null, null, null, null, false);

        // Checks

        Assert.assertEquals(dossier01, dossier01bis);
        Assert.assertEquals("id_01", dossier01.getId());

        Assert.assertNotEquals(dossier01, dossier02);
        Assert.assertNotEquals(null, dossier01);
        Assert.assertNotEquals("id_02", dossier01);
        Assert.assertNotEquals(1, dossier01);
    }


    @Test public void dossierHashCode() {

        Dossier dossier01 = new Dossier("id_01", null, null, null, null, null, null, null, false);
        Dossier dossier02 = new Dossier(null, null, null, null, null, null, null, null, false);

        // Checks

        Assert.assertEquals(dossier01.hashCode(), "id_01".hashCode());
        Assert.assertEquals(dossier02.hashCode(), -1);
    }

}