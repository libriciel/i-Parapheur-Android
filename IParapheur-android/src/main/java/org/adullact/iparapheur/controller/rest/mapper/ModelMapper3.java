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
package org.adullact.iparapheur.controller.rest.mapper;

import android.support.annotation.NonNull;

import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.utils.JsonExplorer;

import java.util.ArrayList;


public class ModelMapper3 extends ModelMapper {

	@Override public @NonNull ArrayList<Bureau> getBureaux(RequestResponse response) {
		ArrayList<Bureau> bureaux = new ArrayList<>();

		if (response.getResponseArray() == null)
			return bureaux;

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponseArray());
		for (int i = 0; i < response.getResponseArray().length(); i++) {

			int lateCount = jsonExplorer.find(i).optInt("en-retard", 0);
			int todoCount = jsonExplorer.find(i).optInt("a-traiter", 0);
			String name = jsonExplorer.find(i).optString("name", "");
			String bureauRef = jsonExplorer.find(i).optString("nodeRef", null);

			if (bureauRef != null)
				bureaux.add(new Bureau(bureauRef, name, todoCount, lateCount));
		}

		return bureaux;
	}

}
