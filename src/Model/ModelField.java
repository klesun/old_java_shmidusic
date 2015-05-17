package Model;

import Stuff.Tools.Logger;
import org.json.JSONObject;

// TODO: MAKE DEFINITELY separate wrapper for each primitive
public class ModelField {

	private String name;
	private Object value;

	public ModelField(String name, Object value) {
		if (new JSONObject("{}").getGetterByClass(value.getClass()) == null) {
			Logger.fatal("Unsupported Field Value Class! [" + value.getClass().getSimpleName() + "]");
		}
		this.value = value;
		this.name = name;
	}

	public void setValueFromJsObject(JSONObject jsObject) {
		Object value = jsObject.get(getName(), getValue().getClass());
		setValue(value);
	}

	// field getters/setters

	public Object getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public ModelField setValue(Object value) {
		if (this.value.getClass() != value.getClass()) {
			Logger.fatal("Little nigga wanna change field's class? Not so fast! [" +
				this.value.getClass().getSimpleName() + "] != [" +
				value.getClass().getSimpleName() + "]");
		}
		this.value = value;
		return this;
	}
}
