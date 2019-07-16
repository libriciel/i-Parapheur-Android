/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
	 * {@link ArrayList#addAll(Collection)} with a null check.
	 *
	 * @param list  the target list
	 * @param toAdd collection to add
	 * @param <E>   Generic type
	 */
	public static <E> void safeAddAll(@NonNull List<E> list, @Nullable Collection<? extends E> toAdd) {
		if (toAdd != null)
			list.addAll(toAdd);
	}

	/**
	 * Pretty print of every element's method call.
	 * Only no-parameters methods can be called, otherwise it'll pretty-print an exception.
	 *
	 * This method obviously uses reflexion, with an un-elegant amount of {@link Exception} catches.
	 * It should only be used for testing purposes.
	 *
	 * @param collection the collection to pretty-print
	 * @param methodName usually some "getId"
	 * @return a String like "[id_01, id_02]", "null", or a printed exception like "[-incompatible...-]"
	 */
	public static @NonNull String printListReflexionCall(@Nullable Collection<?> collection, @NonNull String methodName) {

		// Default cases

		if (collection == null)
			return "null";

		if (collection.isEmpty())
			return "[]";

		// Building String

		List<Object> list = new ArrayList<>(collection);
		StringBuilder result = new StringBuilder();

		try {
			Method getter = list.get(0).getClass().getMethod(methodName);

			result.append("[");
			for (int i = 0; i < list.size(); i++) {

				if (list.get(i) != null)
					result.append(getter.invoke(list.get(i)));
				else
					result.append("null");

				if (i < list.size() - 1)
					result.append(", ");
			}
			result.append("]");
		}
		catch (Exception e) {
			return "[-class incompatible with " + methodName + "()-]";
		}

		return result.toString();
	}

	/**
	 * Server sends date in ms and/or ISO8601, so we have to customize the Gson object to parse them easily.
	 * Since the parsing is waiting for long numbers, we customize the serialization too.
	 *
	 * @return {@link Gson} object
	 */
	public static @NonNull Gson buildGsonWithDateParser() {

		GsonBuilder builder = new GsonBuilder();

		builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {

			public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(date.getTime());
			}
		});

		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {

			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

				if (json.getAsJsonPrimitive().isNumber())
					return new Date(json.getAsJsonPrimitive().getAsLong());
				else
					return StringsUtils.parseIso8601Date(json.getAsString());
			}
		});

		return builder.create();
	}
}
