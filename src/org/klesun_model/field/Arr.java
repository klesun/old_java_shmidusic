package org.klesun_model.field;

import org.klesun_model.IModel;
import org.shmidusic.stuff.tools.Logger;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// this class represents our model field that stores collections of AbstractModel-z
// TODO: it's not array anymore, it's collection - rename it!
public class Arr<ELEM_CLASS extends IModel> implements IField, Iterable<ELEM_CLASS>
{
	Class<ELEM_CLASS> elemClass;

	final Collection<ELEM_CLASS> elements;

	private Boolean omitDefaultFromJson = false;

	public Arr(Collection<ELEM_CLASS> value, Class<ELEM_CLASS> elemClass)
	{
		elements = value;
		this.elemClass = elemClass;
	}

	@Override
	public JSONArray getJsonValue() {
		JSONArray arr = new JSONArray("[]");
		for (IModel el: elements) {
			if (el.getJsonRepresentation().keySet().size() != 0 || !omitDefaultFromJson()) {
				arr.put(el.getJsonRepresentation());
			}
		}
		return arr;
	}

	@Override
	public void setJsonValue(Object jsonValue) {
		elements.clear();
		JSONArray arr = (JSONArray)jsonValue;
		for (int i = 0; i < arr.length(); ++i) {
			ELEM_CLASS el = null;
			try {
				el = elemClass.newInstance();
			} catch (Exception e) {
				Logger.fatal(e, "Come on, every class has an empty constructor in java! {" + elemClass.getSimpleName() + "}");
			}

			el.reconstructFromJson(arr.getJSONObject(i)); // it's important to do reconstructFromJson before add, cuz the Collection may be a set
			elements.add(el);
		}
	}

	public ELEM_CLASS get(int index) {
		return elemClass.cast(elements.toArray()[index]);
	}

	public ELEM_CLASS add(ELEM_CLASS elem) {
		elements.add(elem);
		return elem;
	}
	public ELEM_CLASS add(ELEM_CLASS elem, int index) {
		List<ELEM_CLASS> newList = new ArrayList<>(elements);
		newList.add(index, elem);
		setFromList(newList);
		return elem;
	}

	public void remove(ELEM_CLASS elem) {
		elements.remove(elem);
	}

	public int size() {
		return elements.size();
	}

	public int indexOf(ELEM_CLASS elem) {
		return IntStream.range(0, size()).boxed().filter(i -> get(i) == elem).findAny().get();
	}

	public void setFromList(Collection<ELEM_CLASS> list)
	{
		elements.clear();
		list.forEach(elements::add);
	}

	public Arr<ELEM_CLASS> setOmitDefaultFromJson(Boolean value) {
		this.omitDefaultFromJson = value;
		return this;
	}

	public Boolean omitDefaultFromJson() {
		return this.omitDefaultFromJson;
	}

	public Boolean mustBeStored() {
		return true;
	}

	public Stream<ELEM_CLASS> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}

	@Override
	public Iterator<ELEM_CLASS> iterator() {
		return elements.iterator();
	}

	// it's not supposed to call setJsonValue twice
	public Boolean isFinal() {
		return true;
	}
}
