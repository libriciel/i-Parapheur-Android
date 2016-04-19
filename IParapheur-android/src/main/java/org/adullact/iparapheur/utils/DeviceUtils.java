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
