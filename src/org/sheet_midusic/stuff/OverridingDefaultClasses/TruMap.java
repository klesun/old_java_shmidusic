package org.sheet_midusic.stuff.OverridingDefaultClasses;

import java.util.LinkedHashMap;

public class TruMap<KeyClass, ValueClass> extends LinkedHashMap {

	public TruMap<KeyClass, ValueClass> p(KeyClass key, ValueClass value) {
		put(key, value);
		return this;
	}

}
