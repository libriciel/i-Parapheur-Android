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

import java.util.ArrayList;
import java.util.Collections;
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

	@SafeVarargs public static <T> List<T> asList(T... objects) {
		ArrayList<T> result = new ArrayList<>();
		Collections.addAll(result, objects);
		return result;
	}

	@SafeVarargs public static <T> Set<T> asSet(T... objects) {
		HashSet<T> result = new HashSet<>();
		Collections.addAll(result, objects);
		return result;
	}
}
