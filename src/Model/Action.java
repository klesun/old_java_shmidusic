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

	public Boolean doDo() {
		Map<String, Object> result = factory.doLambda.apply(factory.combo);
		if (result != null) {
			paramsForUndo = result;
			if (!undone) { // TODO: ctrl-y
				AbstractHandler.destroyRedoHistory();
			}
			done = true;
			undone = false;
		}
		return result != null;
	}

	public Boolean unDo() {
		done = false;
		undone = true;
		return factory.undoLambda.apply(factory.combo, this.paramsForUndo);
	}

	// getters

	public Map<String, Object> getParamsForUndo() {
		return paramsForUndo;
	}
}
