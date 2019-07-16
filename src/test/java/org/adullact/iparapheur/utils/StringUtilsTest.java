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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;


@RunWith(RobolectricTestRunner.class)
public class StringUtilsTest {


    @Test public void areNotEmpty() {

        Assert.assertTrue(StringsUtils.areNotEmpty("Test"));

        Assert.assertFalse(StringsUtils.areNotEmpty(null, "Test"));
        Assert.assertFalse(StringsUtils.areNotEmpty("", "Test"));
        Assert.assertFalse(StringsUtils.areNotEmpty(""));
        Assert.assertFalse(StringsUtils.areNotEmpty(null, ""));
        Assert.assertFalse(StringsUtils.areNotEmpty());
    }


    @Test public void endsWithIgnoreCase() {

        Assert.assertTrue(StringsUtils.endsWithIgnoreCase("test 123", "test 123"));
        Assert.assertTrue(StringsUtils.endsWithIgnoreCase("test 123", "T 123"));
        Assert.assertTrue(StringsUtils.endsWithIgnoreCase("test 123", "123"));
        Assert.assertTrue(StringsUtils.endsWithIgnoreCase("test 123", ""));

        Assert.assertFalse(StringsUtils.endsWithIgnoreCase("test 123", "test 1234"));
        Assert.assertFalse(StringsUtils.endsWithIgnoreCase("test 123", "2"));
        Assert.assertFalse(StringsUtils.endsWithIgnoreCase("test 123", null));
        Assert.assertFalse(StringsUtils.endsWithIgnoreCase(null, null));
    }


    @Test public void parseIso8601Date() {

        Date parsedDate = StringsUtils.parseIso8601Date("2016-12-25T23:45:00");
        Assert.assertNotNull(parsedDate);

        Assert.assertNull(StringsUtils.parseIso8601Date("999999"));
        Assert.assertNull(StringsUtils.parseIso8601Date(""));
        Assert.assertNull(StringsUtils.parseIso8601Date(null));
    }


    @Test public void serializeToIso8601Date() {
        Assert.assertNotNull(StringsUtils.serializeToIso8601Date(new Date(1482705900000L)));
    }


    @Test public void fixIssuerDnX500NameStringOrder() {

        String input = "OU=ADULLACT-Projet,E=systeme@adullact.org,CN=AC ADULLACT Projet\\, g2,O=ADULLACT-Projet,ST=Herault,C=FR";
        String value = StringsUtils.fixIssuerDnX500NameStringOrder(input);
        String expected = "EMAIL=systeme@adullact.org,CN=AC ADULLACT Projet\\, g2,OU=ADULLACT-Projet,O=ADULLACT-Projet,ST=Herault,C=FR";

        Assert.assertEquals(expected, value);
    }


    @Test public void fixUrl() {
        Assert.assertEquals("parapheur", StringsUtils.fixUrl("https://m.parapheur/iparapheur.plop//"));
        Assert.assertEquals("parapheur.test.adullact.org", StringsUtils.fixUrl("http://parapheur.test.adullact.org/parapheur/test"));
        Assert.assertEquals("parapheur.test.adullact.org", StringsUtils.fixUrl("m.parapheur.test.adullact.org/"));
        Assert.assertEquals("parapheur.test.adullact.org", StringsUtils.fixUrl("parapheur.test.adullact.org"));
        Assert.assertEquals("parapheur", StringsUtils.fixUrl("https://parapheur"));
        Assert.assertEquals("parapheur", StringsUtils.fixUrl("https://m.parapheur"));
        Assert.assertEquals("parapheur", StringsUtils.fixUrl("https://m-parapheur"));
        Assert.assertEquals("parapheur", StringsUtils.fixUrl("m.parapheur"));
        Assert.assertEquals("parapheur", StringsUtils.fixUrl("m-parapheur"));
        Assert.assertEquals("parapheur", StringsUtils.fixUrl("parapheur"));
    }


    @Test public void getLocalizedSmallDate() {
        Assert.assertNotNull(StringsUtils.getLocalizedSmallDate(new Date(1482705900000L)));
        Assert.assertEquals("???", StringsUtils.getLocalizedSmallDate(null));
    }


    @Test public void getVerySmallDate() {
        Assert.assertEquals("25/12", StringsUtils.getVerySmallDate(new Date(1482705900000L)));
        Assert.assertEquals("???", StringsUtils.getVerySmallDate(null));
    }


    @Test public void getSmallTime() {
        Assert.assertNotNull(StringsUtils.getSmallTime(new Date(1482705900000L)));
        Assert.assertEquals("???", StringsUtils.getSmallTime(null));
    }


    @Test public void isUrlValid() {
        Assert.assertTrue(StringsUtils.isUrlValid("parapheur"));
        Assert.assertTrue(StringsUtils.isUrlValid("parapheur.test.adullact.org"));
        Assert.assertFalse(StringsUtils.isUrlValid(":::::"));
        Assert.assertFalse(StringsUtils.isUrlValid(""));
        Assert.assertFalse(StringsUtils.isUrlValid(null));
    }


    @Test public void nullableBooleanValueOf() {

    }

}