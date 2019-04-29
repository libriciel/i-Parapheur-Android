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

import org.adullact.iparapheur.model.Bureau;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;


@RunWith(RobolectricTestRunner.class)
public class BureauUtilsTest {


    @Test public void findInList() {

        List<Bureau> bureauList = new ArrayList<>();
        bureauList.add(new Bureau("id_01", "Name 01", 10, 5));
        bureauList.add(new Bureau("id_02", "Name 02", 10, 5));
        bureauList.add(null);
        bureauList.add(new Bureau("id_03", "Name 03", 10, 5));

        List<Bureau> emptyList = new ArrayList<>();

        // Checks

        org.junit.Assert.assertNull(BureauUtils.findInList(null, null));
        org.junit.Assert.assertNull(BureauUtils.findInList(bureauList, null));
        org.junit.Assert.assertNull(BureauUtils.findInList(bureauList, "id_missing"));
        org.junit.Assert.assertNull(BureauUtils.findInList(emptyList, "id_01"));
        //noinspection ConstantConditions
        org.junit.Assert.assertEquals(BureauUtils.findInList(bureauList, "id_02").getTitle(), "Name 02");
    }

}