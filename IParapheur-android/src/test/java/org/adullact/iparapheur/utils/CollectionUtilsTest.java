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
package org.adullact.iparapheur.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.junit.Assert;
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

import static org.mockito.Matchers.any;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class CollectionUtilsTest {

	// <editor-fold desc="Utils">

	/**
	 * Simple private class to test serialization
	 */
	private class DateWrapper {

		@SerializedName("date") private Date mDate;

		private DateWrapper(Date date) {
			mDate = date;
		}

		private Date getDate() {
			return mDate;
		}
	}

	// </editor-fold desc="Utils">

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

	@Test public void findBureau() throws Exception {

		List<Bureau> bureauList = new ArrayList<>();
		bureauList.add(new Bureau("id_01", "Name 01", 10, 5));
		bureauList.add(new Bureau("id_02", "Name 02", 10, 5));
		bureauList.add(new Bureau("id_03", "Name 03", 10, 5));

		List<Bureau> emptyList = new ArrayList<>();

		// Checks

		Assert.assertNull(CollectionUtils.findBureau(null, null));
		Assert.assertNull(CollectionUtils.findBureau(bureauList, null));
		Assert.assertNull(CollectionUtils.findBureau(bureauList, "id_missing"));
		Assert.assertNull(CollectionUtils.findBureau(emptyList, "id_01"));
		//noinspection ConstantConditions
		Assert.assertEquals(CollectionUtils.findBureau(bureauList, "id_02").getTitle(), "Name 02");
	}

	@Test public void printListReflexionCall() throws Exception {

		List<Bureau> bureauList = new ArrayList<>();
		bureauList.add(new Bureau("b_01", "Name 01", 10, 5));
		bureauList.add(new Bureau("b_02", "Name 02", 10, 5));
		bureauList.add(null);

		List<Document> documentList = new ArrayList<>();
		documentList.add(new Document("d_01", null, 0, false, false));

		List<String> incompatibleList = new ArrayList<>();
		incompatibleList.add("s_01");

		List<Document> emptyList = new ArrayList<>();

		// Checks

		Assert.assertEquals(CollectionUtils.printListReflexionCall(null, "getId"), "null");
		Assert.assertEquals(CollectionUtils.printListReflexionCall(bureauList, "getId"), "[b_01, b_02, null]");
		Assert.assertEquals(CollectionUtils.printListReflexionCall(documentList, "getId"), "[d_01]");
		Assert.assertEquals(CollectionUtils.printListReflexionCall(incompatibleList, "getId"), "[-class incompatible with getId()-]");
		Assert.assertEquals(CollectionUtils.printListReflexionCall(emptyList, "getId"), "[]");
	}

	@Test public void buildGsonWithLongToDate() throws Exception {

		Gson gson = CollectionUtils.buildGsonWithLongToDate();
		Date testDate = new Date(1396017643828L);

		// Serialize and deserialize

		DateWrapper original = new DateWrapper(testDate);
		String serialized = gson.toJson(original);
		DateWrapper deserialized = gson.fromJson(serialized, DateWrapper.class);

		String nullDateString = "{\"date\":null}";
		DateWrapper nullDeserialized = gson.fromJson(nullDateString, DateWrapper.class);

		// Checks

		Assert.assertEquals(original.getDate().getTime(), deserialized.getDate().getTime());
		Assert.assertNull(nullDeserialized.getDate());
	}

}