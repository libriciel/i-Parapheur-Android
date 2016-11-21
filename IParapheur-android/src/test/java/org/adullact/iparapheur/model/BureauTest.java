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

import java.util.List;

import static org.mockito.Matchers.any;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class BureauTest {

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

		PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Object>() {
			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
				CharSequence a = (CharSequence) invocation.getArguments()[0];
				return org.adullact.iparapheur.mock.TextUtils.isEmpty(a);
			}
		});
	}

	@Test public void fromJsonArray() throws Exception {

		// Parsed data

		String incorrectArrayJsonString = "[[{]   \"id\": \"Value 01\" , \"collectivite\": [\"Value 01-01\"  ]]";
		String correctArrayJsonString = "[" +
				"{" +
				"    \"hasSecretaire\": false," +
				"    \"collectivite\": \"Collectivité 01 \\\"\\\\/%@&éè\"," +
				"    \"description\": null," +
				"    \"en-preparation\": 0," +
				"    \"nodeRef\": \"workspace:\\/\\/SpacesStore\\/44abe93c-16d7-4e00-b561-f6d1b8b6c1d3\"," +
				"    \"shortName\": \"C1\"," +
				"    \"en-retard\": 5," +
				"    \"image\": \"\"," +
				"    \"show_a_venir\": null," +
				"    \"habilitation\": {" +
				"        \"traiter\": null," +
				"        \"secretariat\": null," +
				"        \"archivage\": null," +
				"        \"transmettre\": null" +
				"    }," +
				"    \"a-archiver\": 27," +
				"    \"a-traiter\": 10," +
				"    \"id\": \"id_01\"," +
				"    \"isSecretaire\": false," +
				"    \"name\": \"Name 01 \\\"\\/%@&éè\"," +
				"    \"retournes\": 13," +
				"    \"dossiers-delegues\": 59" +
				"}, {" +
				"    \"hasSecretaire\": true," +
				"    \"collectivite\": \"Collectivité 02 \\\"\\\\/%@&éè\"," +
				"    \"description\": \"Description 02 \\\"\\\\/%@&éè\"," +
				"    \"en-preparation\": 1," +
				"    \"nodeRef\": \"id_02\"," +
				"    \"shortName\": \"C2\"," +
				"    \"image\": null," +
				"    \"show_a_venir\": null," +
				"    \"habilitation\": {" +
				"        \"traiter\": null," +
				"        \"secretariat\": null," +
				"        \"archivage\": null," +
				"        \"transmettre\": null" +
				"    }," +
				"    \"a-archiver\": 33," +
				"    \"isSecretaire\": false," +
				"    \"name\": \"Name 02 \\\"\\/%@&éè\"," +
				"    \"retournes\": 10," +
				"    \"dossiers-delegues\": 0" +
				"}]";

		List<Bureau> incorrectArrayParsed = Bureau.fromJsonArray(incorrectArrayJsonString, sGson);
		List<Bureau> correctArrayParsed = Bureau.fromJsonArray(correctArrayJsonString, sGson);

		// Valid types

		Bureau bureau01 = new Bureau("id_01", "Name 01 \"/%@&éè", 10, 5);
		Bureau bureau02 = new Bureau("workspace://SpacesStore/id_02", "Name 02 \"/%@&éè", 0, 0);

		// Checks

		Assert.assertNull(incorrectArrayParsed);
		Assert.assertNotNull(correctArrayParsed);

		Assert.assertEquals(correctArrayParsed.get(0).toString(), bureau01.toString());
		Assert.assertEquals(correctArrayParsed.get(0).getId(), bureau01.getId());
		Assert.assertEquals(correctArrayParsed.get(0).getTitle(), bureau01.getTitle());
		Assert.assertEquals(correctArrayParsed.get(0).getLateCount(), bureau01.getLateCount());
		Assert.assertEquals(correctArrayParsed.get(0).getTodoCount(), bureau01.getTodoCount());

		Assert.assertEquals(correctArrayParsed.get(1).toString(), bureau02.toString());
		Assert.assertEquals(correctArrayParsed.get(1).getId(), bureau02.getId());
		Assert.assertEquals(correctArrayParsed.get(1).getTitle(), bureau02.getTitle());
		Assert.assertEquals(correctArrayParsed.get(1).getLateCount(), bureau02.getLateCount());
		Assert.assertEquals(correctArrayParsed.get(1).getTodoCount(), bureau02.getTodoCount());

		Assert.assertTrue(bureau01.equals(correctArrayParsed.get(0)));
		Assert.assertTrue(bureau02.equals(correctArrayParsed.get(1)));
	}

}