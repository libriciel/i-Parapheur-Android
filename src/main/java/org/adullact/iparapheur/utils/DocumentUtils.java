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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DocumentUtils {

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

	public static @NonNull File getFile(@NonNull Context context, @NonNull Dossier dossier, @NonNull Document document) {

		String documentName = document.getName() + (StringsUtils.endsWithIgnoreCase(document.getName(), ".pdf") ? "" : "_visuel.pdf");
		return new File(FileUtils.getDirectoryForDossier(context, dossier), documentName);
	}

	public static @NonNull List<Document> getDeletableDossierList(@NonNull List<Dossier> parentDossierList, @NonNull List<Document> newDocumentList) {

		final List<Document> documentsToDeleteList = new ArrayList<>();

		for (Dossier parentDossier : parentDossierList)
			CollectionUtils.safeAddAll(documentsToDeleteList, parentDossier.getChildrenDocuments());

		documentsToDeleteList.removeAll(newDocumentList);

		return documentsToDeleteList;
	}

	public static @NonNull List<Document> getAllChildrenFrom(@Nullable List<Dossier> dossierList) {

		List<Document> result = new ArrayList<>();

		if (dossierList != null)
			for (Dossier dossier : dossierList)
				if (dossier.getChildrenDocuments() != null)
					CollectionUtils.safeAddAll(result, dossier.getChildrenDocuments());

		return result;
	}
}
