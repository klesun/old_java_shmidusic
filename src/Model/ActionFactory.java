package Model;

import Stuff.Tools.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.*;

public class ActionFactory {

	final private static HashMap<String, Object> emptyHashMap = new HashMap<>();

	public Function<Combo, Map<String, Object>> doLambda = (combo) -> null;
	public BiFunction<Combo, Map<String, Object>, Boolean> undoLambda = (combo, paramsForUndo) -> false;

	private Boolean omitMenuBar = false;

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

	// -_-
	public ActionFactory setDo2(Function<Combo, Map<String, Object>> lambda) {
		doLambda = lambda;
		return this;
	}

	public ActionFactory setDo(Consumer<Combo> lambda) {
		doLambda = (e) -> { lambda.accept(e); return emptyHashMap; };
		return this;
	}

	public ActionFactory setDo(Runnable lambda) {
		doLambda = (e) -> {
			lambda.run(); return emptyHashMap; };
		return this;
	}

	public ActionFactory setDo(Callable<Boolean> lambda) {
		doLambda = (e) -> {
			Boolean result = false;
			try { result = lambda.call(); }
			catch (Exception exc) { Logger.fatal(exc, "Sorry bro, nevhujebu. This lambda came from ActionFactory::setDo(Callable<Boolean>)"); }
			return result ? emptyHashMap : null;
		};
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

	public ActionFactory biDirectional() {
		this.undoLambda = (combo, action) -> this.doLambda.apply(combo) != null ? true : false;
		return this;
	}

	public ActionFactory setUndoChangeSign() {
		this.undoLambda = (combo, action) -> this.doLambda.apply(combo.changeSign()) != null ? true : false;
		return this;
	}

	public ActionFactory setOmitMenuBar(Boolean value) {
		this.omitMenuBar = true;
		return this;
	}

	public Boolean omitMenuBar() {
		return this.omitMenuBar;
	}

	public Action createAction() {
		return new Action(this);
	}
}
