package Model.Field;

import Model.AbstractModel;
import Model.IModel;
import Stuff.Tools.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Arr<ELEM_CLASS extends AbstractModel> extends ModelField<List<ELEM_CLASS>> {

	Class<ELEM_CLASS> elemClass;

	public Arr(String name, List<ELEM_CLASS> value, IModel owner, Class<ELEM_CLASS> elemClass) {
		super(name, value, owner);
		this.elemClass = elemClass;
	}

	@Override
	public JSONArray getJsonValue() {
		JSONArray arr = new JSONArray("[]");
		for (AbstractModel el: getValue()) {
			arr.put(el.getJsonRepresentation());
		}
		return arr;
	}

	@Override
	public void setValueFromJsObject(JSONObject jsObject) {
		getValue().clear();
		JSONArray arr = jsObject.getJSONArray(getName());
		for (int i = 0; i < arr.length(); ++i) {
			ELEM_CLASS el = null;
			try { el = elemClass.getDeclaredConstructor(owner.getClass()).newInstance(owner); }
			catch (Exception e) { Logger.fatal(e, "Failed to make instance of {" + elemClass.getSimpleName() + "}"); }

			el.reconstructFromJson(arr.getJSONObject(i));
			getValue().add(el);
		}
	}
}
