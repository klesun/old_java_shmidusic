
package org.klesun_model;

// AbstractModel is used to store field list, since an interface can't have properties...


import org.json.JSONException;
import org.json.JSONObject;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;
import org.klesun_model.field.IField;
import org.shmidusic.stuff.tools.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public abstract class AbstractModel implements IModel
{
	private Map<String, IField> fieldStorage = new LinkedHashMap<>();

	/** use this constructor when creating new object */
	public AbstractModel() {}

	/** use this constructor when restoring object from json */
	public AbstractModel(JSONObject state) {
		Logger.fatal("Please override this constructor!");
	}

	@Override
	public String toString() {
		return this.getJsonRepresentation().toString();
	}

	// the name because it can't guess generics when call in call
	private <E> Field<E> addIHateJava(String name, Field<E> field) {
		fieldStorage.put(name, field);
		return field;
	}

	/** mutable */
	protected <E> Field<E> add(String name, E value) {
		return addIHateJava(name, new Field<>(value));
	}

	protected <E> Field<E> add(String name, E value, Function<E,E> normalize) {
		return addIHateJava(name, new Field<>(value, normalize));
	}

	/** final */
	protected <E> Field<E> add(String name, Class<E> elemClass) {
		return addIHateJava(name, new Field<>(elemClass));
	}

	protected <E extends IModel> Arr<E> add(String name, Collection<E> container, Class<E> elemClass) {
		Arr<E> field = new Arr<>(container, elemClass);
		fieldStorage.put(name, field);
		return field;
	}

	public Map<String, IField> getFieldStorage() {
		return fieldStorage;
	}
}
