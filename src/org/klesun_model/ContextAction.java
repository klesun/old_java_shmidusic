package org.klesun_model;

import java.util.function.Consumer;
import java.util.function.Function;

public class ContextAction<C extends IComponent> {

	private Boolean isDone = false;
	private String caption = null;
	private Boolean omitMenuBar = false;
	private String postfix = "";

	private Function<C, Explain> redo;
	private Runnable undo = null;

	public ContextAction() {}

	public ContextAction<C> setRedo(Function<C, Explain> lambda) {
		this.redo = lambda;
		return this;
	}

	public ContextAction<C> setRedo(Consumer<C> lambda) {
		return setRedo(context -> {
			lambda.accept(context);
			return new Explain(true);
		});
	}

	public ContextAction<C> setUndo(Runnable lambda) {
		this.undo = lambda;
		return this;
	}

	public ContextAction<C> setOmitMenuBar(Boolean value) {
		this.omitMenuBar = value;
		return this;
	}

	public Boolean omitMenuBar() {
		return this.omitMenuBar;
	}

	public ContextAction<C> setPostfix(String postfix) {
		this.postfix = postfix;
		return this;
	}

	// shortcuts will be grouped by postfix
	public String getPostfix() {
		return this.postfix;
	}

	public ContextAction<C> setCaption(String caption) {
		this.caption = caption;
		return this;
	}

	public String getCaption() {
		return this.caption;
	}

	public Explain redo(C context) {
		return this.redo.apply(context);
	}

	public Function<C, Explain> getRedo() {
		return this.redo;
	}
}
