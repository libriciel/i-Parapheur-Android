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
package org.adullact.iparapheur.utils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RunWith(RobolectricTestRunner.class)
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


    @Test public void printListReflexionCall() {

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


    @Test public void buildGsonWithLongToDate() {

        Gson gson = CollectionUtils.buildGsonWithDateParser();
        Date testDate = new Date(1396017643828L);

        // Serialize and deserialize

        DateWrapper original = new DateWrapper(testDate);
        String serialized = gson.toJson(original);
        DateWrapper deserialized = gson.fromJson(serialized, DateWrapper.class);

        String nullDateString = "{\"date\":null}";
        DateWrapper nullDeserialized = gson.fromJson(nullDateString, DateWrapper.class);

        String voidDateString = "{}";
        DateWrapper voidDeserialized = gson.fromJson(voidDateString, DateWrapper.class);

        String iso8601DateString = "{\"date\":\"2016-12-25T23:45:59\"}";
        DateWrapper iso8601Deserialized = gson.fromJson(iso8601DateString, DateWrapper.class);

        // Checks

        Assert.assertEquals(original.getDate().getTime(), deserialized.getDate().getTime());
        Assert.assertNotNull(iso8601Deserialized.getDate());
        Assert.assertEquals(iso8601Deserialized.getDate().getTime(), 1482705959000L);

        Assert.assertNull(nullDeserialized.getDate());
        Assert.assertNull(voidDeserialized.getDate());
    }

}