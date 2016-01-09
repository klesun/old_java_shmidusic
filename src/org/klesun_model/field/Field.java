package org.klesun_model.field;

import org.klesun_model.IModel;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMap;
import org.shmidusic.stuff.tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;
import org.json.JSONObject;

import java.awt.*;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

// primitive field
// E generic may be only of [Number, String, Boolean]

public class Field<E> implements IField<E>
{
	private E value;

	private E defaultValue = null;
	private Boolean isValueSet = false;
	final public Class<E> elemClass;
	final public Function<E, E> normalize;
	private Boolean omitDefaultFromJson = false;

	final private Boolean isFinal;

    public Field(E value) { this(value, a -> a); }

	public Field(E value, Function<E, E> normalize) {
		this((Class<E>)value.getClass(), normalize, false);
		this.defaultValue = value;
		set(value);
	}

	public Field(Class<E> elemClass) {
		this(elemClass, a -> a, true);
	}

	private Field(Class<E> elemClass, Function<E, E> normalize, Boolean isFinal) {
		checkValueClass(elemClass);
		this.elemClass = elemClass;
		this.normalize = normalize;
		this.isFinal = isFinal;
	}

	public Field<E> setOmitDefaultFromJson(Boolean value) {
		this.omitDefaultFromJson = value;
		return this;
	}

	public Boolean omitDefaultFromJson() {
		return this.omitDefaultFromJson;
	}

	public E get() {
		if (!isValueSet) {
			Logger.fatal("You are trying to get not initialized field! " + (value == null ? "null" : value));
			return null;
		} else {
			return value;
		}
	}

	public Field<E> set(E value)
	{
		E normalizedValue = normalize.apply(value);
		if (!normalizedValue.equals(value)) {
			Logger.warning("Tried to set invalid value: [" + value + "] for field");
		}

		if (isFinal && isValueSet) {
			Logger.fatal("You are trying to change immutable field! " + get() + " " + value);
		} else {
			this.value = normalizedValue;
		};
		isValueSet = true;

		return this;
	}

	public Object getJsonValue() { return get().toString(); };
	public void setJsonValue(Object jsonValue) { this.setValueFromString(jsonValue.toString()); };

	protected Boolean hasAssignedValue() {
		return get() != defaultValue;
	}

	private Field<E> setValueFromString(String str)
	{
		set((E)getParserMap().get(elemClass).apply(str));
		return this;
	}

	private static Map<Class, Function<String, Object>> getParserMap() {
		TruMap<Class, Function<String, Object>> map = new TruMap<>();
		map.p(Integer.class, Integer::parseInt)
				.p(Boolean.class, Boolean::parseBoolean)
				.p(Fraction.class, new FractionFormat()::parse)
				.p(String.class, s -> s);
		return map;
	}

	/** dies if class is wrong */
	private void checkValueClass(Class cls) {
		if (!getParserMap().containsKey(cls)) {
			Logger.fatal("Unsupported field Value Class! [" + cls.getSimpleName() + "]");
		}
	}

	final public Boolean mustBeStored() {
		Boolean isOmmitable = elemClass == Boolean.class || omitDefaultFromJson;
		return !isOmmitable || hasAssignedValue();
	}

	// setJsonValue may be called if field is not marked as final
	public Boolean isFinal() {
		return isFinal;
	}
}
