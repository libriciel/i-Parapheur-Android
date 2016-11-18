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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;


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