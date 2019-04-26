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

import org.adullact.iparapheur.utils.CollectionUtils;
import org.adullact.iparapheur.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EtapeCircuitTest {

    private static Gson sGson = CollectionUtils.buildGsonWithDateParser();


    @Test public void fromJsonArray() {

        // Parsed data

        String incorrectArrayJsonString = "[[{]   \"id\": \"Value 01\" , \"collectivite\": [\"Value 01-01\"  ]]";
        String correctArrayObjectString = "[{" +
                "        \"approved\": true," +
                "        \"signataire\": \"Signataire 01\"," +
                "        \"rejected\": false," +
                "        \"dateValidation\": 1478792085000," +
                "        \"annotPub\": \"Annotation publique 01 \\\"\\/%@&éè\"," +
                "        \"parapheurName\": \"Bureau 01\"," +
                "        \"delegueName\": \"Delegue 01\"," +
                "        \"signatureInfo\": {}," +
                "        \"delegateur\": \"Delegateur 01\"," +
                "        \"actionDemandee\": \"SIGNATURE\"," +
                "        \"id\": \"id_01\"," +
                "        \"isCurrent\": true," +
                "        \"signatureEtape\": null" +
                "    }, {" +
                "        \"approved\": false," +
                "        \"rejected\": true," +
                "        \"dateValidation\": null," +
                "        \"annotPub\": null," +
                "        \"id\": \"id_02\"," +
                "        \"isCurrent\": false," +
                "        \"signatureEtape\": null" +
                "    }]";

        List<EtapeCircuit> incorrectObjectParsed = EtapeCircuit.fromJsonArray(incorrectArrayJsonString, sGson);
        List<EtapeCircuit> correctObjectParsed = EtapeCircuit.fromJsonArray(correctArrayObjectString, sGson);

        // Valid types

        List<EtapeCircuit> etapesList01 = new ArrayList<>();
        etapesList01.add(new EtapeCircuit(
                StringUtils.serializeToIso8601Date(new Date(1478792085000L)),
                true,
                false,
                "Bureau 01",
                "Signataire 01",
                Action.SIGNATURE.toString(),
                "Annotation publique 01 \"/%@&éè"
        ));
        etapesList01.add(new EtapeCircuit(null, false, true, null, null, Action.VISA.toString(), null));

        // Checks

        Assert.assertNull(incorrectObjectParsed);
        Assert.assertNotNull(correctObjectParsed);

        Assert.assertEquals(correctObjectParsed.get(0).toString(), etapesList01.get(0).toString());
        Assert.assertEquals(correctObjectParsed.get(0).getAction(), etapesList01.get(0).getAction());
        Assert.assertEquals(correctObjectParsed.get(0).getBureauName(), etapesList01.get(0).getBureauName());
        Assert.assertEquals(correctObjectParsed.get(0).getSignataire(), etapesList01.get(0).getSignataire());
        Assert.assertEquals(correctObjectParsed.get(0).isApproved(), etapesList01.get(0).isApproved());
        Assert.assertEquals(correctObjectParsed.get(0).isRejected(), etapesList01.get(0).isRejected());
        Assert.assertEquals(correctObjectParsed.get(0).getDateValidation().getTime(), etapesList01.get(0).getDateValidation().getTime());

        Assert.assertEquals(correctObjectParsed.get(1).toString(), etapesList01.get(1).toString());
        Assert.assertEquals(correctObjectParsed.get(1).getAction(), etapesList01.get(1).getAction());
        Assert.assertEquals(correctObjectParsed.get(1).getBureauName(), etapesList01.get(1).getBureauName());
        Assert.assertEquals(correctObjectParsed.get(1).getSignataire(), etapesList01.get(1).getSignataire());
        Assert.assertEquals(correctObjectParsed.get(1).isApproved(), etapesList01.get(1).isApproved());
        Assert.assertEquals(correctObjectParsed.get(1).isRejected(), etapesList01.get(1).isRejected());
        Assert.assertNull(correctObjectParsed.get(1).getDateValidation());
    }

}