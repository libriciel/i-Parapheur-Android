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
package org.adullact.iparapheur.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.adullact.iparapheur.model.Account;
import org.adullact.iparapheur.model.Bureau;

import java.util.ArrayList;
import java.util.Comparator;
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

	public static @NonNull List<Bureau> getDeletableBureauList(@NonNull final Account account, final @NonNull List<Bureau> newBureauList) {

		final List<Bureau> bureauToDeleteList = new ArrayList<>();
		CollectionUtils.safeAddAll(bureauToDeleteList, account.getChildrenBureaux());
		bureauToDeleteList.removeAll(newBureauList);

		return bureauToDeleteList;
	}

	public static @NonNull Comparator<Bureau> buildAlphabeticalComparator() {

		return new Comparator<Bureau>() {
			@Override public int compare(Bureau lhs, Bureau rhs) {
				return lhs.getTitle().compareTo(rhs.getTitle());
			}
		};
	}
}
