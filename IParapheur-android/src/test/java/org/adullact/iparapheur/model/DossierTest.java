/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.model;

import android.text.TextUtils;

import com.google.gson.Gson;

import junit.framework.Assert;

import org.adullact.iparapheur.utils.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.adullact.iparapheur.model.Action.ARCHIVAGE;
import static org.adullact.iparapheur.model.Action.EMAIL;
import static org.adullact.iparapheur.model.Action.ENREGISTRER;
import static org.adullact.iparapheur.model.Action.JOURNAL;
import static org.adullact.iparapheur.model.Action.MAILSEC;
import static org.adullact.iparapheur.model.Action.REJET;
import static org.adullact.iparapheur.model.Action.SIGNATURE;
import static org.adullact.iparapheur.model.Action.TDT;
import static org.adullact.iparapheur.model.Action.TDT_ACTES;
import static org.adullact.iparapheur.model.Action.TDT_HELIOS;
import static org.adullact.iparapheur.model.Action.VISA;
import static org.mockito.Matchers.any;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class DossierTest {

	private static Gson sGson = CollectionUtils.buildGsonWithLongToDate();

	@Before public void setUp() throws Exception {
		PowerMockito.mockStatic(TextUtils.class);

		PowerMockito.when(TextUtils.equals(any(CharSequence.class), any(CharSequence.class))).thenAnswer(new Answer<Object>() {
			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
				CharSequence a = (CharSequence) invocation.getArguments()[0];
				CharSequence b = (CharSequence) invocation.getArguments()[1];
				return org.adullact.iparapheur.mock.TextUtils.equals(a, b);
			}
		});
	}

	@Test public void fromJsonArray() throws Exception {

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

		Set<Action> dossier01ActionsSet = CollectionUtils.asSet(Action.ENREGISTRER,
																EMAIL,
																JOURNAL,
																Action.SECRETARIAT,
																REJET,
																SIGNATURE,
																Action.TRANSFERT_SIGNATURE,
																Action.AVIS_COMPLEMENTAIRE
		);

		Dossier dossier01 = new Dossier("id_01", "Title 01", SIGNATURE, dossier01ActionsSet, "Type 01", "Subtype 01", new Date(1392829477205L), null, true);
		Dossier dossier02 = new Dossier("id_02", "Title 02", VISA, CollectionUtils.asSet(VISA, SIGNATURE), "Type 02", "Subtype 02", null, null, false);

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

	@Test public void fromJsonObject() throws Exception {

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
		Assert.assertEquals(correctObjectParsed.isSignPapier(), true);
		Assert.assertEquals(correctObjectParsed.getDocumentList().size(), 2);
	}

	// <editor-fold desc="Static utils">

	@Test public void areDetailsAvailable() throws Exception {

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

		Assert.assertTrue(Dossier.areDetailsAvailable(dossier01));
		Assert.assertFalse(Dossier.areDetailsAvailable(dossier02));
		Assert.assertFalse(Dossier.areDetailsAvailable(dossier03));
	}

	@SuppressWarnings("ConstantConditions") @Test public void findCurrentDocument() throws Exception {

		ArrayList<Document> documentList = new ArrayList<>();
		documentList.add(new Document("id_01", null, 0, false, false));
		documentList.add(new Document("id_02", null, 0, true, false));

		Dossier dossier = new Dossier(null, null, null, null, null, null, null, null, false);
		dossier.setDocumentList(documentList);

		// Checks

		Assert.assertNull(Dossier.findCurrentDocument(null, "id_01"));
		Assert.assertEquals(Dossier.findCurrentDocument(dossier, null).getId(), "id_01");
		Assert.assertEquals(Dossier.findCurrentDocument(dossier, "id_01").getId(), "id_01");
		Assert.assertEquals(Dossier.findCurrentDocument(dossier, "id_02").getId(), "id_02");
	}

	@Test public void haveActions() throws Exception {

		Dossier dossier01 = new Dossier(null, null, null, CollectionUtils.asSet(EMAIL, JOURNAL, ENREGISTRER), null, null, null, null, false);
		Dossier dossier02 = new Dossier(null, null, null, CollectionUtils.asSet(VISA, EMAIL, JOURNAL, ENREGISTRER), null, null, null, null, false);
		Dossier dossier03 = new Dossier(null, null, null, CollectionUtils.asSet(REJET, EMAIL, ENREGISTRER), null, null, null, null, false);
		Dossier dossier04 = new Dossier(null, null, null, null, null, null, null, null, false);

		// Checks

		Assert.assertFalse(Dossier.haveActions(dossier01));
		Assert.assertTrue(Dossier.haveActions(dossier02));
		Assert.assertTrue(Dossier.haveActions(dossier03));
		Assert.assertFalse(Dossier.haveActions(dossier04));
	}

	@Test public void getPositiveAction() throws Exception {

		Dossier emptyDossier = new Dossier(null, null, null, null, null, null, null, null, false);

		Dossier dossier = new Dossier(null,
									  null,
									  null,
									  CollectionUtils.asSet(VISA, MAILSEC, TDT, TDT_HELIOS, SIGNATURE, REJET, TDT_ACTES, ARCHIVAGE),
									  null,
									  null,
									  null,
									  null,
									  false
		);

		Dossier dossierWithActionDemandee = new Dossier(null,
														null,
														TDT,
														CollectionUtils.asSet(VISA, MAILSEC, TDT, TDT_HELIOS, SIGNATURE, REJET, TDT_ACTES, ARCHIVAGE),
														null,
														null,
														null,
														null,
														false
		);

		// Checks

		Assert.assertNull(Dossier.getPositiveAction(emptyDossier));
		Assert.assertEquals(Dossier.getPositiveAction(dossierWithActionDemandee), TDT);
		Assert.assertEquals(Dossier.getPositiveAction(dossier), SIGNATURE);

		dossier.getActions().remove(SIGNATURE);
		Assert.assertEquals(Dossier.getPositiveAction(dossier), VISA);

		dossier.getActions().remove(VISA);
		Assert.assertEquals(Dossier.getPositiveAction(dossier), ARCHIVAGE);

		dossier.getActions().remove(ARCHIVAGE);
		Assert.assertEquals(Dossier.getPositiveAction(dossier), MAILSEC);

		dossier.getActions().remove(MAILSEC);
		Assert.assertEquals(Dossier.getPositiveAction(dossier), TDT_ACTES);

		dossier.getActions().remove(TDT_ACTES);
		Assert.assertEquals(Dossier.getPositiveAction(dossier), TDT_HELIOS);

		dossier.getActions().remove(TDT_HELIOS);
		Assert.assertEquals(Dossier.getPositiveAction(dossier), TDT);

		dossier.getActions().remove(TDT);
		Assert.assertNull(Dossier.getPositiveAction(dossier));
	}

	@Test public void getNegativeAction() throws Exception {

		Dossier emptyDossier = new Dossier(null, null, null, null, null, null, null, null, false);
		Dossier dossier = new Dossier(null, null, SIGNATURE, CollectionUtils.asSet(VISA, SIGNATURE, REJET, TDT), null, null, null, null, false);

		// Checks

		Assert.assertNull(Dossier.getNegativeAction(emptyDossier));
		Assert.assertEquals(Dossier.getNegativeAction(dossier), REJET);

		dossier.getActions().remove(REJET);
		Assert.assertNull(Dossier.getNegativeAction(dossier));
	}

	@Test public void fixActions() throws Exception {

		Dossier dossier01 = new Dossier(null, null, null, null, null, null, null, null, false);

		Set<Action> dossier02ActionsSet = CollectionUtils.asSet(VISA, SIGNATURE);
		Dossier dossier02 = new Dossier(null, null, SIGNATURE, dossier02ActionsSet, null, null, null, null, false);

		Set<Action> dossier03ActionsSet = CollectionUtils.asSet();
		Dossier dossier03 = new Dossier(null, null, ARCHIVAGE, dossier03ActionsSet, null, null, null, null, false);

		Dossier.fixActions(dossier01);
		Dossier.fixActions(dossier02);
		Dossier.fixActions(dossier03);

		// Checks

		Assert.assertTrue(dossier01.getActions().contains(VISA));
		Assert.assertTrue(dossier01.getActions().contains(SIGNATURE));
		Assert.assertEquals(dossier01.getActionDemandee(), VISA);
		Assert.assertEquals(dossier01.getActions().size(), 2);

		Assert.assertTrue(dossier02.getActions().contains(SIGNATURE));
		Assert.assertEquals(dossier02.getActionDemandee(), SIGNATURE);
		Assert.assertEquals(dossier02.getActions().size(), 1);

		Assert.assertTrue(dossier03.getActions().contains(ARCHIVAGE));
		Assert.assertEquals(dossier03.getActions().size(), 1);
	}

	@Test public void getMainDocumentsAndAnnexes() throws Exception {

		Dossier emptyDossier = new Dossier(null, null, null, null, null, null, null, null, false);
		emptyDossier.setDocumentList(new ArrayList<Document>());

		ArrayList<Document> documentList = new ArrayList<>();
		documentList.add(new Document("id_01", null, 0, true, false));
		documentList.add(new Document("id_02", null, 0, false, false));
		documentList.add(new Document("id_03", null, 0, false, false));
		documentList.add(new Document("id_04", null, 0, true, false));

		Dossier dossier = new Dossier(null, null, null, null, null, null, null, null, false);
		dossier.setDocumentList(documentList);

		List<Document> mainDocumentList = Dossier.getMainDocuments(dossier);
		List<Document> annexesList = Dossier.getAnnexes(dossier);

		// Checks

		Assert.assertEquals(Dossier.getMainDocuments(emptyDossier), new ArrayList<Document>());
		Assert.assertEquals(Dossier.getAnnexes(emptyDossier), new ArrayList<Document>());

		Assert.assertEquals(mainDocumentList.size(), 2);
		Assert.assertEquals(mainDocumentList.get(0).getId(), "id_01");
		Assert.assertEquals(mainDocumentList.get(1).getId(), "id_04");

		Assert.assertEquals(annexesList.size(), 2);
		Assert.assertEquals(annexesList.get(0).getId(), "id_02");
		Assert.assertEquals(annexesList.get(1).getId(), "id_03");
	}

	// </editor-fold desc="Static utils">

	@SuppressWarnings({"EqualsBetweenInconvertibleTypes", "ObjectEqualsNull"}) @Test public void dossierEquals() {

		Dossier dossier01 = new Dossier("id_01", null, null, null, null, null, null, null, false);
		Dossier dossier01bis = new Dossier("id_01", null, null, null, null, null, null, null, false);
		Dossier dossier02 = new Dossier(null, "id_02", null, null, null, null, null, null, false);

		// Checks

		Assert.assertTrue(dossier01.equals(dossier01bis));
		Assert.assertTrue(dossier01.equals("id_01"));

		Assert.assertFalse(dossier01.equals(dossier02));
		Assert.assertFalse(dossier01.equals(null));
		Assert.assertFalse(dossier01.equals("id_02"));
		Assert.assertFalse(dossier01.equals(1));
	}

	@Test public void dossierHashCode() {

		Dossier dossier01 = new Dossier("id_01", null, null, null, null, null, null, null, false);
		Dossier dossier02 = new Dossier(null, null, null, null, null, null, null, null, false);

		// Checks

		Assert.assertEquals(dossier01.hashCode(), "id_01".hashCode());
		Assert.assertEquals(dossier02.hashCode(), -1);
	}

}