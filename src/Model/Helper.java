package Model;

import Stuff.Tools.Logger;
import org.json.JSONObject;

public class Helper {

	public static JSONObject getJsonRepresentation(IModel model) {
		JSONObject dict = new JSONObject();
		model.getJsonRepresentation(dict);
		return dict;
	}

	public static Object getDefaultValue(Class cls) {
		return  cls == Boolean.class ? false :
				cls == Integer.class ? 0 :
				Logger.fatal("Unknown Class [" + cls.getSimpleName() + "] does not have default value");
	}
}
