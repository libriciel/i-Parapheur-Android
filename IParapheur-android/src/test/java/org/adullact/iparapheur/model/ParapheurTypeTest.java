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
import junit.framework.TestCase;

import org.adullact.iparapheur.utils.CollectionUtils;

import java.util.Arrays;
import java.util.List;


public class ParapheurTypeTest extends TestCase {

	private static Gson sGson = CollectionUtils.buildGsonWithLongToDate();

	public void testFromJsonArray() throws Exception {

		// Parsed data

		String incorrectArrayJsonString = "[[{]   \"id\": \"Value 01\" , \"sousTypes\": [\"Value 01-01\"  ]]";
		String correctArrayJsonString = "[{\"id\": \"Value 01\", \"sousTypes\": [\"Value 01-01\", \"Value 01-02\"]}," //
				+ "{\"id\": \"Value 02\",\"sousTypes\": [\"Value 02-01\", \"Value 02-02\", \"Value 02-03\"]}]";

		List<ParapheurType> incorrectArrayParsed = ParapheurType.fromJsonArray(incorrectArrayJsonString, sGson);
		List<ParapheurType> correctArrayParsed = ParapheurType.fromJsonArray(correctArrayJsonString, sGson);

		// Valid types

		ParapheurType type01 = new ParapheurType();
		type01.setName("Value 01");
		type01.setSubTypes(Arrays.asList("Value 01-01", "Value 01-02"));

		ParapheurType type02 = new ParapheurType();
		type02.setName("Value 02");
		type02.setSubTypes(Arrays.asList("Value 02-01", "Value 02-02", "Value 02-03"));

		// Checks

		Assert.assertNotNull(correctArrayParsed);
		Assert.assertNull(incorrectArrayParsed);
		Assert.assertEquals(correctArrayParsed.get(0).toString(), type01.toString());
		Assert.assertEquals(correctArrayParsed.get(1).toString(), type02.toString());
	}

}