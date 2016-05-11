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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;


public class Bureau implements Parcelable {

	public static Parcelable.Creator<Bureau> CREATOR = new Parcelable.Creator<Bureau>() {
		public Bureau createFromParcel(Parcel source) {
			return new Bureau(source);
		}

		public Bureau[] newArray(int size) {
			return new Bureau[size];
		}
	};

	private String mId;
	private String mTitle;
	private int mTodoCount;
	private int mLateCount;

	public Bureau(String id, String title) {
		this(id, title, 0, 0);
	}

	public Bureau(String id, String title, int todo, int late) {

		if (id.contains("workspace://SpacesStore/"))
			id = id.substring("workspace://SpacesStore/".length());

		mId = id;
		mTitle = title;
		mTodoCount = todo;
		mLateCount = late;
	}

	private Bureau(Parcel in) {
		mId = in.readString();
		mTitle = in.readString();
		mTodoCount = in.readInt();
		mLateCount = in.readInt();
	}

	public String getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public int getTodoCount() {
		return mTodoCount;
	}

	public int getLateCount() {
		return mLateCount;
	}

	@Override public String toString() {
		return mTitle;
	}

	@Override public boolean equals(Object o) {
		return (o != null) && (o instanceof Bureau) && TextUtils.equals(mId, ((Bureau) o).getId());
	}

	// <editor-fold desc="Parcelable">

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mTitle);
		dest.writeInt(mTodoCount);
		dest.writeInt(mLateCount);
	}

	// </editor-fold desc="Parcelable">
}
