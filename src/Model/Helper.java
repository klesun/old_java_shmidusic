package Model;

import Model.Field.Arr;
import Model.Field.Field;
import Storyspace.Article.Paragraph;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
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
			for (Field field : helper.fieldStorage) {
				dict.put(field.getName(), field.getValue());
			}
		}

		return dict;
	}

	public void getJsonRepresentation(JSONObject dict) {
		for (Field field : fieldStorage) {
			dict.put(field.getName(), field.getJsonValue());
		}
	}

	public void reconstructFromJson(JSONObject jsObject) {
		for (Field field : fieldStorage) {
			if (jsObject.has(field.getName())) { field.setValueFromJsObject(jsObject); }
			else { Logger.warning("Source does not have field [" + field.getName() + "] for class {" + getClass().getSimpleName() + "}"); }
		}
	}

	public Cursor getDefaultCursor() {
		return  model instanceof Paragraph ? Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR) :
				Cursor.getDefaultCursor();
	}

	// field getters

	private List<Field> fieldStorage = new ArrayList<>();

	public List<Field> getFieldStorage() { return fieldStorage; }

	public Field<Integer> addField(String fieldName, Integer fieldValue) {
		return new Field<>(fieldName, fieldValue, model).addTo(fieldStorage);
	}
	public Field<Fraction> addField(String fieldName, Fraction fieldValue) {
		return new Field<>(fieldName, fieldValue, model).addTo(fieldStorage);
	}
	public Field<String> addField(String fieldName, String fieldValue) {
		return new Field<>(fieldName, fieldValue, model).addTo(fieldStorage);
	}
	public Field<Boolean> addField(String fieldName, Boolean fieldValue) {
		return new Field<>(fieldName, fieldValue, model).addTo(fieldStorage);
	}
	public Arr<? extends AbstractModel> addField(String fieldName, List<? extends AbstractModel> fieldValue, Class<? extends AbstractModel> cls) {
		return (Arr)new Arr(fieldName, fieldValue, model, cls).addTo(fieldStorage);
	}

	final public static int limit(int value, int min, int max) { return Math.min(Math.max(value, min), max); }
}