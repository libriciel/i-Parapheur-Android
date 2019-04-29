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
import org.robolectric.annotation.Config;

import java.util.Date;


@Config(qualifiers = "fr")
@RunWith(RobolectricTestRunner.class)
public class StringUtilsTest {


    @Test public void areNotEmpty() {

        Assert.assertTrue(StringUtils.areNotEmpty("Test"));

        Assert.assertFalse(StringUtils.areNotEmpty(null, "Test"));
        Assert.assertFalse(StringUtils.areNotEmpty("", "Test"));
        Assert.assertFalse(StringUtils.areNotEmpty(""));
        Assert.assertFalse(StringUtils.areNotEmpty(null, ""));
        Assert.assertFalse(StringUtils.areNotEmpty());
    }


    @Test public void endsWithIgnoreCase() {

        Assert.assertTrue(StringUtils.endsWithIgnoreCase("test 123", "test 123"));
        Assert.assertTrue(StringUtils.endsWithIgnoreCase("test 123", "T 123"));
        Assert.assertTrue(StringUtils.endsWithIgnoreCase("test 123", "123"));
        Assert.assertTrue(StringUtils.endsWithIgnoreCase("test 123", ""));

        Assert.assertFalse(StringUtils.endsWithIgnoreCase("test 123", "test 1234"));
        Assert.assertFalse(StringUtils.endsWithIgnoreCase("test 123", "2"));
        Assert.assertFalse(StringUtils.endsWithIgnoreCase("test 123", null));
        Assert.assertFalse(StringUtils.endsWithIgnoreCase(null, null));
    }


    @Test public void parseIso8601Date() {

        Date parsedDate = StringUtils.parseIso8601Date("2016-12-25T23:45:00");
        Assert.assertNotNull(parsedDate);

        Assert.assertNull(StringUtils.parseIso8601Date("999999"));
        Assert.assertNull(StringUtils.parseIso8601Date(""));
        Assert.assertNull(StringUtils.parseIso8601Date(null));
    }


    @Test public void serializeToIso8601Date() {
        Assert.assertNotNull(StringUtils.serializeToIso8601Date(new Date(1482705900000L)));
    }


    @Test public void fixIssuerDnX500NameStringOrder() {

        String input = "OU=ADULLACT-Projet,E=systeme@adullact.org,CN=AC ADULLACT Projet\\, g2,O=ADULLACT-Projet,ST=Herault,C=FR";
        String value = StringUtils.fixIssuerDnX500NameStringOrder(input);
        String expected = "EMAIL=systeme@adullact.org,CN=AC ADULLACT Projet\\, g2,OU=ADULLACT-Projet,O=ADULLACT-Projet,ST=Herault,C=FR";

        Assert.assertEquals(value, expected);
    }


    @Test public void fixUrl() {
        Assert.assertEquals(StringUtils.fixUrl("https://m.parapheur/iparapheur.plop//"), "parapheur");
        Assert.assertEquals(StringUtils.fixUrl("http://parapheur.test.adullact.org/parapheur/test"), "parapheur.test.adullact.org");
        Assert.assertEquals(StringUtils.fixUrl("m.parapheur.test.adullact.org/"), "parapheur.test.adullact.org");
        Assert.assertEquals(StringUtils.fixUrl("parapheur.test.adullact.org"), "parapheur.test.adullact.org");
        Assert.assertEquals(StringUtils.fixUrl("https://parapheur"), "parapheur");
        Assert.assertEquals(StringUtils.fixUrl("https://m.parapheur"), "parapheur");
        Assert.assertEquals(StringUtils.fixUrl("https://m-parapheur"), "parapheur");
        Assert.assertEquals(StringUtils.fixUrl("m.parapheur"), "parapheur");
        Assert.assertEquals(StringUtils.fixUrl("m-parapheur"), "parapheur");
        Assert.assertEquals(StringUtils.fixUrl("parapheur"), "parapheur");
    }


    @Test public void getLocalizedSmallDate() {
        Assert.assertNotNull(StringUtils.getLocalizedSmallDate(new Date(1482705900000L)));
        Assert.assertEquals(StringUtils.getLocalizedSmallDate(null), "???");
    }


    @Test public void getVerySmallDate() {
        Assert.assertEquals(StringUtils.getVerySmallDate(new Date(1482705900000L)), "25/12");
        Assert.assertEquals(StringUtils.getVerySmallDate(null), "???");
    }


    @Test public void getSmallTime() {
        Assert.assertNotNull(StringUtils.getSmallTime(new Date(1482705900000L)));
        Assert.assertEquals(StringUtils.getSmallTime(null), "???");
    }


    @Test public void isUrlValid() {
        Assert.assertTrue(StringUtils.isUrlValid("parapheur"));
        Assert.assertTrue(StringUtils.isUrlValid("parapheur.test.adullact.org"));
        Assert.assertFalse(StringUtils.isUrlValid(":::::"));
        Assert.assertFalse(StringUtils.isUrlValid(""));
        Assert.assertFalse(StringUtils.isUrlValid(null));
    }


    @Test public void nullableBooleanValueOf() {

    }

}