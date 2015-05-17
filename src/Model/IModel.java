package Model;

import org.json.JSONException;
import org.json.JSONObject;

public interface IModel {
	IModel getFocusedChild();
	IModel getModelParent();
	AbstractHandler getHandler();

	void getJsonRepresentation(JSONObject dict);
	IModel reconstructFromJson(JSONObject jsObject) throws JSONException;
}
