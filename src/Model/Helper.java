package Model;

import Model.Field.AbstractModelField;
import Model.Field.Bool;
import Model.Field.Int;
import Storyspace.Article.Paragraph;
import Stuff.Tools.Logger;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Helper {

	Component /* IComponentModel */ model;

	public Helper(Component /* IComponentModel */ model) {
		this.model = model;
	}

	public static JSONObject getJsonRepresentation(IModel model) {
		JSONObject dict = new JSONObject();
		model.getJsonRepresentation(dict);

		// TODO: arghhh! No instanceof! Interface::getHelper mazafaka!
		if (model instanceof Paragraph) {
			Helper helper = Paragraph.class.cast(model).getModelHelper();
			for (AbstractModelField field : helper.fieldValueStorage) {
				dict.put(field.getName(), field.getValue());
			}
		}

		return dict;
	}

	public void reconstructFromJson(JSONObject jsObject) {
		for (AbstractModelField field : fieldValueStorage) {
			if (jsObject.has(field.getName())) { field.setValueFromJsObject(jsObject); }
			else { Logger.warning("Source does not have field [" + field.getName() + "] for class {" + getClass().getSimpleName() + "}"); }
		}
	}

	public Cursor getDefaultCursor() {
		return  model instanceof Paragraph ? Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR) :
				Cursor.getDefaultCursor();
	}

	// field getters

	private List<AbstractModelField> fieldValueStorage = new ArrayList<>();

	public Int addField(String fieldName, Integer fieldValue) {
		Int field = new Int(fieldName, fieldValue);
		fieldValueStorage.add(field);
		return field;
	}
	public Bool addField(String fieldName, Boolean fieldValue) {
		Bool field = new Bool(fieldName, fieldValue);
		fieldValueStorage.add(field);
		return field;
	}
}
