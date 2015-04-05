package Model;

import java.util.HashMap;
import java.util.LinkedList;
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
		Boolean result = factory.doLambda.apply(factory.combo) != null ? true : false;
		if (result) {
			if (!undone) { // TODO: ctrl-y
				AbstractHandler.destroyRedoHistory();
			}
			done = true;
			undone = false;
			factory.doAfterDo.accept(null);
		}
		return result;
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
