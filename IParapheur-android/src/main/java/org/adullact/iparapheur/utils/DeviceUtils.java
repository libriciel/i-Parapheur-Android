package org.adullact.iparapheur.utils;

import android.content.Context;
import android.util.TypedValue;

public class DeviceUtils {

	public static float dipsToPixels(Context context, int dips) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.getResources().getDisplayMetrics());
	}

}
