package Model.Field;

import Model.IModel;
import Stuff.Tools.Logger;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Function;

public class ModelField<huj> {

	private String name;
	private huj value;
	protected IModel owner;

	public ModelField(String name, huj value, IModel owner) {
		if (new JSONObject("{}").getGetterByClass(value.getClass()) == null) {
			Logger.fatal("Unsupported Field Value Class! [" + value.getClass().getSimpleName() + "]");
		}
		this.owner = owner;
		this.value = value;
		this.name = name;
	}

	// override me please!
	public Object getJsonValue() { return getValue(); };

	// override me please!
	public void setValueFromJsObject(JSONObject jsObject) {
		huj value = (huj)jsObject.get(getName(), getValue().getClass());
		setValue(value);
	}

	public ModelField<huj> addTo(List<ModelField> fieldStorage) {
		fieldStorage.add(this);
		return this;
	}

	// field getters/setters

	public huj getValue() { return value; }

	public String getName() { return name; }

	public ModelField setValue(huj value) {
		this.value = value;
		return this;
	}

	public ModelField setValueFromString(String str) {
		setValue((huj)getParseStringLambda().apply(str));
		return this;
	}

	private Function<String, Object> getParseStringLambda() {
		return 	getValue().getClass() == Integer.class ? Integer::parseInt :
				getValue().getClass() == Boolean.class ? Boolean::parseBoolean :
				getValue().getClass() == String.class ? s -> s :
				s -> Logger.fatal("NO WAI!!!");

	}
}
