package Model;

import Gui.SheetPanel;
import Model.Staff.Staff;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

abstract public class AbstractHandler {

	private AbstractModel context = null;
	protected LinkedHashMap<Combo, ActionFactory> actionMap = new LinkedHashMap<>();
	protected static LinkedList<Action> handledEventQueue = new LinkedList<>(); // for ctrl-z
	protected static LinkedList<Action> dehandledEventQueue = new LinkedList<>(); // for ctrl-y

	public AbstractHandler(AbstractModel context) {
		this.context = context;
		this.init();

		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Z)).addTo(actionMap).setDo((event) -> {
			Action lastAction;
			while ((lastAction = handledEventQueue.pollLast()) != null) {
				if (lastAction.unDo()) {
					dehandledEventQueue.add(lastAction);
					return true;
				}
			}
			return false;
		});
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Y)).addTo(actionMap).setDo((event) -> {
			Action lastAction;
			while ((lastAction = dehandledEventQueue.pollLast()) != null) {
				if (lastAction.doDo()) {
					// it adds to handledEventQueue automatically
					return true;
				}
			}
			return false;
		});
	}

	abstract protected void init();

	final public Boolean handleKey(Combo combo) {
		Boolean result = false;
		if (getContext().getFocusedChild() != null &&
			getContext().getFocusedChild().gettHandler().handleKey(combo)) {
			result = true;
		} else {
			if (getActionMap().containsKey(combo)) {
				Action action = getActionMap().get(combo).createAction();
				if (action.doDo()) {
					this.handledEventQueue.add(action);
					result = true;
				}
			}
		}
		getSheetPanel().checkCam();
		return result;
	}

	public static void destroyRedoHistory() {
		while (dehandledEventQueue.poll() != null);
	}

	private SheetPanel getSheetPanel() {
		IModel context = getContext();
		while (!(context instanceof Staff)) { // circular import? yes...
			context = ((AbstractModel)context).getParent();
		}
		return ((Staff)context).getParentSheet();
	}

	final public Map<Combo, ActionFactory> getActionMap() {
		return actionMap;
	}

	public AbstractModel getContext() {
		return this.context;
	}
}
