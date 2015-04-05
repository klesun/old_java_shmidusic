package Model;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActionFactory {

	private Function<Combo, Boolean> doLambda = (event) -> { return false; };
	private BiFunction<Combo, Map<String, Object>, Boolean> undoLambda = (event, action) -> { return false; };
	private Consumer<Combo> doAfterDo = (nothing) -> {};

	private LinkedList<Map<String, Object>> paramsForUndoQueue = new LinkedList<>();

	public int done = 0;

	public ActionFactory setDo(Function<Combo, Boolean> lambda) {
		doLambda = lambda;
		return this;
	}

	public ActionFactory setDo2(Function<Combo, Map<String, Object>> lambda) {
		doLambda = (e) -> {
			Map<String, Object> map = lambda.apply(e);
			this.paramsForUndoQueue.add(map);

			return map != null ? true : false;
		};
		return this;
	}

	public ActionFactory setDo(Consumer<Combo> lambda) {
		doLambda = (e) -> { lambda.accept(e); return true; };
		return this;
	}

	public ActionFactory setUndo(Function<Combo, Boolean> lambda) {
		undoLambda = (combo, action) -> { return lambda.apply(combo); };
		return this;
	}

	public ActionFactory setUndo(BiFunction<Combo, Map<String, Object>, Boolean> lambda) {
		undoLambda = lambda;
		return this;
	}

	public ActionFactory setUndo(Consumer<Combo> lambda) {
		undoLambda = (e, action) -> { lambda.accept(e); return true; };
		return this;
	}

	public ActionFactory setUndo(BiConsumer<Combo, Map<String, Object>> lambda) {
		undoLambda = (combo, paramsForUndo) -> { lambda.accept(combo, paramsForUndo); return true; };
		return this;
	}

	public ActionFactory doAfterDo(Consumer appendLambda) {
		this.doAfterDo = appendLambda;
		return this;
	}

	public ActionFactory biDirectional() {
		this.undoLambda = (combo, action) -> this.doLambda.apply(combo);
		return this;
	}

	public ActionFactory setUndoChangeSign() {
		this.undoLambda = (combo, action) -> {
			return this.doLambda.apply(combo.changeSign());
		};
		return this;
	}

	public Boolean doDo(Combo combo) {
		Boolean result = doLambda.apply(combo);
		if (result) {
			this.doAfterDo.accept(null);
		}
		++done;
		return result;
	}

	public Boolean unDo(Combo combo, Map<String, Object> paramsForUndo) {
		--done;
		return undoLambda.apply(combo, paramsForUndo);
	}

	// getters

	public Map<String, Object> getParamsForUndo() {
		return this.paramsForUndoQueue.pollLast();
	}
}
