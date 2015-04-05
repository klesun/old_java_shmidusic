package Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActionFactory {

	public Function<Combo, Map<String, Object>> doLambda = (combo) -> { return null; };
	public BiFunction<Combo, Map<String, Object>, Boolean> undoLambda = (combo, paramsForUndo) -> { return false; };
	public Consumer<Combo> doAfterDo = (nothing) -> {};

	public Combo combo = null;

	public ActionFactory(Combo combo) {
		this.combo = combo;
	}
	public ActionFactory addTo(Map<Combo, ActionFactory> map) {
		map.put(this.combo, this);
		return this;
	}

	public ActionFactory setDo(Function<Combo, Boolean> lambda) {
		doLambda = (combo) -> {
			if (lambda.apply(combo)) {
				return new HashMap<>();
			} else {
				return null;
			}
		};
		return this;
	}

	public ActionFactory setDo2(Function<Combo, Map<String, Object>> lambda) {
		doLambda = lambda;
		return this;
	}

	public ActionFactory setDo(Consumer<Combo> lambda) {
		doLambda = (e) -> { lambda.accept(e); return new HashMap<>(); };
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
		this.undoLambda = (combo, action) -> this.doLambda.apply(combo) != null ? true : false;
		return this;
	}

	public ActionFactory setUndoChangeSign() {
		this.undoLambda = (combo, action) -> this.doLambda.apply(combo.changeSign()) != null ? true : false;
		return this;
	}

	public Action createAction() {
		return new Action(this);
	}
}
