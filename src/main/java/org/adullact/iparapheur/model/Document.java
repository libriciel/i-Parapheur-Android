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

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.adullact.iparapheur.utils.SerializableSparseArray;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
@NoArgsConstructor
@DatabaseTable(tableName = "Document")
public class Document {

    public static final String DB_FIELD_ID = "Id";
    private static final String DB_FIELD_NAME = "Name";
    private static final String DB_FIELD_SIZE = "Size";
    private static final String DB_FIELD_IS_PDF_VISUAL = "IsPdfVisual";
    private static final String DB_FIELD_IS_MAIN_DOCUMENT = "IsMainDocument";
    private static final String DB_FIELD_ANNOTATIONS = "Annotations";
    private static final String DB_FIELD_SYNC = "Sync";
    private static final String DB_FIELD_DOSSIER = "Dossier";


    @DatabaseField(columnName = DB_FIELD_ID, id = true, index = true)  //
    @SerializedName("id")  //
    private String id;

    @DatabaseField(columnName = DB_FIELD_NAME, canBeNull = false, defaultValue = "")  //
    @SerializedName("name")  //
    private String name;

    @DatabaseField(columnName = DB_FIELD_SIZE, defaultValue = "-1")  //
    @SerializedName("size")  //
    private int size;                          // TODO : download image instead of too heavy files

    @DatabaseField(columnName = DB_FIELD_IS_PDF_VISUAL, defaultValue = "false")  //
    @SerializedName("visuelPdf")  //
    private boolean isPdfVisual;

    @DatabaseField(columnName = DB_FIELD_IS_MAIN_DOCUMENT)  //
    @SerializedName("isMainDocument")  //
    private boolean isMainDocument;

    @DatabaseField(columnName = DB_FIELD_ANNOTATIONS, dataType = DataType.SERIALIZABLE)  //
    private SerializableSparseArray<PageAnnotations> pagesAnnotations;

    @DatabaseField(columnName = DB_FIELD_SYNC)  //
    private Date syncDate;

    @DatabaseField(columnName = DB_FIELD_DOSSIER, foreign = true, foreignAutoRefresh = true)  //
    private transient Dossier parent;


    public Document(String id, String name, int size, boolean isMainDocument, boolean isPdfVisual) {
        this.id = id;
        this.name = name;
        this.size = size;
        pagesAnnotations = new SerializableSparseArray<>();
        this.isMainDocument = isMainDocument;
        this.isPdfVisual = isPdfVisual;
    }


    @Override public boolean equals(Object o) {
        return (o instanceof Document) && (id.contentEquals(((Document) o).getId()));
    }


    @Override public int hashCode() {
        return id.hashCode();
    }

}
