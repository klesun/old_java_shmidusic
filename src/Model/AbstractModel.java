
package Model;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Gui.Settings;
import Model.Field.AbstractModelField;
import Model.Field.Bool;
import Model.Field.Int;
import Model.Field.Str;
import Stuff.Tools.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractModel implements IModel {

	private IModel parent = null;
	private AbstractHandler eventHandler = null;

	public AbstractModel(IModel parent) { // TODO: parent should be AbstractModel
		this.parent = parent;
		this.eventHandler = this.makeHandler();
	}

	// override me please, if you got special fields (like Lists or Objects)
	public AbstractModel reconstructFromJson(JSONObject jsObject) throws JSONException {
		for (AbstractModelField field: fieldValueStorage) {
			if (jsObject.has(field.getName())) { field.setValueFromJsObject(jsObject); }
			else { Logger.warning("Source does not have field [" + field.getName() + "] for class {" + getClass().getSimpleName() + "}"); }
		}

		return this;
	}

	// override me please, if you got special fields (like Lists or Objects)
	public void getJsonRepresentation(JSONObject dict) {
		for (AbstractModelField field: fieldValueStorage) {
			dict.put(field.getName(), field.getValue());
		}
	}

	final public JSONObject getJsonRepresentation() { return Helper.getJsonRepresentation(this); }


	abstract public AbstractModel getFocusedChild();
	abstract protected AbstractHandler makeHandler();

	final public AbstractHandler getHandler() { return this.eventHandler; }

	public abstract void drawOn(Graphics surface, int x, int y);

	// field getters

	protected List<AbstractModelField> fieldValueStorage = new ArrayList<>();

	protected Int addField(String fieldName, Integer fieldValue) {
		Int field = new Int(fieldName, fieldValue);
		fieldValueStorage.add(field);
		return field;
	}
	protected Bool addField(String fieldName, Boolean fieldValue) {
		Bool field = new Bool(fieldName, fieldValue);
		fieldValueStorage.add(field);
		return field;
	}
	protected Str addField(String fieldName, String fieldValue) {
		Str field = new Str(fieldName, fieldValue);
		fieldValueStorage.add(field);
		return field;
	}

	public IModel getModelParent() { return this.parent; }

	// from static context

	final public int dx() { return Settings.getStepWidth(); }
	final public int dy() { return Settings.getStepHeight(); }
	final protected static int limit(int value, int min, int max) { return Math.min(Math.max(value, min), max); }
}
