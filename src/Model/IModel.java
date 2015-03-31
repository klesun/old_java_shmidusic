
package Model;

import java.util.LinkedHashMap;
import org.json.JSONException;
import org.json.JSONObject;

public interface IModel {
	public JSONObject getJsonRepresentation(); // TODO: return JSONObject instead of HashMap // your wish is granted
	public IModel reconstructFromJson(JSONObject jsObject) throws JSONException;
}
