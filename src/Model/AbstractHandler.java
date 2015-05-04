package Model;

import Model.Containers.Panels.MusicPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

abstract public class AbstractHandler implements KeyListener, MouseListener, MouseMotionListener {

	private IModel context = null;
	protected LinkedHashMap<Combo, ActionFactory> actionMap = new LinkedHashMap<>();
	protected static LinkedList<Action> handledEventQueue = new LinkedList<>(); // for ctrl-z
	protected static LinkedList<Action> unhandledEventQueue = new LinkedList<>(); // for ctrl-y

	// mouse
	protected Point mouseLocation = new Point(0,0);

	public AbstractHandler(IModel context) {
		this.context = context;
		this.initActionMap();

		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Z)).addTo(actionMap).setDo((event) -> {
			Action lastAction;
			while ((lastAction = handledEventQueue.pollLast()) != null) {
				if (lastAction.unDo()) {
					unhandledEventQueue.add(lastAction);
					return true;
				}
			}
			return false;
		});
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Y)).addTo(actionMap).setDo((event) -> {
			Action lastAction;
			while ((lastAction = unhandledEventQueue.pollLast()) != null) {
				if (lastAction.doDo()) {
					// it adds to handledEventQueue automatically
					return true;
				}
			}
			return false;
		});
	}

	// override me, please!
	protected void initActionMap() {}
	// implemented methods
	final public void keyPressed(KeyEvent e) { this.handleKey(new Combo(e)); }
	final public void keyTyped(KeyEvent e) {}
	final public void keyReleased(KeyEvent e) {}

	final public Boolean handleKey(Combo combo) {
		Boolean result = false;
		if (getContext().getFocusedChild() != null &&
			getContext().getFocusedChild().getHandler().handleKey(combo)) {
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

	final public static void destroyRedoHistory() {
		while (unhandledEventQueue.poll() != null);
	}

	// TODO: it's so ugly...
	private MusicPanel getSheetPanel() {
		IModel context = getContext();
		while (!(context instanceof MusicPanel) && context != null) { // circular import? yes...
			context = context.getModelParent();
		}
		return (MusicPanel)context;
	}

	public IModel getContext() {
		return this.context;
	}

	// constants

	final public static int ctrl = KeyEvent.CTRL_MASK;
	final public static KeyEvent k = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants

	//---------
	// mouse
	//--------

	// override us, please!
	public Boolean mouseDraggedFinal(ComboMouse combo) { return false; } // TODO: remove "final" from name somehow, it's ugly, they are almost abstract
	public Boolean mousePressedFinal(ComboMouse combo) { return false; }
	public Boolean mouseMovedFinal(ComboMouse combo) { return false; }

	final public Boolean mousePressed(ComboMouse combo) {
		if (mousePressedFinal(combo)) {
			this.mouseLocation = combo.getPoint();
			return true;
		} else {
			int x = Component.class.cast(getContext()).getX();
			int y = Component.class.cast(getContext()).getY();
			combo.getPoint().move(x, y);
			return (getContext().getModelParent() != null && getContext().getModelParent().getHandler().mousePressed(combo));
		}
	}

	final public Boolean mouseDragged(ComboMouse combo) {
		if (mouseDraggedFinal(combo)) {
			this.mouseLocation = combo.getPoint();
			return true;
		} else {
			return (getContext().getModelParent() != null && getContext().getModelParent().getHandler().mouseDragged(combo));
		}
	}

	final public Boolean mouseMoved(ComboMouse combo) {
		// TODO: maybe do it as in mouseDragged() if got issues
		Boolean result = mouseMovedFinal(combo) ||
			(getContext().getModelParent() != null && getContext().getModelParent().getHandler().mouseMoved(combo));
		this.mouseLocation = combo.getPoint();
		return result;
	}

	// implementing mouse listeners
	final public void mousePressed(MouseEvent e) { mousePressed(new ComboMouse(e)); }
	final public void mouseDragged(MouseEvent e) { mouseDragged(new ComboMouse(e, mouseLocation)); }
	final public void mouseMoved(MouseEvent e) { mouseMoved(new ComboMouse(e, mouseLocation)); }
	// useless for now - so i put final
	final public void mouseReleased(MouseEvent e) {}
	final public void mouseEntered(MouseEvent e) {}
	final public void mouseExited(MouseEvent e) {}
	final public void mouseClicked(MouseEvent e) {}
}
