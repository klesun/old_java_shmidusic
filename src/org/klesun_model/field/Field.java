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

public class Field<E> {

	final private String name;
	final protected IModel owner;
	final public Boolean isFinal;
	final public Class<E> elemClass;

	private E value;

	public E defaultValue = null;
	private Boolean isValueSet = false;
	final public Function<E, E> normalize;
	@Deprecated // i used it to repaint, but now we have separate repinting lambda
	private Runnable onChange = null;
	private Boolean omitDefaultFromJson = false;
	private BiFunction<Rectangle, E, Consumer<Graphics>> paintingLambda = null;
	public Boolean changedSinceLastRepaint = true;

    public Field(String name, E value, IModel owner) { this(name, value, owner, a -> a); }

	public Field(String name, E value, IModel owner, Function<E, E> normalizeLambda) {
        this(name, (Class<E>)value.getClass(), false, owner, normalizeLambda);
		this.defaultValue = value;
        set(value);
	}

	public Field(String name, Class<E> cls, Boolean isFinal, IModel owner) {
		this(name, cls, isFinal, owner, a -> a);
	}

	private Field(String name, Class<E> cls, Boolean isFinal, IModel owner, Function<E, E> normalizeLambda) {
		checkValueClass(cls);
		this.elemClass = cls;
		this.owner = owner;
		this.name = name;
		this.normalize = normalizeLambda;

		owner.getModelHelper().getFieldStorage().add(this);
		this.isFinal = isFinal;
	}

	// field getters/setters

	public Field setOmitDefaultFromJson(Boolean value) {
		this.omitDefaultFromJson = value;
		return this;
	}

	public Boolean omitDefaultFromJson() {
		return this.omitDefaultFromJson;
	}

	public E get() { return value; }

	public String getName() { return name; }

	public Field<E> set(E value) {
		if (this.isFinal && isValueSet) {
			Logger.fatal("You are trying to change immutable field! " + getName() + " " + get() + " " + value);
		} else {

			E normalizedValue = normalize.apply(value);
			if (!normalizedValue.equals(value)) {
				Logger.warning("Tried to set invalid value: [" + value + "] for field [" + getName() + "] of [" + owner.getClass().getSimpleName() + "]");
			}

			this.changedSinceLastRepaint |= this.value != normalizedValue;
			this.value = normalizedValue;
			isValueSet = true;

			if (onChange != null) {
				onChange.run();
			}
		}
		return this;
	}

	public Field<E> setPaintingLambda(BiFunction<Rectangle, E, Consumer<Graphics>> paintingLambda) {
		this.paintingLambda = paintingLambda;
		return this;
	}

	public Boolean hasPaintingLambda() {
		return this.paintingLambda != null;
	}

	public void repaint(Graphics g, Rectangle r) {
		if (hasPaintingLambda()) {
			this.paintingLambda.apply(r, get()).accept(g);
			this.changedSinceLastRepaint = false;
		}
	}

	// TODO: do something similar for Arr when need to add/remove element
	public void setOnChange(Runnable onChange) {
		this.onChange = onChange;
	}

	// override me please!
	public Object getJsonValue() { return get().toString(); };

	// override me please!
	public void setValueFromJsObject(JSONObject jsObject)
	{
		this.setValueFromString(jsObject.get(getName()).toString());
	}

	public Field setValueFromString(String str)
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
	protected void checkValueClass(Class cls) {
		if (!getParserMap().containsKey(cls)) {
			Logger.fatal("Unsupported field Value Class! [" + cls.getSimpleName() + "]");
		}
	}
}
