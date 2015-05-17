package Stuff.OverridingDefaultClasses;

import java.util.HashMap;

public class TruHashMap extends HashMap {

	public TruHashMap p(String key, Object value) {
		put(key, value);
		return this;
	}

}
