package Model.Field;

import Model.AbstractModel;
import Model.IModel;
import Stuff.Tools.Logger;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// this class represents our model Field that sores list of AbstractModel-z
public class Arr<ELEM_CLASS extends AbstractModel> extends Field<Collection<ELEM_CLASS>> {

	Class<ELEM_CLASS> elemClass;
	Class<? extends Collection<ELEM_CLASS>> collectionClass;

	public Arr(String name, Collection<ELEM_CLASS> value, IModel owner, Class<ELEM_CLASS> elemClass) {
		super(name, value, owner);
		this.elemClass = elemClass;
		this.collectionClass = (Class<Collection<ELEM_CLASS>>)value.getClass();
	}

	@Override
	public JSONArray getJsonValue() {
		JSONArray arr = new JSONArray("[]");
		for (AbstractModel el: get()) {
			arr.put(el.getJsonRepresentation());
		}
		return arr;
	}

	@Override
	public void setValueFromJsObject(JSONObject jsObject) {
		get().clear();
		JSONArray arr = jsObject.getJSONArray(getName());
		for (int i = 0; i < arr.length(); ++i) {
			ELEM_CLASS el = null;
			try { el = elemClass.getDeclaredConstructor(owner.getClass()).newInstance(owner); }
			catch (Exception e) { Logger.fatal(e, "Failed to make instance of {" + elemClass.getSimpleName() + "}"); }

			el.reconstructFromJson(arr.getJSONObject(i)); // it's important to do reconstructFromJson before add, cuz the Collection may be a set
			get().add(el);
		}
	}

	public ELEM_CLASS get(int index) {
		return elemClass.cast(get().toArray()[index]);
	}
	public ELEM_CLASS add(ELEM_CLASS elem) {
		get().add(elem);
		set(get()); // problem? ... it's for onChange() lambda to be called
		return elem;
	}
	public ELEM_CLASS add(ELEM_CLASS elem, int index) {
		List<ELEM_CLASS> newList = new ArrayList<>(get());
		newList.add(index, elem);
		try { set(collectionClass.getDeclaredConstructor(Collection.class).newInstance(newList)); }
		catch (NoSuchMethodException exc) { Logger.fatal(exc, "I dont believe you! What is it a Collection, that cannot be created from another Collection?"); }
		catch (InstantiationException exc) { Logger.fatal(exc, "Abstract class? NO WAI!"); }
		catch (InvocationTargetException exc) { Logger.fatal(exc, "Bet it wont occur evar? I dont even know what this exception means"); }
		catch (IllegalAccessException exc) { Logger.fatal(exc, "Shouldn't private method work only on compilation time, like generics?"); } // heretic
		return elem;
	}

	@Override
	protected void checkValueClass(Class cls) {
		// nothing can go wrong i suppose =P
	}

	@Override
	public Field setValueFromString(String str) {
		Logger.fatal("Not supported " + str);
		return this;
	}
}
