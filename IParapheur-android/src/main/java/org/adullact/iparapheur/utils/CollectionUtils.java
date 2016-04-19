package org.adullact.iparapheur.utils;

import java.util.Map;


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

}
