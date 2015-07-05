package Model;

import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONException;
import org.json.JSONObject;

public interface IModel {

	IComponentModel getModelParent();
	Helper getModelHelper();

	default void getJsonRepresentation(JSONObject dict) {
		getModelHelper().getJsonRepresentation(dict);
	}
	default JSONObject getJsonRepresentation() {
		return Helper.getJsonRepresentation(this);
	}
	default IModel reconstructFromJson(JSONObject jsObject) throws JSONException {
		return getModelHelper().reconstructFromJson(jsObject);
	}

	default int limit(int value, int min, int max) { return Math.min(Math.max(value, min), max); }
	default Fraction limit(Fraction value, Fraction min, Fraction max) {
		value = value.compareTo(min) < 0 ? min : value;
		value = value.compareTo(max) > 0 ? max : value;
		return value;
	}
}
