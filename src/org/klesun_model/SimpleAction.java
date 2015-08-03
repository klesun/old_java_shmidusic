package org.klesun_model;

import org.sheet_midusic.stuff.tools.Logger;

import java.util.concurrent.Callable;

public class SimpleAction {

	private Boolean isDone = false;

	public Callable<Boolean> redo = () -> true;
	public Runnable undo = () -> {};

	public SimpleAction() {}

	public SimpleAction setRedo(Runnable lambda) {
		this.redo = () -> { lambda.run(); return true; };
		return this;
	}

	public SimpleAction setUndo(Runnable lambda) {
		this.undo = lambda;
		return this;
	}

	public Boolean redo() {
		Boolean result = false;
		try { result = redo.call(); } catch (Exception exc) { Logger.fatal(exc, "Achtung, exception! SimpleAction::redo()"); }
		if (result) {
			return isDone = true;
		} else {
			return false;
		}
	}
}
