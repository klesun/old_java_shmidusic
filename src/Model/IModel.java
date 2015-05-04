package Model;

import org.json.JSONException;
import org.json.JSONObject;

public interface IModel {
	IModel getFocusedChild();
	IModel getModelParent();
	AbstractHandler getHandler();

	JSONObject getJsonRepresentation();
	IModel reconstructFromJson(JSONObject jsObject) throws JSONException;
}
