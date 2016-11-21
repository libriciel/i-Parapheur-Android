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

import com.google.gson.Gson;

import junit.framework.Assert;

import org.adullact.iparapheur.utils.CollectionUtils;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Set;


public class DossierTest {

	private static Gson sGson = CollectionUtils.buildGsonWithLongToDate();

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

		Dossier dossier02 = new Dossier("id_02",
										"Title 02",
										Action.VISA,
										CollectionUtils.asSet(Action.VISA, Action.SIGNATURE),
										"Type 02",
										"Subtype 02",
										null,
										null,
										false
		);

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
}