package org.adullact.iparapheur.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Allows to fetch a value for any given JsonObject or string, in a single line.
 * <p/>
 * <br/>
 * <p/>
 * It won't crash or throw an exception on any problem. It will just send null, and this null will be sent through the entire search.
 * On the last parse of a primitive element, it will give you the given default value, wherever the problem was.
 * <p/>
 * <br/>
 * Example :
 * <code>String title = new JSONUtils.JsonExplorer(JsonObject).findObject("content").findArray("texts").find(0).optString("title", "defaultValue")</code>
 */
@SuppressWarnings("unused")
public class JsonExplorer {

	private static Gson sGson = new Gson();
	private JsonElement _rootObject;
	private JsonElement _currentObject;

	// <editor-fold desc="Constructors">

	public JsonExplorer(String jsonString) {
		if (!TextUtils.isEmpty(jsonString)) {
			try {
				_currentObject = new JsonParser().parse(jsonString);
			}
			catch (JsonSyntaxException exception) {
				_currentObject = null;
			}
		}
		else {
			_currentObject = null;
		}

		_rootObject = _currentObject;
	}

	public JsonExplorer(JSONObject jsonObject) {
		_currentObject = sGson.fromJson(jsonObject.toString(), JsonElement.class);
		_rootObject = _currentObject;
	}

	public JsonExplorer(JSONArray jsonArray) {
		_currentObject = sGson.fromJson(jsonArray.toString(), JsonElement.class);
		_rootObject = _currentObject;
	}

	public JsonExplorer(JsonElement jsonObject) {
		_currentObject = jsonObject;
		_rootObject = _currentObject;
	}

	// </editor-fold desc="Constructors">

	public JsonElement getCurrentJsonElement() {
		return _currentObject;
	}

	private void resetRootElement() {
		_currentObject = _rootObject;
	}

	// <editor-fold desc="JsonElements">

	public JsonExplorer findObject(@NonNull String fieldName) {
		if ((_currentObject != null) && (_currentObject.isJsonObject()))
			_currentObject = ((JsonObject) _currentObject).getAsJsonObject(fieldName);
		else
			_currentObject = null;

		return this;
	}

	public JsonExplorer findArray(@NonNull String fieldName) {
		if ((_currentObject != null) && (_currentObject.isJsonObject()))
			_currentObject = ((JsonObject) _currentObject).getAsJsonArray(fieldName);
		else
			_currentObject = null;

		return this;
	}

	public JsonExplorer find(int index) {
		if ((_currentObject != null) && (_currentObject.isJsonArray()))
			_currentObject = ((JsonArray) _currentObject).get(index);
		else
			_currentObject = null;

		return this;
	}

	// </editor-fold desc="JsonElements">

	// <editor-fold desc="Primitive types">

	public String optString(@NonNull String fieldName) {
		return optString(fieldName, null);
	}

	public String optString(@NonNull String fieldName, @Nullable String defaultValue) {
		String result = null;

		if ((_currentObject != null) && (_currentObject.isJsonObject())) {
			JsonElement jsonElement = ((JsonObject) _currentObject).get(fieldName);

			if ((jsonElement != null) && (jsonElement.isJsonPrimitive()) && (jsonElement.getAsJsonPrimitive().isString()))
				result = jsonElement.getAsJsonPrimitive().getAsString();
		}

		resetRootElement();
		return (result == null) ? defaultValue : result;
	}

	public int optInt(@NonNull String fieldName, int defaultValue) {
		Integer result = null;

		if ((_currentObject != null) && (_currentObject.isJsonObject())) {
			JsonElement jsonElement = ((JsonObject) _currentObject).get(fieldName);

			if ((jsonElement != null) && (jsonElement.isJsonPrimitive() && (jsonElement.getAsJsonPrimitive().isNumber())))
				result = jsonElement.getAsJsonPrimitive().getAsNumber().intValue();
		}

		resetRootElement();
		return (result == null) ? defaultValue : result;
	}

	public boolean optBoolean(@NonNull String fieldName, boolean defaultValue) {
		boolean result = defaultValue;

		if ((_currentObject != null) && (_currentObject.isJsonObject())) {
			JsonElement jsonElement = ((JsonObject) _currentObject).get(fieldName);

			if ((jsonElement != null) && (jsonElement.isJsonPrimitive() && (jsonElement.getAsJsonPrimitive().isBoolean())))
				result = jsonElement.getAsJsonPrimitive().getAsBoolean();
		}

		resetRootElement();
		return result;
	}

	// </editor-fold desc="Primitive types">

	@Override
	public String toString() {
		return (_currentObject != null ? _currentObject.toString() : null);
	}
}