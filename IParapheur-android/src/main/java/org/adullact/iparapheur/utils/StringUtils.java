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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import org.adullact.iparapheur.R;
import org.adullact.iparapheur.controller.rest.api.IParapheurAPI;
import org.adullact.iparapheur.model.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings("unused")
public class StringUtils extends coop.adullactprojet.mupdffragment.utils.StringUtils {

	private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	public static @NonNull Comparator<Account> buildAccountAlphabeticalComparator(@NonNull final Context context) {

		return new Comparator<Account>() {
			@Override public int compare(Account lhs, Account rhs) {

				if (TextUtils.equals(lhs.getId(), context.getString(R.string.demo_account_id)))
					return 1;

				if (TextUtils.equals(rhs.getId(), context.getString(R.string.demo_account_id)))
					return -1;

				return lhs.getTitle().compareTo(rhs.getTitle());
			}
		};
	}

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

	public static @Nullable String urlEncode(@Nullable String string) {

		// Default value

		if (string == null)
			return null;

		// Encoding

		String result;

		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
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

	public static @Nullable Date parseIso8601Date(@Nullable String iso8601Date) {

		if ((iso8601Date == null) || (iso8601Date.length() == 0))
			return null;

		try { return new SimpleDateFormat(ISO_8601_DATE_FORMAT, Locale.FRENCH).parse(iso8601Date); }
		catch (ParseException ex) { return null; }
	}

	public static @NonNull String serializeToIso8601Date(@NonNull Date date) {
		return new SimpleDateFormat(ISO_8601_DATE_FORMAT, Locale.FRENCH).format(date);
	}

	public static boolean areNotEmpty(@Nullable String... strings) {

		if ((strings == null) || (strings.length == 0))
			return false;

		for (String string : strings)
			if (TextUtils.isEmpty(string))
				return false;

		return true;
	}

	public static @Nullable String utf8SignatureToBase64Ascii(@Nullable String utf8String) {

		// Default value

		if (utf8String == null)
			return null;

		// Wrapping result

		String temp = "-----BEGIN PKCS7-----\n";
		temp += utf8String + "\n";
		temp += "-----END PKCS7-----";

		byte[] bytes = Base64.encode(temp.getBytes(), Base64.NO_WRAP);

		// Building ASCII String

		Charset asciiCharset;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			asciiCharset = StandardCharsets.US_ASCII;
		else
			asciiCharset = Charset.forName("US-ASCII");

		return new String(bytes, asciiCharset);
	}

	/**
	 * Force the DN name, to pass the OpenSSL validation.
	 * OpenSSL validation crashes if the attributes are not in this exact name/order :
	 * "EMAIL=systeme@adullact.org,CN=AC ADULLACT Projet g2,OU=ADULLACT-Projet,O=ADULLACT-Projet,ST=Herault,C=FR"
	 *
	 * @param issuerDnName DN name, with attributes in any order
	 * @return fixed DN, that please OpenSSL
	 * @coveredInLocalUnitTest
	 */
	public static @NonNull String fixIssuerDnX500NameStringOrder(@NonNull String issuerDnName) {

		// Regex, without anti-slash escapes : ([A-Z]+)=(.*?(?<!\\)(?:\\{2})*)(?:,|$)
		//
		//  	([A-Z]+)=				Catches "AC=", "O=", etc.
		// 		(.*?)					Catches everything, "*?" makes it non-greedy
		//		(?<!\\)(?:\\{2})*		Checks if not followed by a odd number of \ (to keep escaped commas)
		// 		(?:,\s*|$)				Ending with a comma, or the end of the string

		String regex = "([A-Z]+)=(.*?(?<!\\\\)(?:\\\\{2})*)(?:,\\s*|$)";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(issuerDnName);
		Map<String, String> parsedData = new HashMap<>();

		while (matcher.find())
			parsedData.put(matcher.group(1), matcher.group(2));

		// Building result in the right order

		String res = "";

		res += "EMAIL=" + parsedData.get("E") + ",";
		res += "CN=" + parsedData.get("CN") + ",";
		res += "OU=" + parsedData.get("OU") + ",";
		res += "O=" + parsedData.get("O") + ",";
		res += "ST=" + parsedData.get("ST") + ",";
		res += "C=" + parsedData.get("C");

		return res;
	}

	/**
	 * Decode the Hexadecimal char sequence (as string) into Byte Array.
	 *
	 * @param data The Hex encoded sequence to be decoded.
	 * @return Decoded byte array.
	 * @throws IllegalArgumentException <var>data</var> when wrong number of chars is given or invalid chars.
	 */
	public static byte[] hexDecode(@NonNull String data) throws IllegalArgumentException {

		int length = data.length();
		if ((length % 2) != 0)
			throw new IllegalArgumentException("Odd number of characters.");

		try {
			byte[] bytes = new byte[length / 2];

			for (int i = 0, j = 0; i < length; i = i + 2)
				bytes[j++] = (byte) Integer.parseInt(data.substring(i, i + 2), 16);

			return bytes;
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal hexadecimal character.", e);
		}
	}

	public static @NonNull String fixUrl(@NonNull String url) {

		// Getting the server name
		// Regex :	- ignore everything before "://" (if exists)					^(?:.*://)*
		//			- then ignore following "m." (if exists)						(?:m\.)?
		//			- then catch every char but "/"	(not geedy)						(.*?)
		//			- then, ignore everything after the first "/" (if exists)		(?:/.*)*$
		String regex = "^(?:.*://)*(?:m\\.)?(.*?)(?:/.*)*$";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url.trim());

		String result = url;
		if (matcher.find() && !TextUtils.isEmpty(matcher.group(1)))
			result = matcher.group(1);

		return result;
	}

	public static @NonNull String getLocalizedSmallDate(@Nullable Date date) {

		if (date == null)
			return "???";

		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
		return dateFormat.format(date);
	}

	public static boolean isUrlValid(@Nullable String url) {

		if (TextUtils.isEmpty(url))
			return false;

		try {
			new URL(IParapheurAPI.BASE_PATH + url);
			return true;
		}
		catch (MalformedURLException ignored) {
			return false;
		}
	}

	public static @Nullable Boolean nullableBooleanValueOf(@NonNull Map<String, String> map, @NonNull String key) {

		if (map.containsKey(key) && (map.get(key) != null))
			return Boolean.valueOf(map.get(key));

		return null;
	}

	/**
	 * Helper functions to query a strings end portion. The comparison is case insensitive.
	 *
	 * @param base the base string.
	 * @param end  the ending text.
	 * @return true, if the string ends with the given ending text.
	 */
	public static boolean endsWithIgnoreCase(@Nullable String base, @Nullable String end) {

		// Default case

		if ((base == null) || (end == null))
			return false;

		//

		return (base.length() >= end.length()) && base.regionMatches(true, base.length() - end.length(), end, 0, end.length());
	}
}
