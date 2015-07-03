package Model.Field;

import Model.IModel;
import Stuff.OverridingDefaultClasses.TruHashMap;
import Stuff.Tools.Logger;
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
	final private Class<E> elemClass;

	private E value;

	public E defaultValue = null;
	private Boolean isValueSet = false;
	Function<E, E> normalize = null;
	@Deprecated // i used it to repaint, but now we have separate repinting lambda
	private Runnable onChange = null;
	private BiFunction<Rectangle, E, Consumer<Graphics>> paintingLambda = null;
	private Boolean changedSinceLastRepaint = true;

    public Field(String name, E value, IModel owner) { this(name, value, owner, null); }

	public Field(String name, E value, IModel owner, Function<E, E> normalizeLambda) {
        this(name, (Class<E>)value.getClass(), false, owner);
		this.normalize = normalizeLambda;
		this.defaultValue = value;
        set(value);
	}

	public Field(String name, Class<E> cls, Boolean isFinal, IModel owner) {
		checkValueClass(cls);
		this.elemClass = cls;
		this.owner = owner;
		this.name = name;

		owner.getModelHelper().getFieldStorage().add(this);
		this.isFinal = isFinal;
	}

	// field getters/setters

	public E get() { return value; }

	public String getName() { return name; }

	public Field<E> set(E value) {
		if (this.isFinal && isValueSet) {
			Logger.fatal("You are trying to change immutable field! " + getName() + " " + get() + " " + value);
		} else {

			E normalizedValue = normalize != null ? normalize.apply(value) : value;
			if (!normalizedValue.equals(value)) {
				Logger.warning("Tried to set invalid value: [" + value + "] for Field [" + getName() + "] of [" + owner.getClass().getSimpleName() + "]");
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

	public void repaintIfNeeded(Graphics g, Rectangle r) {
		if (hasPaintingLambda() && changedSinceLastRepaint) {
			this.paintingLambda.apply(r, get()).accept(g);
			this.changedSinceLastRepaint = false;
		}
	}

	// TODO: do something similar for Arr when need to add/remove element
	public void setOnChange(Runnable onChange) {
		this.onChange = onChange;
	}

	// override me please!
	public Object getJsonValue() { return get(); };

	// override me please!
	public void setValueFromJsObject(JSONObject jsObject) {
		this.setValueFromString(jsObject.get(getName()).toString());
	}

	public Field setValueFromString(String str) {
		set((E)getParserMap().get(elemClass).apply(str));

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
