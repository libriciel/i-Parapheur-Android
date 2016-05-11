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

import android.content.Context;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.TypedValue;


@SuppressWarnings("unused")
public class DeviceUtils {

	private static final boolean DEBUG_FORCE_OFFLINE = false;
	private static final boolean DEBUG_FORCE_SSL = true;

	public static float dipsToPixels(@NonNull Context context, int dips) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.getResources().getDisplayMetrics());
	}

	public static boolean isDebugOffline() {
		return DEBUG_FORCE_OFFLINE;
	}

	public static boolean isDebugSslForced() {
		return DEBUG_FORCE_SSL;
	}
}
