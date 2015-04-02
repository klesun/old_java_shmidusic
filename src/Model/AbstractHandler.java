package Model;

import Gui.SheetPanel;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

abstract public class AbstractHandler {

	private AbstractModel context = null;
	protected LinkedHashMap<Combo, Action> actionMap = new LinkedHashMap<>();
	protected LinkedList<Combo> handledEventQueue = new LinkedList<>(); // for ctrl-z
	protected LinkedList<Combo> dehandledEventQueue = new LinkedList<>(); // for ctrl-y

	public AbstractHandler(AbstractModel context) {
		this.context = context;
		this.init();
		appendEventToQueueOnCall();

		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Z), new Action().setDo((event) -> {
			Combo lastEvent;
			while ((lastEvent = handledEventQueue.pollLast()) != null) {
				Action action = actionMap.get(lastEvent);
				if (action.unDo(lastEvent, action.getParamsForUndo())) {
					dehandledEventQueue.add(lastEvent);
					return true;
				}
			}
			return false;
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Y), new Action().setDo((event) -> {
			Combo lastEvent;
			while ((lastEvent = dehandledEventQueue.pollLast()) != null) {
				if (actionMap.get(lastEvent).doDo(lastEvent)) {
					// it adds to handledEventQueue automatically
					return true;
				}
			}
			return false;
		}));
	}

	private void appendEventToQueueOnCall() {
		for (Map.Entry<Combo, Action> e: actionMap.entrySet()) {
			e.getValue().doAfterDo((v) -> {
				this.handledEventQueue.add(e.getKey());
			});
		}
	}

	abstract protected void init();

	final public Boolean handleKey(Combo combo) {
		Boolean result;
		if (getContext().getFocusedChild() != null &&
			getContext().getFocusedChild().gettHandler().handleKey(combo)) {
			result = true;
		} else {
			result = getActionMap().containsKey(combo) &&
					getActionMap().get(combo).doDo(combo);
		}
		if (result) {
			getSheetPanel().parentWindow.keyHandler.requestNewSurface();
			if (!combo.isUndoOrRedo()) {
				destroyRedoHistory();
			}
		}
		return result;
	}

	private void destroyRedoHistory() {
		while (this.dehandledEventQueue.poll() != null);
	}

	private SheetPanel getSheetPanel() {
		IModel context = getContext();
		while (!(context instanceof SheetPanel)) { // why don't i like this?
			context = ((AbstractModel)context).getParent();
		}
		return (SheetPanel)context;
	}

	final public Map<Combo, Action> getActionMap() {
		// i hope, it clones only links to lambdas, not lambdas as well
//		Map<Combo, Action> combinedHandle = (LinkedHashMap)actionMap.clone();
//		if (getContext().getFocusedChild() != null) {
//			Map<Combo, Action> handleKey = getContext().getFocusedChild().gettHandler().getActionMap();
//			combinedHandle.putAll(handleKey);
//		}
//		return combinedHandle;
		return actionMap;
	}

	public AbstractModel getContext() {
		return this.context;
	}
}
