package Model;

import Main.MajesticWindow;
import Storyspace.Music.MusicPanel;
import Storyspace.Storyspace;
import Storyspace.Music.Staff.Staff;
import Storyspace.Music.Staff.StaffConfig.StaffConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class ActionFactory {

	final private static HashMap<String, Object> emptyHashMap = new HashMap<>();

	public Function<Combo, Map<String, Object>> doLambda = (combo) -> { return null; };
	public BiFunction<Combo, Map<String, Object>, Boolean> undoLambda = (combo, paramsForUndo) -> { return false; };

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

	public ActionFactory setDoHuj(Consumer<StaffConfig> lambda) {
		doLambda = (e) -> { lambda.accept(new StaffConfig(new Staff(new MusicPanel(new Storyspace(new MajesticWindow()))))); return emptyHashMap; };
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

	public Action createAction() {
		return new Action(this);
	}
}
