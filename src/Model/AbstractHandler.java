package Model;

import Model.Panels.SheetPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

abstract public class AbstractHandler implements KeyListener {

	private IHandlerContext context = null;
	protected LinkedHashMap<Combo, ActionFactory> actionMap = new LinkedHashMap<>();
	protected static LinkedList<Action> handledEventQueue = new LinkedList<>(); // for ctrl-z
	protected static LinkedList<Action> dehandledEventQueue = new LinkedList<>(); // for ctrl-y

	public AbstractHandler(IHandlerContext context) {
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
		if (getSheetPanel() != null) { getSheetPanel().checkCam(); } // говно!!!
		return result;
	}

	final protected ActionFactory addCombo(int keyMods, int keyCode) {
		return new ActionFactory(new Combo(keyMods, keyCode)).addTo(this.actionMap);
	}

	final public Map<Combo, ActionFactory> getActionMap() {
		return actionMap;
	}

	public static void destroyRedoHistory() {
		while (dehandledEventQueue.poll() != null);
	}

	// TODO: it's so ugly...
	// ни в коем блядь случае не вызывай для не AbstractModel!!!
	private SheetPanel getSheetPanel() {
		IHandlerContext context = getContext();
		while (!(context instanceof SheetPanel) && context != null) { // circular import? yes...
			context = context.getModelParent();
		}
		return (SheetPanel)context;
	}

	public IHandlerContext getContext() {
		return this.context;
	}

	public void keyTyped(KeyEvent e) {}
	final public void keyPressed(KeyEvent e) { this.handleKey(new Combo(e)); }
	public void keyReleased(KeyEvent e) {}

	// constants

	final public static int ctrl = KeyEvent.CTRL_MASK;
	final public static KeyEvent k = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants
}
