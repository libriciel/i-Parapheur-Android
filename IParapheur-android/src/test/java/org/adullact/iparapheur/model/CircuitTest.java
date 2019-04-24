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

import org.junit.Assert;

import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CircuitTest {

	private static Gson sGson = CollectionUtils.buildGsonWithDateParser();

	@Test public void fromJsonObject() throws Exception {

		// Parsed data

		String incorrectArrayJsonString = "[[{]   \"id\": \"Value 01\" , \"collectivite\": [\"Value 01-01\"  ]]";
		String correctArrayObjectString = "{" +
				"    \"etapes\": [{" +
				"        \"approved\": true," +
				"        \"signataire\": \"Signataire 01\"," +
				"        \"rejected\": false," +
				"        \"dateValidation\": 1478792085680," +
				"        \"annotPub\": \"Annotation publique 01 \\\"\\/%@&éè\"," +
				"        \"parapheurName\": \"Bureau 01\"," +
				"        \"delegueName\": \"Delegue 01\"," +
				"        \"signatureInfo\": {}," +
				"        \"delegateur\": \"Delegateur 01\"," +
				"        \"actionDemandee\": \"SIGNATURE\"," +
				"        \"id\": \"id_01\"," +
				"        \"isCurrent\": true," +
				"        \"signatureEtape\": null" +
				"    }, {" +
				"        \"approved\": false," +
				"        \"rejected\": true," +
				"        \"dateValidation\": null," +
				"        \"annotPub\": null," +
				"        \"id\": \"id_02\"," +
				"        \"isCurrent\": false," +
				"        \"signatureEtape\": null" +
				"    }]," +
				"    \"annotPriv\": \"Annotation privée \\\"\\/%@&éè\"," +
				"    \"isDigitalSignatureMandatory\": true," +
				"    \"hasSelectionScript\": false," +
				"    \"sigFormat\": \"PKCS#7\\\\/single\"" +
				"}";

		Circuit incorrectObjectParsed = Circuit.fromJsonObject(incorrectArrayJsonString, sGson);
		Circuit correctObjectParsed = Circuit.fromJsonObject(correctArrayObjectString, sGson);

		// Valid types

		List<EtapeCircuit> etapes01 = new ArrayList<>();
		etapes01.add(new EtapeCircuit(
				StringUtils.serializeToIso8601Date(new Date(1478792085680L)),
				true,
				false,
				"Bureau 01",
				"Signataire 01",
				Action.SIGNATURE.toString(),
				"Annotation publique 01 \"/%@&éè"
		));
		etapes01.add(new EtapeCircuit(null, false, true, null, null, Action.VISA.toString(), null));

		Circuit circuit01 = new Circuit(etapes01, "PKCS#7\\/single", true);

		// Checks

		Assert.assertNull(incorrectObjectParsed);
		Assert.assertNotNull(correctObjectParsed);

		Assert.assertEquals(correctObjectParsed.toString(), circuit01.toString());
		Assert.assertEquals(correctObjectParsed.getEtapeCircuitList().toString(), circuit01.getEtapeCircuitList().toString());
		Assert.assertEquals(correctObjectParsed.getSigFormat(), circuit01.getSigFormat());
		Assert.assertEquals(correctObjectParsed.isDigitalSignatureMandatory(), circuit01.isDigitalSignatureMandatory());
	}

}