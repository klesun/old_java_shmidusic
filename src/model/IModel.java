package model;

import model.field.Field;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONException;
import org.json.JSONObject;
import stuff.tools.Logger;

import java.awt.*;

public interface IModel {

	IComponentModel getModelParent();
	Helper getModelHelper();

	default JSONObject getJsonRepresentation() {

		JSONObject dict = new JSONObject();

		getModelHelper().getFieldStorage().stream()
			.filter(f -> f.get().getClass() != Boolean.class || f.get() != f.defaultValue) // Issue[69]
			.filter(f -> !f.omitDefaultFromJson() || f.get() != f.defaultValue)
			.forEach(field -> dict.put(field.getName(), field.getJsonValue()));

		return dict;
	}

	default IModel reconstructFromJson(JSONObject jsObject) throws JSONException {

		for (Field field : getModelHelper().getFieldStorage()) {
			if (jsObject.has(field.getName())) {
				field.setValueFromJsObject(jsObject);
			} else if (field.isFinal) {
				Logger.fatal("Source does not have final field [" + field.getName() + "] for class {" + getClass().getSimpleName() + "}");
			}
		}
		return this;
	}

	default <T> Field<T> addField(String name, T defaultValue) {
		return new Field<>(name, defaultValue, this);
	}

	default Cursor getDefaultCursor() {
		return Cursor.getDefaultCursor();
	}

	default int limit(int value, int min, int max) { return Math.min(Math.max(value, min), max); }
	default Fraction limit(Fraction value, Fraction min, Fraction max) {
		value = value.compareTo(min) < 0 ? min : value;
		value = value.compareTo(max) > 0 ? max : value;
		return value;
	}
}
