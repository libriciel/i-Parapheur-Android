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
package org.adullact.iparapheur.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class BureauUtils {

	public static @Nullable Bureau findInList(@Nullable List<Bureau> bureauList, @Nullable String bureauId) {

		// Default case

		if ((bureauList == null) || (bureauId == null))
			return null;

		//

		for (Bureau bureau : bureauList)
			if (bureau != null)
				if (TextUtils.equals(bureau.getId(), bureauId))
					return bureau;

		return null;
	}

	/**
	 * Safe update, with old data cleanup.
	 *
	 * @param account       the parent account
	 * @param newBureauList the new full list
	 * @throws SQLException
	 */
	public static @NonNull List<Bureau> getDeletableBureauList(@NonNull final Account account, final @NonNull List<Bureau> newBureauList) {

		final List<Bureau> bureauToDeleteList = new ArrayList<>();
		CollectionUtils.safeAddAll(bureauToDeleteList, account.getChildrenBureaux());
		bureauToDeleteList.removeAll(newBureauList);

		return bureauToDeleteList;
	}

	public static @NonNull List<Dossier> getAllChildrenDossiers(@Nullable List<Bureau> bureauList) {

		List<Dossier> result = new ArrayList<>();

		if (bureauList != null)
			for (Bureau bureau : bureauList)
				CollectionUtils.safeAddAll(result, bureau.getChildrenDossiers());

		return result;
	}

	public static @NonNull List<Document> getAllChildrenDocuments(@Nullable List<Bureau> bureauList) {

		List<Document> result = new ArrayList<>();

		if (bureauList != null)
			for (Bureau bureau : bureauList)
				if (bureau.getChildrenDossiers() != null)
					for (Dossier dossier : bureau.getChildrenDossiers())
						CollectionUtils.safeAddAll(result, dossier.getChildrenDocuments());

		return result;
	}
}
