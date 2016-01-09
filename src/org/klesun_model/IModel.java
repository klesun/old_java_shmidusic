package org.klesun_model;

import org.klesun_model.field.Field;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONException;
import org.json.JSONObject;
import org.klesun_model.field.IField;
import org.shmidusic.stuff.tools.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// IModel can store itself to json and be reconstructed from json

public interface IModel
{
	Map<String, IField> getFieldStorage();

	default JSONObject getJsonRepresentation()
	{
		JSONObject dict = new JSONObject();

		getFieldStorage().entrySet().stream()
			.filter(e -> e.getValue().mustBeStored())
			.forEach(e -> dict.put(e.getKey(), e.getValue().getJsonValue()));

		return dict;
	}

	default IModel reconstructFromJson(JSONObject jsObject) throws JSONException
	{
		/** @debug */
		System.out.println("gonna reconstruct " + Arrays.toString(getFieldStorage().keySet().toArray()));

		for (Map.Entry<String, IField> e: getFieldStorage().entrySet()) {
			if (jsObject.has(e.getKey())) {
				e.getValue().setJsonValue(jsObject.get(e.getKey()));
			} else if (e.getValue().isFinal()) {
				Logger.fatal("final field not present in json! " + e.getKey());
			} else {
				Logger.warning("field not present in json " + e.getKey());
                // TODO: DIE HORRIBLY. it was bad idea to omit default values. at least it should be list of fields that can be omitted, no allowing omit anything!
            }
		}
		return this;
	}

	default Set<String> getFieldList() {
		return getFieldStorage().keySet();
	}

	default int limit(int value, int min, int max) { return Math.min(Math.max(value, min), max); }
	default Fraction limit(Fraction value, Fraction min, Fraction max) {
		value = value.compareTo(min) < 0 ? min : value;
		value = value.compareTo(max) > 0 ? max : value;
		return value;
	}

	/** @not sure if it is good idea... */
	default Boolean mustBeStored() {
		return true;
	}
}
