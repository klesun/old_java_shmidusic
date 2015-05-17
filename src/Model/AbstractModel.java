
package Model;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import Gui.Settings;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractModel implements IModel {

	private IModel parent = null;
	private AbstractHandler eventHandler = null;

	public AbstractModel(IModel parent) { // TODO: parent should be AbstractModel
		this.parent = parent;
		this.eventHandler = this.makeHandler();
	}

	final public JSONObject getJsonRepresentation() {
		return Helper.getJsonRepresentation(this);
	}
	abstract public IModel reconstructFromJson(JSONObject jsObject) throws JSONException;
	abstract public AbstractModel getFocusedChild();

	abstract protected AbstractHandler makeHandler();

	final public AbstractHandler getHandler() { return this.eventHandler; }

	public abstract void drawOn(Graphics surface, int x, int y);

	// field getters
	
	public IModel getModelParent() { return this.parent; }

	// from static context

	final public int dx() { return Settings.getStepWidth(); }
	final public int dy() { return Settings.getStepHeight(); }
	final protected static int limit(int value, int min, int max) { return Math.min(Math.max(value, min), max); }
}
