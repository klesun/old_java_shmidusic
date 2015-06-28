package Model;

import java.util.function.Consumer;
import java.util.function.Function;

public class ContextAction<C extends IComponentModel> {


	private Boolean isDone = false;
	private String name = "Anonymous";
	private String description = "No description";

	private Function<C, ActionResult> redo;
	private Runnable undo = null;

	public ContextAction() {}

	public ContextAction<C> setRedo(Function<C, ActionResult> lambda) {
		this.redo = lambda;
		return this;
	}

	public ContextAction<C> setRedo(Consumer<C> lambda) {
		return setRedo(context -> {
			lambda.accept(context);
			return new ActionResult(true);
		});
	}

	public ContextAction<C> setUndo(Runnable lambda) {
		this.undo = lambda;
		return this;
	}

	public ActionResult redo(C context) {
		return this.redo.apply(context);
	}

	public Function<C, ActionResult> getRedo() {
		return this.redo;
	}
}
