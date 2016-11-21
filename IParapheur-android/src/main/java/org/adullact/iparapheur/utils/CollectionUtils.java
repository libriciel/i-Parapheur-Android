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

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class CollectionUtils {

	/**
	 * Safe get, with default value
	 */
	public static <V, K> V opt(Map<K, V> map, K key, V defaultValue) {
		if ((map != null) && (map.containsKey(key)))
			return map.get(key);
		else
			return defaultValue;
	}

	@SafeVarargs public static <T> Set<T> asSet(T... objects) {
		HashSet<T> result = new HashSet<>();
		Collections.addAll(result, objects);
		return result;
	}

	/**
	 * Server sends date in ms, so we have to customize the Gson object to parse them easily.
	 * Since the parsing is waiting for long numbers, we customize the serialization too.
	 *
	 * @return {@link Gson} object
	 * @coveredInLocalUnitTest
	 */
	public static @NonNull Gson buildGsonWithLongToDate() {

		GsonBuilder builder = new GsonBuilder();

		builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {

			public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {

				if (date != null)
					return new JsonPrimitive(date.getTime());

				return null;
			}
		});

		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {

			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

				if (json.getAsJsonPrimitive().isNumber())
					return new Date(json.getAsJsonPrimitive().getAsLong());
				else
					return StringUtils.parseIso8601Date(json.getAsString());
			}
		});

		return builder.create();
	}
}
