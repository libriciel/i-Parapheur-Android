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

	/**
	 * Here's the trick : MuPDF on Android rasterizes its PDF at 144dpi (2 x 72dpi).
	 * Ghostscript on the server rasterize at 150dpi, and takes that as a root scale.
	 * Every annotation has a pixel-coordinates based on that 150dpi, on the server.
	 * We need to translate it from 150 to 144dpi, by default.
	 * <p/>
	 * Not by default : The server-dpi is an open parameter, in the alfresco-global.properties file...
	 * So we can't hardcode the old "150 dpi", we have to let an open parameter too, to allow any density coordinates.
	 * <p/>
	 * Maybe some day, we'll want some crazy 300dpi on tablets, that's why we don't want to hardcode the new "144 dpi" one.
	 */
	public static RectF translateDpiRect(@NonNull RectF rect, int oldDpi, int newDpi) {
		return new RectF(rect.left * newDpi / oldDpi, rect.top * newDpi / oldDpi, rect.right * newDpi / oldDpi, rect.bottom * newDpi / oldDpi);
	}

	public static boolean isDebugOffline() {
		return DEBUG_FORCE_OFFLINE;
	}

	public static boolean isDebugSslForced() {
		return DEBUG_FORCE_SSL;
	}
}
