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

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class ParapheurType {

	@SerializedName("id") private String mName;
	@SerializedName("sousTypes") List<String> mSubTypes;

	// <editor-fold desc="Setters / Getters">

	public String getName() {
		return mName;
	}

	public List<String> getSubTypes() {
		return mSubTypes;
	}

	// </editor-fold desc="Setters / Getters">

	@Override public String toString() {
		return "{Type id=" + mName + " sousType=" + mSubTypes + "}";
	}
}
