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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.google.gson.annotations.SerializedName;


public class Document implements Parcelable {

	public static Creator<Document> CREATOR = new Creator<Document>() {
		public Document createFromParcel(Parcel source) {
			return new Document(source);
		}

		public Document[] newArray(int size) {
			return new Document[size];
		}
	};

	@SerializedName("id") private String mId;
	@SerializedName("name") private String mName;
	@SerializedName("size") private int mSize;                        // TODO : download image instead of too heavy files
	@SerializedName("isLocked") private boolean mIsLocked;
	//@SerializedName("visuelPdf") private boolean mIsPdfVisual;
	//@SerializedName("canDelete") private boolean mCanDelete;
	private SparseArray<PageAnnotations> mPagesAnnotations;
	private boolean mIsMainDocument;
	private String mUrl;                                              // URL of the document (its content)
	private String mPath;                                             // Path of the file (if downloaded) on the device's storage

	public Document() {}

	public Document(String id, String name, int size, String url, boolean isLocked, boolean isMainDocument) {
		mId = id;
		mName = name;
		mUrl = url;
		mSize = size;
		mPagesAnnotations = new SparseArray<>();
		mIsLocked = isLocked;
		mIsMainDocument = isMainDocument;
	}

	@SuppressWarnings("unchecked") private Document(Parcel in) {
		mId = in.readString();
		mName = in.readString();
		mPagesAnnotations = (SparseArray<PageAnnotations>) in.readBundle().get("mPagesAnnotations");
		mUrl = in.readString();
		mSize = in.readInt();
		mPath = in.readString();
		mIsLocked = (in.readByte() != 0);
		mIsMainDocument = (in.readByte() != 0);
	}

	// <editor-fold desc="Setters / Getters">

	public String getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public String getUrl() {
		return mUrl;
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

	public void setIsLocked(boolean isLocked) {
		mIsLocked = isLocked;
	}

	public boolean isMainDocument() {
		return mIsMainDocument;
	}

	public void setIsMainDocument(boolean isLocked) {
		mIsLocked = isLocked;
	}

	public SparseArray<PageAnnotations> getPagesAnnotations() {
		return mPagesAnnotations;
	}

	public void setPagesAnnotations(SparseArray<PageAnnotations> pagesAnnotations) {
		mPagesAnnotations = pagesAnnotations;
	}

	// </editor-fold desc="Setters / Getters">

	// <editor-fold desc="Parcelable">

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		bundle.putSparseParcelableArray("mPagesAnnotations", mPagesAnnotations);
		dest.writeString(mId);
		dest.writeString(mName);
		dest.writeBundle(bundle);
		dest.writeString(mUrl);
		dest.writeInt(mSize);
		dest.writeString(mPath);
		dest.writeByte((byte) (mIsLocked ? 1 : 0));
		dest.writeByte((byte) (mIsMainDocument ? 1 : 0));
	}

	// </editor-fold desc="Parcelable">

	@Override public String toString() {
		return "{Document id:" + mId + " name:" + mName + " isMainDoc:" + mIsMainDocument + "}";
	}

	@Override public boolean equals(Object o) {
		return (o != null) && (o instanceof Document) && (mId.contentEquals(((Document) o).getId()));
	}
}
