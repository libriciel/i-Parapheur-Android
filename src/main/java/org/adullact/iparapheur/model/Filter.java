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

import android.os.Parcel;
import android.os.Parcelable;

import org.adullact.iparapheur.utils.StringsUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class Filter implements Parcelable {

    public static final String REQUEST_JSON_FILTER_TYPE_METIER = "ph:typeMetier";
    public static final String REQUEST_JSON_FILTER_SOUS_TYPE_METIER = "ph:soustypeMetier";
    public static final String REQUEST_JSON_FILTER_TITLE = "cm:title";
    public static final String REQUEST_JSON_FILTER_AND = "and";
    public static final String REQUEST_JSON_FILTER_OR = "or";
    public static final String EDIT_FILTER_ID = "edit-filter";

    private static final State DEFAULT_STATE = State.A_TRAITER;

    public static final Parcelable.Creator<Filter> CREATOR = new Parcelable.Creator<Filter>() {
        public Filter createFromParcel(Parcel source) {
            return new Filter(source);
        }

        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };

    private String id = UUID.randomUUID().toString();
    private String name;

    // Filter values
    private String title;
    private List<String> typeList = new ArrayList<>();
    private List<String> subTypeList = new ArrayList<>();
    private State state = DEFAULT_STATE;
    private Date beginDate;
    private Date endDate;


    public Filter(String id) {
        this.id = id;
        name = null;
        state = DEFAULT_STATE;
        typeList = new ArrayList<>();
        subTypeList = new ArrayList<>();
    }


    private Filter(Parcel in) {
        id = in.readString();
        name = in.readString();
        title = in.readString();
        typeList = new ArrayList<>();
        in.readList(typeList, String.class.getClassLoader());
        subTypeList = new ArrayList<>();
        in.readList(subTypeList, String.class.getClassLoader());
        state = State.values()[in.readInt()];
        long tmpDateDebut = in.readLong();
        beginDate = tmpDateDebut == -1 ? null : new Date(tmpDateDebut);
        long tmpDateFin = in.readLong();
        endDate = tmpDateFin == -1 ? null : new Date(tmpDateFin);
    }


    public String getJSONFilter() {

        JSONObject jsonFilter = new JSONObject();
        try {

            // TYPES
            JSONArray jsonTypes = new JSONArray();
            if (typeList != null) {
                for (String type : typeList) {
                    jsonTypes.put(new JSONObject().put(REQUEST_JSON_FILTER_TYPE_METIER, StringsUtils.urlEncode(type)));
                }
            }
            // SOUSTYPES
            JSONArray jsonSousTypes = new JSONArray();
            if (subTypeList != null) {
                for (String sousType : subTypeList) {
                    jsonSousTypes.put(new JSONObject().put(REQUEST_JSON_FILTER_SOUS_TYPE_METIER, StringsUtils.urlEncode(sousType)));
                }
            }
            //TITRE

            JSONArray jsonTitre = new JSONArray();
            if ((title != null) && (!title.trim().isEmpty())) {
                jsonTitre.put(new JSONObject().put(REQUEST_JSON_FILTER_TITLE, "*" + title.trim() + "*"));
            }

            // FILTRE FINAL
            jsonFilter.put(REQUEST_JSON_FILTER_AND, new JSONArray().
                    put(new JSONObject().put(REQUEST_JSON_FILTER_OR, jsonTypes)).
                    put(new JSONObject().put(REQUEST_JSON_FILTER_OR, jsonSousTypes)).
                    put(new JSONObject().put(REQUEST_JSON_FILTER_OR, jsonTitre)));

        } catch (JSONException e) {
            //Log.w(Filter.class.getSimpleName(), "Erreur lors de la conversion du filtre", e);
        }
        return jsonFilter.toString();
    }


    @Override public int describeContents() {
        return 0;
    }


    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(title);
        dest.writeList(typeList);
        dest.writeList(subTypeList);
        dest.writeInt(state.ordinal());
        dest.writeLong(beginDate != null ? beginDate.getTime() : -1);
        dest.writeLong(endDate != null ? endDate.getTime() : -1);
    }


    @Override public boolean equals(Object o) {
        if (o instanceof Filter) {
            Filter toCompare = (Filter) o;
            return id.equals(toCompare.id);
        }
        return false;
    }


    @Override public int hashCode() {
        return id.hashCode();
    }

}
