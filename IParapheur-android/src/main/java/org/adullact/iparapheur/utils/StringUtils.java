package org.adullact.iparapheur.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class StringUtils {

	public static @NonNull String bundleToString(@Nullable Bundle bundle) {
		if (bundle == null)
			return "(Bundle null)";

		if (bundle.isEmpty())
			return "(Bundle empty)";

		StringBuilder stringBuilder = new StringBuilder("(Bundle");
		for (String key : bundle.keySet()) {
			stringBuilder.append(" ");
			stringBuilder.append(key);
			stringBuilder.append(":");
			stringBuilder.append(bundle.get(key));
		}
		stringBuilder.append(")");

		return stringBuilder.toString();
	}

	@SuppressLint("NewApi")
	public static @Nullable String urlEncode(@Nullable String string) {
		String result;

		try {
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
				result = URLEncoder.encode(string, StandardCharsets.UTF_8.name());
			else
				result = URLEncoder.encode(string, "UTF-8");
		}
		catch (UnsupportedEncodingException exception) {
			result = null;
		}

		return result;
	}

}
