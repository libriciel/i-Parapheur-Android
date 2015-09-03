package org.adullact.iparapheur.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

	public static @NonNull String inputStreamToString(@NonNull InputStream is) throws IOException {
		String line;
		StringBuilder total = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		while ((line = rd.readLine()) != null)
			total.append(line);

		return total.toString();
	}

	public static @Nullable Date parseISO8601Date(@Nullable String iso8601Date) {
		if ((iso8601Date == null) || iso8601Date.isEmpty())
			return null;

		try {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.FRENCH).parse(iso8601Date);
		}
		catch (ParseException ex) {
			return null;
		}
	}
}
