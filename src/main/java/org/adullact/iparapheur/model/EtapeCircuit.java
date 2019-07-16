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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.adullact.iparapheur.utils.StringsUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@ToString
@Setter
@Getter
public class EtapeCircuit {

    @SerializedName("dateValidation") private Date dateValidation;
    @SerializedName("approved") private boolean isApproved;
    @SerializedName("rejected") private boolean isRejected;
    @SerializedName("parapheurName") private String bureauName;
    @SerializedName("signataire") private String signataire;
    @SerializedName("actionDemandee") private Action action;
    @SerializedName("annotPub") private String publicAnnotation;


    public EtapeCircuit(String dateValidation, boolean isApproved, boolean isRejected, String bureauName, String signataire, String action,
                        String publicAnnotation) {

        this.dateValidation = StringsUtils.parseIso8601Date(dateValidation);
        this.isApproved = isApproved;
        this.isRejected = isRejected;
        this.bureauName = bureauName;
        this.signataire = signataire;
        this.action = (action != null) ? Action.valueOf(action) : Action.VISA;
        this.publicAnnotation = publicAnnotation;
    }


    /**
     * Static parser, useful for Unit tests
     *
     * @param jsonArrayString data as a Json array, serialized with some {@link org.json.JSONArray#toString}.
     * @param gson            passed statically to prevent re-creating it.
     */
    public static @Nullable List<EtapeCircuit> fromJsonArray(@NonNull String jsonArrayString, @NonNull Gson gson) {

        Type typologyType = new TypeToken<ArrayList<EtapeCircuit>>() {}.getType();

        try {
            List<EtapeCircuit> etapeCircuitList = gson.fromJson(jsonArrayString, typologyType);

            // Fix default value on parse.
            // There is no easy way (@annotation) to do it with Gson,
            // So we're doing it here instead of overriding everything.
            for (EtapeCircuit etapeCircuit : etapeCircuitList)
                if (etapeCircuit.getAction() == null)
                    etapeCircuit.setAction(Action.VISA);

            return etapeCircuitList;
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

}