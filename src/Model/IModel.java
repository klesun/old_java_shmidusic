package Model;

import org.json.JSONException;
import org.json.JSONObject;

public interface IModel {
	IComponentModel getModelParent();
	Helper getModelHelper();

	void getJsonRepresentation(JSONObject dict);
	IModel reconstructFromJson(JSONObject jsObject) throws JSONException;
}
