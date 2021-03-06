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

import java.util.Arrays;
import java.util.List;


public class ParapheurTypeTest {

	private static Gson sGson = CollectionUtils.buildGsonWithDateParser();

	@Test public void fromJsonArray() throws Exception {

		// Parsed data

		String incorrectArrayJsonString = "[[{]   \"id\": \"Value 01\" , \"sousTypes\": [\"Value 01-01\"  ]]";
		String correctArrayJsonString = "[{" +
				"    \"id\": \"Value 01 \\\"\\\\/%@&éè\"," +
				"    \"sousTypes\": [\"Value 01-01\", \"Value 01-02\"]" +
				"}, {" +
				"    \"id\": \"Value 02 \\\"\\\\/%@&éè\"," +
				"    \"sousTypes\": [\"Value 02-01\", \"Value 02-02\", \"Value 02-03\"]" +
				"}]";

		List<ParapheurType> incorrectArrayParsed = ParapheurType.fromJsonArray(incorrectArrayJsonString, sGson);
		List<ParapheurType> correctArrayParsed = ParapheurType.fromJsonArray(correctArrayJsonString, sGson);

		// Valid types

		ParapheurType type01 = new ParapheurType("Value 01 \"\\/%@&éè", Arrays.asList("Value 01-01", "Value 01-02"));
		ParapheurType type02 = new ParapheurType("Value 02 \"\\/%@&éè", Arrays.asList("Value 02-01", "Value 02-02", "Value 02-03"));

		// Checks

		Assert.assertNull(incorrectArrayParsed);
		Assert.assertNotNull(correctArrayParsed);

		Assert.assertEquals(correctArrayParsed.get(0).toString(), type01.toString());
		Assert.assertEquals(correctArrayParsed.get(0).getName(), type01.getName());
		Assert.assertEquals(correctArrayParsed.get(0).getSubTypes().toString(), type01.getSubTypes().toString());

		Assert.assertEquals(correctArrayParsed.get(1).toString(), type02.toString());
		Assert.assertEquals(correctArrayParsed.get(1).getName(), type02.getName());
		Assert.assertEquals(correctArrayParsed.get(1).getSubTypes().toString(), type02.getSubTypes().toString());

	}

}