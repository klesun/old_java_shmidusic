
package Tools;

import Gui.staff.pointerable.Accord;
import java.util.LinkedHashMap;
import org.json.JSONException;
import org.json.JSONObject;

public interface IModel {
	public LinkedHashMap<String, Object> getObjectState();
	public IModel setObjectStateFromJson(JSONObject jsObject) throws JSONException;
}
