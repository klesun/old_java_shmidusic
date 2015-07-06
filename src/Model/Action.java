package Model;

import java.util.HashMap;
import java.util.Map;

public class Action {

	private Map<String, Object> paramsForUndo = new HashMap<>();

	private ActionFactory factory = null;
	Boolean done = false;
	Boolean undone = false;

	public Action(ActionFactory factory) {
		this.factory = factory;
	}
}
