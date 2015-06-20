package Stuff.OverridingDefaultClasses;

import java.util.HashMap;

public class TruHashMap<KeyClass, ValueClass> extends HashMap {

	public TruHashMap<KeyClass, ValueClass> p(KeyClass key, ValueClass value) {
		put(key, value);
		return this;
	}

}
