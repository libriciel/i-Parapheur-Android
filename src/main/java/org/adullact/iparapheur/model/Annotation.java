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

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import org.adullact.iparapheur.utils.JsonExplorer;

import java.io.IOException;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class Annotation implements Serializable {

    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_IS_SECRETAIRE = "secretaire";
    private static final String JSON_KEY_AUTHOR = "author";
    private static final String JSON_KEY_DATE = "date";
    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_KEY_TEXT = "text";
    private static final String JSON_KEY_RECT = "rect";
    private static final String JSON_KEY_PEN_COLOR = "penColor";
    private static final String JSON_KEY_FILL_COLOR = "fillColor";
    private static final String JSON_KEY_TOP_LEFT = "topLeft";
    private static final String JSON_KEY_BOTTOM_RIGHT = "bottomRight";
    private static final String JSON_KEY_X = "x";
    private static final String JSON_KEY_Y = "y";

    public static final Parcelable.Creator<Annotation> CREATOR = new Parcelable.Creator<Annotation>() {

        public Annotation createFromParcel(Parcel source) {
            return new Annotation(source);
        }

        public Annotation[] newArray(int size) {
            return new Annotation[size];
        }

    };

    private String uuid;
    private int page;
    private String author;
    private boolean secretaire;
    private String date;
    private RectF rect;
    private String text;
    private String type;
    private String penColor;
    private String fillColor;
    private int step;
    private boolean updated = false;
    private boolean deleted = false;


    // <editor-fold desc="Constructors">


    public Annotation(@NonNull JsonObject json, int page, int step) {

        JsonExplorer jsonExplorer = new JsonExplorer(json);

        this.uuid = jsonExplorer.optString(JSON_KEY_ID);
        this.author = jsonExplorer.optString(JSON_KEY_AUTHOR, "");
        this.page = page;
        this.secretaire = jsonExplorer.optBoolean(JSON_KEY_IS_SECRETAIRE, false);
        this.date = jsonExplorer.optString(JSON_KEY_DATE);
        this.rect = new RectF(
                jsonExplorer.findObject(JSON_KEY_RECT).findObject(JSON_KEY_TOP_LEFT).optLong(JSON_KEY_X, 0),
                jsonExplorer.findObject(JSON_KEY_RECT).findObject(JSON_KEY_TOP_LEFT).optLong(JSON_KEY_Y, 0),
                jsonExplorer.findObject(JSON_KEY_RECT).findObject(JSON_KEY_BOTTOM_RIGHT).optLong(JSON_KEY_X, 0),
                jsonExplorer.findObject(JSON_KEY_RECT).findObject(JSON_KEY_BOTTOM_RIGHT).optLong(JSON_KEY_Y, 0)
        );
        this.text = jsonExplorer.optString(JSON_KEY_TEXT, "");
        this.type = jsonExplorer.optString(JSON_KEY_TYPE, "rect");
        this.step = step;
        this.penColor = jsonExplorer.optString(JSON_KEY_PEN_COLOR, "blue");
        this.fillColor = jsonExplorer.optString(JSON_KEY_FILL_COLOR, "undefined");
    }


    public Annotation(String author, int page, boolean secretaire, String date, RectF rect, String text, int step) {

        this.author = author;
        this.page = page;
        this.secretaire = secretaire;
        this.date = date;
        this.rect = rect;
        this.text = text;
        this.step = step;
    }


    public Annotation(String uuid, String author, int page, boolean secretaire, String date, RectF rect, String text, String type, int step) {

        this.uuid = uuid;
        this.author = author;
        this.page = page;
        this.secretaire = secretaire;
        this.date = date;
        this.rect = rect;
        this.text = text;
        this.type = type;
        this.step = step;
    }


    private Annotation(Parcel in) {
        this.uuid = in.readString();
        this.page = in.readInt();
        this.author = in.readString();
        this.secretaire = in.readByte() != 0;
        this.date = in.readString();
        this.rect = in.readParcelable(((Object) rect).getClass().getClassLoader());
        this.text = in.readString();
        this.type = in.readString();
        this.step = in.readInt();
        this.updated = in.readByte() != 0;
        this.deleted = in.readByte() != 0;
    }


    // </editor-fold desc="Constructors">


    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(uuid);
        out.writeInt(page);
        out.writeObject(author);
        out.writeBoolean(secretaire);
        out.writeObject(date);
        out.writeFloat(rect.left);
        out.writeFloat(rect.top);
        out.writeFloat(rect.bottom);
        out.writeFloat(rect.right);
        out.writeObject(text);
        out.writeObject(type);
        out.writeObject(penColor);
        out.writeObject(fillColor);
        out.writeInt(step);
        out.writeBoolean(updated);
        out.writeBoolean(deleted);
    }


    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        uuid = (String) in.readObject();
        page = in.readInt();
        author = (String) in.readObject();
        secretaire = in.readBoolean();
        date = (String) in.readObject();

        float left = in.readFloat();
        float top = in.readFloat();
        float right = in.readFloat();
        float bottom = in.readFloat();
        rect = new RectF(left, top, right, bottom);

        text = (String) in.readObject();
        type = (String) in.readObject();
        penColor = (String) in.readObject();
        fillColor = (String) in.readObject();
        step = in.readInt();
        updated = in.readBoolean();
        deleted = in.readBoolean();
    }

}
