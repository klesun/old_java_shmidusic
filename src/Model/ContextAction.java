package Model;

import java.util.function.Consumer;
import java.util.function.Function;

public class ContextAction<C extends IComponentModel> {


	private Boolean isDone = false;
	private String name = "Anonymous";
	private String description = "No description";

	private Function<C, Boolean> redo;
	private Runnable undo = null;

	public ContextAction() {}

	public ContextAction<C> setRedo(Function<C, Boolean> lambda) {
		this.redo = lambda;
		return this;
	}

	public ContextAction<C> setRedo(Consumer<C> lambda) {
		return setRedo(context -> {
			lambda.accept(context);
			return true;
		});
	}

	public ContextAction<C> setUndo(Runnable lambda) {
		this.undo = lambda;
		return this;
	}

	public Boolean redo(C context) {
		return this.redo.apply(context);
	}

	public Function<C, Boolean> getRedo() {
		return this.redo;
	}
}
