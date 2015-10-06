package org.adullact.iparapheur.utils;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtils {

	@SuppressWarnings("unused") public static @NonNull String bundleToString(@Nullable Bundle bundle) {
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
}
