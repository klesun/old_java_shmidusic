
package Model;

import java.util.LinkedHashMap;
import org.json.JSONException;
import org.json.JSONObject;

public interface IModel {
	public LinkedHashMap<String, Object> getJsonRepresentation(); // TODO: return JSONObject instead of HashMap
	public IModel reconstructFromJson(JSONObject jsObject) throws JSONException;
}
