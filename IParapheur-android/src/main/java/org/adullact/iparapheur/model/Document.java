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
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.annotations.SerializedName;


public class Document {

	@SerializedName("id") private String mId;
	@SerializedName("name") private String mName;
	@SerializedName("size") private int mSize;                          // TODO : download image instead of too heavy files
	@SerializedName("isLocked") private boolean mIsLocked;
	@SerializedName("visuelPdf") private boolean mIsPdfVisual;
	@SerializedName("canDelete") private boolean mCanDelete;
	@SerializedName("isMainDocument") private boolean mIsMainDocument;

	private String mPath;                                               // Path of the file (if downloaded) on the device's storage
	private SparseArray<PageAnnotations> mPagesAnnotations;

	// <editor-fold desc="Static utils">

	public static @NonNull String generateContentUrl(@NonNull Document document) {

		String downloadUrl = "/api/node/workspace/SpacesStore/" + document.getId() + "/content";
		if (document.isPdfVisual())
			downloadUrl += ";ph:visuel-pdf";

		return downloadUrl;
	}

	public static boolean isMainDocument(@NonNull Dossier dossier, @NonNull Document document) {

		return document.isMainDocument()                                                            // Api4 case
				|| (dossier.getDocumentList().size() == 1)                                          // Api3 default case
				|| TextUtils.equals(dossier.getDocumentList().get(0).getId(), document.getId());    // Api3 other case
	}

	// </editor-fold desc="Static utils">

	public Document() {}

	public Document(String id, String name, int size, boolean isLocked, boolean isMainDocument) {
		mId = id;
		mName = name;
		mSize = size;
		mPagesAnnotations = new SparseArray<>();
		mIsLocked = isLocked;
		mIsMainDocument = isMainDocument;
	}

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

	public boolean isLocked() {
		return mIsLocked;
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

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{Document id:" + mId + " name:" + mName + " isMainDoc:" + mIsMainDocument + "}";
	}

	@Override public boolean equals(Object o) {
		return (o != null) && (o instanceof Document) && (mId.contentEquals(((Document) o).getId()));
	}
}
