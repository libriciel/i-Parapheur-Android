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
																Action.EMAIL,
																Action.JOURNAL,
																Action.SECRETARIAT,
																Action.REJET,
																Action.SIGNATURE,
																Action.TRANSFERT_SIGNATURE,
																Action.AVIS_COMPLEMENTAIRE
		);

		Dossier dossier01 = new Dossier("id_01",
										"Title 01",
										Action.SIGNATURE,
										dossier01ActionsSet,
										"Type 01",
										"Subtype 01",
										new Date(1392829477205L),
										null,
										true
		);

		Dossier dossier02 = new Dossier("id_02", "Title 02", VISA, CollectionUtils.asSet(VISA, Action.SIGNATURE), "Type 02", "Subtype 02", null, null, false);

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
	}

//	@Test public void isDetailsAvailable() throws Exception {
//
//	}
//
//	@Test public void hasActions() throws Exception {
//
//	}

	// <editor-fold desc="Static utils">

//	@Test public void findCurrentDocument() throws Exception {
//
//	}
//
//	@Test public void getPositiveAction() throws Exception {
//
//	}
//
//	@Test public void getNegativeAction() throws Exception {
//
//	}

	@Test public void fixActions() throws Exception {

		Dossier dossier01 = new Dossier(null, null, null, null, null, null, null, null, false);

		Set<Action> dossier02ActionsSet = CollectionUtils.asSet(Action.VISA, Action.SIGNATURE);
		Dossier dossier02 = new Dossier(null, null, Action.SIGNATURE, dossier02ActionsSet, null, null, null, null, false);

		Set<Action> dossier03ActionsSet = CollectionUtils.asSet();
		Dossier dossier03 = new Dossier(null, null, Action.ARCHIVAGE, dossier03ActionsSet, null, null, null, null, false);

		Dossier.fixActions(dossier01);
		Dossier.fixActions(dossier02);
		Dossier.fixActions(dossier03);

		// Checks

		Assert.assertTrue(dossier01.getActions().contains(Action.VISA));
		Assert.assertTrue(dossier01.getActions().contains(Action.SIGNATURE));
		Assert.assertEquals(dossier01.getActionDemandee(), Action.VISA);
		Assert.assertEquals(dossier01.getActions().size(), 2);

		Assert.assertTrue(dossier02.getActions().contains(Action.SIGNATURE));
		Assert.assertEquals(dossier02.getActionDemandee(), Action.SIGNATURE);
		Assert.assertEquals(dossier02.getActions().size(), 1);

		Assert.assertTrue(dossier03.getActions().contains(Action.ARCHIVAGE));
		Assert.assertEquals(dossier03.getActions().size(), 1);
	}

	@Test public void getMainDocumentsAndAnnexes() throws Exception {

		Dossier emptyDossier = new Dossier(null, null, null, null, null, null, null, null, false);
		emptyDossier.setDocumentList(new ArrayList<Document>());

		ArrayList<Document> documentList = new ArrayList<>();
		documentList.add(new Document("id_01", null, 0, false, true));
		documentList.add(new Document("id_02", null, 0, false, false));
		documentList.add(new Document("id_03", null, 0, false, false));
		documentList.add(new Document("id_04", null, 0, false, true));

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
}