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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


@DatabaseTable(tableName = "Document")
public class Document {

	@DatabaseField(columnName = "Id", id = true, index = true)  //
	@SerializedName("id")  //
	private String mId;

	@DatabaseField(columnName = "Name", canBeNull = false, defaultValue = "")  //
	@SerializedName("name")  //
	private String mName;

	@DatabaseField(columnName = "Size", defaultValue = "-1")  //
	@SerializedName("size")  //
	private int mSize;                          // TODO : download image instead of too heavy files

	@DatabaseField(columnName = "IsPdfVisual", defaultValue = "false")  //
	@SerializedName("visuelPdf")  //
	private boolean mIsPdfVisual;

	@DatabaseField(columnName = "IsMainDocument")  //
	@SerializedName("isMainDocument")  //
	private boolean mIsMainDocument;

	@DatabaseField(columnName = "Path")  //
	private String mPath;                                               // Path of the file (if downloaded) on the device's storage

	@DatabaseField(columnName = "Sync")  //
	private Date mSyncDate;

	@DatabaseField(foreign = true, foreignAutoRefresh = true)  //
	private Dossier mParent;

	private SparseArray<PageAnnotations> mPagesAnnotations;

	// <editor-fold desc="Static utils">

	public static @Nullable String generateContentUrl(@NonNull Document document) {

		if (document.getId() == null)
			return null;

		String downloadUrl = "/api/node/workspace/SpacesStore/" + document.getId() + "/content";
		if (document.isPdfVisual())
			downloadUrl += ";ph:visuel-pdf";

		return downloadUrl;
	}

	public static boolean isMainDocument(@NonNull Dossier dossier, @NonNull Document document) {

		// Default case

		if ((dossier.getDocumentList() == null) || !dossier.getDocumentList().contains(document))
			return false;

		// Api4 case :
		// If the mainDoc wasn't the first one in the list,
		// But there is at least one declared main document,
		// Then the first doc isn't the main one...

		if (document.isMainDocument())
			return true;

		for (Document doc : dossier.getDocumentList())
			if (doc.isMainDocument())
				return false;

		// Api3 case :
		// We already know here the list isn't empty,
		// and the first document is the only main one.

		return (TextUtils.equals(dossier.getDocumentList().get(0).getId(), document.getId()));
	}

	// </editor-fold desc="Static utils">

	public Document(String id, String name, int size, boolean isMainDocument, boolean isPdfVisual) {
		mId = id;
		mName = name;
		mSize = size;
		mPagesAnnotations = new SparseArray<>();
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

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		mPath = path;
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

	public void setPagesAnnotations(SparseArray<PageAnnotations> pagesAnnotations) {
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
