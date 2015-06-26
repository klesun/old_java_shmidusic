package Stuff.OverridingDefaultClasses;

import java.util.HashMap;

public class SynchronizedHashMap<K, V> extends HashMap<K, V> {

	@Override
	public V put(K key, V value) {
		synchronized (this) {
			super.put(key, value);
		}
		return value;
	}

	@Override
	public V get(Object key) {
		V value;
		synchronized (this) {
			value = super.get(key);
		}
		return value;
	}

	public K findKey(K similarKey) {
		return keySet().stream().filter(k -> k.equals(similarKey)).findAny().orElse(null);
	}
}
