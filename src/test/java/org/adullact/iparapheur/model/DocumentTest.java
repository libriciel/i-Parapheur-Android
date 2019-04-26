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
import com.google.gson.JsonSyntaxException;

import org.adullact.iparapheur.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


@RunWith(RobolectricTestRunner.class)
public class DocumentTest {

    private static Gson sGson = CollectionUtils.buildGsonWithDateParser();


    @Test public void gsonParse() {

        String incorrectJsonString = "[[{]   \"id\": \"Value 01\" , \"collectivite\": [\"Value 01-01\"  ]]";
        String correctJsonString = "{" +
                "    \"size\": 50000," +
                "    \"visuelPdf\": true," +
                "    \"isMainDocument\": true," +
                "    \"pageCount\": 5," +
                "    \"attestState\": 0," +
                "    \"id\": \"id_01\"," +
                "    \"name\": \"name 01.pdf\"," +
                "    \"canDelete\": true," +
                "    \"isLocked\": true" +
                "}";

        Document incorrectObjectParsed;
        try { incorrectObjectParsed = sGson.fromJson(incorrectJsonString, Document.class); } catch (JsonSyntaxException ex) { incorrectObjectParsed = null; }

        Document correctObjectParsed = sGson.fromJson(correctJsonString, Document.class);
        Document document = new Document("id_01", "name 01.pdf", 50000, true, true);
        document.setPagesAnnotations(null);
        document.setSyncDate(null);
        document.setParent(null);

        // Checks

        Assert.assertNull(incorrectObjectParsed);
        Assert.assertNotNull(correctObjectParsed);

        Assert.assertEquals(document, correctObjectParsed);
        Assert.assertEquals(correctObjectParsed.toString(), document.toString());
        Assert.assertEquals(correctObjectParsed.getId(), document.getId());
        Assert.assertEquals(correctObjectParsed.getName(), document.getName());
        Assert.assertEquals(correctObjectParsed.getSize(), document.getSize());
        Assert.assertEquals(correctObjectParsed.isMainDocument(), document.isMainDocument());
        Assert.assertEquals(correctObjectParsed.isPdfVisual(), document.isPdfVisual());
        Assert.assertEquals(correctObjectParsed.getPagesAnnotations(), document.getPagesAnnotations());
        Assert.assertEquals(correctObjectParsed.getSyncDate(), document.getSyncDate());
        Assert.assertEquals(correctObjectParsed.getParent(), document.getParent());
    }

}