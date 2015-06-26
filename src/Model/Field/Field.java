package Model.Field;

import Model.IModel;
import Stuff.OverridingDefaultClasses.TruHashMap;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Field<EncapsulatedClass> {

	private String name;
	private EncapsulatedClass value;
	protected IModel owner;

	private Boolean isFinal = false;
	private Boolean isValueSet = false;
	private Class<EncapsulatedClass> elemClass;

	Function<EncapsulatedClass, EncapsulatedClass> normalize = null;

	public Field(String name, EncapsulatedClass value, IModel owner, Function<EncapsulatedClass, EncapsulatedClass> normalizeLambda) {
		this(name, value, owner);
		this.normalize = normalizeLambda;
	}

	public Field(String name, EncapsulatedClass value, IModel owner) {
		this(name, (Class<EncapsulatedClass>)value.getClass(), false, owner);
		set(value);
	}

	public Field(String name, Class<EncapsulatedClass> cls, Boolean isFinal, IModel owner) {
		checkValueClass(cls);
		this.elemClass = cls;
		this.owner = owner;
		this.name = name;

		owner.getModelHelper().getFieldStorage().add(this);
		this.isFinal = isFinal;
	}

	// field getters/setters

	public EncapsulatedClass get() { return value; }

	public String getName() { return name; }

	public Field set(EncapsulatedClass value) {
		if (this.isFinal && isValueSet) {
			Logger.fatal("You are trying to change immutable field! " + getName() + " " + get() + " " + value);
		} else {

			EncapsulatedClass normalizedValue = normalize != null ? normalize.apply(value) : value;
			if (!normalizedValue.equals(value)) {
				Logger.warning("Tried to set invalid value: [" + value + "] for Field [" + getName() + "] of [" + owner.getClass().getSimpleName() + "]");
			}

			this.value = normalizedValue;
			isValueSet = true;
		}
		return this;
	}

	// override me please!
	public Object getJsonValue() { return get(); };

	// override me please!
	public void setValueFromJsObject(JSONObject jsObject) {
		this.setValueFromString(jsObject.get(getName()).toString());
	}

	public Field setValueFromString(String str) {
		set((EncapsulatedClass)getParserMap().get(elemClass).apply(str));

		return this;
	}

	private static Map<Class, Function<String, Object>> getParserMap() {
		TruHashMap<Class, Function<String, Object>> map = new TruHashMap<>();
		map.p(Integer.class, Integer::parseInt)
			.p(Boolean.class, Boolean::parseBoolean)
			.p(Fraction.class, new FractionFormat()::parse)
			.p(String.class, s -> s);
		return map;
	}

	/** dies if class is wrong */
	protected void checkValueClass(Class cls) {
		if (!getParserMap().containsKey(cls)) {
			Logger.fatal("Unsupported Field Value Class! [" + cls.getSimpleName() + "]");
		}
	}
}
