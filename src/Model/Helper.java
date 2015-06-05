package Model;

import Model.Field.Arr;
import Model.Field.ModelField;
import Storyspace.Article.Paragraph;
import Stuff.Tools.Logger;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Helper {

	IModel model;

	public Helper(IModel model) {
		this.model = model;
	}

	@Deprecated
	public static JSONObject getJsonRepresentation(IModel model) {
		JSONObject dict = new JSONObject();
		model.getJsonRepresentation(dict);

		// TODO: arghhh! No instanceof! Interface::getHelper mazafaka!
		if (model instanceof Paragraph) {
			Helper helper = Paragraph.class.cast(model).getModelHelper();
			for (ModelField field : helper.fieldStorage) {
				dict.put(field.getName(), field.getValue());
			}
		}

		return dict;
	}

	public void getJsonRepresentation(JSONObject dict) {
		for (ModelField field : fieldStorage) {
			dict.put(field.getName(), field.getJsonValue());
		}
	}

	public void reconstructFromJson(JSONObject jsObject) {
		for (ModelField field : fieldStorage) {
			if (jsObject.has(field.getName())) { field.setValueFromJsObject(jsObject); }
			else { Logger.warning("Source does not have field [" + field.getName() + "] for class {" + getClass().getSimpleName() + "}"); }
		}
	}

	public Cursor getDefaultCursor() {
		return  model instanceof Paragraph ? Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR) :
				Cursor.getDefaultCursor();
	}

	// field getters

	private List<ModelField> fieldStorage = new ArrayList<>();

	public List<ModelField> getFieldStorage() { return fieldStorage; }

	public ModelField<Integer> addField(String fieldName, Integer fieldValue) {
		return new ModelField<>(fieldName, fieldValue, model).addTo(fieldStorage);
	}
	public ModelField<String> addField(String fieldName, String fieldValue) {
		return new ModelField<>(fieldName, fieldValue, model).addTo(fieldStorage);
	}
	public ModelField<Boolean> addField(String fieldName, Boolean fieldValue) {
		return new ModelField<>(fieldName, fieldValue, model).addTo(fieldStorage);
	}
	public Arr<? extends AbstractModel> addField(String fieldName, List<? extends AbstractModel> fieldValue, Class<? extends AbstractModel> cls) {
		return (Arr)new Arr(fieldName, fieldValue, model, cls).addTo(fieldStorage);
	}
}
