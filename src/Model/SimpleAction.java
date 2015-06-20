package Model;

public class SimpleAction {

	private Boolean isDone = false;

	public Runnable redo = () -> {};
	public Runnable undo = () -> {};

	public SimpleAction() {}

	public SimpleAction setRedo(Runnable lambda) {
		this.redo = lambda;
		return this;
	}

	public SimpleAction setUndo(Runnable lambda) {
		this.undo = lambda;
		return this;
	}

	public void redo() {
		redo.run();
		isDone = true;
	}
}
