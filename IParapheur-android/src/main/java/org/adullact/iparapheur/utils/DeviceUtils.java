package org.adullact.iparapheur.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import org.adullact.iparapheur.R;

public class DeviceUtils {

	public static float dipsToPixels(@NonNull Context context, int dips) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.getResources().getDisplayMetrics());
	}

	public static boolean isDebugOffline(@NonNull Context context) {
		return context.getResources().getBoolean(R.bool.debug_force_offline);
	}
}
