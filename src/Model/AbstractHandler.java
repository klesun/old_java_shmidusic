package Model;

import Storyspace.Staff.MidianaComponent;
import Storyspace.Staff.StaffPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

abstract public class AbstractHandler implements KeyListener, MouseListener, MouseMotionListener {

	private IComponentModel context = null;
	protected LinkedHashMap<Combo, ActionFactory> actionMap = new LinkedHashMap<>();
	@Deprecated
	protected static LinkedList<Action> handledEventQueue = new LinkedList<>(); // for ctrl-z
	@Deprecated
	protected static LinkedList<Action> unhandledEventQueue = new LinkedList<>(); // for ctrl-y

	private LinkedList<SimpleAction> simpleActionQueue = new LinkedList<>();
	private int simpleActionIterator = 0;

	synchronized final public void performAction(SimpleAction action) {
		if (simpleActionIterator < simpleActionQueue.size()) {
			this.simpleActionQueue = new LinkedList<>(simpleActionQueue.subList(0, simpleActionIterator));
		}
		simpleActionQueue.addLast(action);
		action.redo();
		++simpleActionIterator;
	}

	// mouse
	protected Point mouseLocation = new Point(0,0);

	public AbstractHandler(IComponentModel context) {
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
	// override me, please!
	public LinkedHashMap<Combo, ContextAction> getStaticActionMap() {
		return new LinkedHashMap<>();
	}

	// implemented methods
	final public void keyPressed(KeyEvent e) {
		getRootHandler().handleKey(new Combo(e));
	}
	final public void keyTyped(KeyEvent e) {}
	final public void keyReleased(KeyEvent e) {}

	public AbstractHandler getRootHandler() {
		IComponentModel rootContext = getContext();
		while (rootContext.getModelParent() != null) {
			rootContext = rootContext.getModelParent();
		}

		return rootContext.getHandler();
	}

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
		if (getContext() instanceof MidianaComponent) { // i don't like this
			MidianaComponent.class.cast(getContext()).getFirstPanelParent().checkCam();
		}
		return result;
	}

	final protected ActionFactory addCombo(int keyMods, int keyCode) {
		return new ActionFactory(new Combo(keyMods, keyCode)).addTo(this.actionMap);
	}

	final protected void addNumberComboList(int keyMods, Consumer<Integer> lambda) {
		List<Integer> keyList = Combo.getNumberKeyList();
		for (int keyCode: keyList) {
			int number = new Combo(0, keyCode).getPressedNumber();
			addCombo(keyMods, keyCode).setDo(c -> { lambda.accept(number); });
		}
	}

	final public Map<Combo, ActionFactory> getActionMap() { return actionMap; }

	final public static void destroyRedoHistory() {
		while (unhandledEventQueue.poll() != null);
	}

	public IComponentModel getContext() {
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
	public Boolean mouseReleasedFinal(ComboMouse combo) { return false; }
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

	final public Boolean mouseReleased(ComboMouse combo) {
		if (mouseReleasedFinal(combo)) {
			this.mouseLocation = combo.getPoint();
			return true;
		} else {
			int x = Component.class.cast(getContext()).getX();
			int y = Component.class.cast(getContext()).getY();
			combo.getPoint().move(x, y);
			return (getContext().getModelParent() != null && getContext().getModelParent().getHandler().mouseReleased(combo));
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
	final public void mousePressed(MouseEvent e) { mousePressed(new ComboMouse(e).setOrigin(getContext())); }
	final public void mouseReleased(MouseEvent e) { mouseReleased(new ComboMouse(e).setOrigin(getContext())); }
	final public void mouseDragged(MouseEvent e) { mouseDragged(new ComboMouse(e, mouseLocation).setOrigin(getContext())); }
	final public void mouseMoved(MouseEvent e) { mouseMoved(new ComboMouse(e, mouseLocation).setOrigin(getContext())); }
	// useless for now - so i put final
	final public void mouseEntered(MouseEvent e) {}
	final public void mouseExited(MouseEvent e) {}
	final public void mouseClicked(MouseEvent e) {}
}
