/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.model;

import android.util.SparseArray;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.adullact.iparapheur.utils.SerializableSparseArray;

import java.util.Date;


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
	private String mId;

	@DatabaseField(columnName = DB_FIELD_NAME, canBeNull = false, defaultValue = "")  //
	@SerializedName("name")  //
	private String mName;

	@DatabaseField(columnName = DB_FIELD_SIZE, defaultValue = "-1")  //
	@SerializedName("size")  //
	private int mSize;                          // TODO : download image instead of too heavy files

	@DatabaseField(columnName = DB_FIELD_IS_PDF_VISUAL, defaultValue = "false")  //
	@SerializedName("visuelPdf")  //
	private boolean mIsPdfVisual;

	@DatabaseField(columnName = DB_FIELD_IS_MAIN_DOCUMENT)  //
	@SerializedName("isMainDocument")  //
	private boolean mIsMainDocument;

	@DatabaseField(columnName = DB_FIELD_ANNOTATIONS, dataType = DataType.SERIALIZABLE)  //
	private SerializableSparseArray<PageAnnotations> mPagesAnnotations;

	@DatabaseField(columnName = DB_FIELD_SYNC)  //
	private Date mSyncDate;

	@DatabaseField(columnName = DB_FIELD_DOSSIER, foreign = true, foreignAutoRefresh = true)  //
	private transient Dossier mParent;

	public Document(String id, String name, int size, boolean isMainDocument, boolean isPdfVisual) {
		mId = id;
		mName = name;
		mSize = size;
		mPagesAnnotations = new SerializableSparseArray<>();
		mIsMainDocument = isMainDocument;
		mIsPdfVisual = isPdfVisual;
	}

	public Document() {}

	// <editor-fold desc="Setters / Getters">

	public String getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public int getSize() {
		return mSize;
	}

	public boolean isMainDocument() {
		return mIsMainDocument;
	}

	public boolean isPdfVisual() {
		return mIsPdfVisual;
	}

	public SparseArray<PageAnnotations> getPagesAnnotations() {
		return mPagesAnnotations;
	}

	public void setPagesAnnotations(SerializableSparseArray<PageAnnotations> pagesAnnotations) {
		mPagesAnnotations = pagesAnnotations;
	}

	public Date getSyncDate() {
		return mSyncDate;
	}

	public void setSyncDate(Date syncDate) {
		mSyncDate = syncDate;
	}

	public Dossier getParent() {
		return mParent;
	}

	public void setParent(Dossier parent) {
		mParent = parent;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{Document id:" + mId + " name:" + mName + " isMainDoc:" + mIsMainDocument + "}";
	}

	@Override public boolean equals(Object o) {
		return (o != null) && (o instanceof Document) && (mId.contentEquals(((Document) o).getId()));
	}

}
