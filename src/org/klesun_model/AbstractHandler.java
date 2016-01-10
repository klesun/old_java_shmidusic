package org.klesun_model;

import org.json.JSONObject;
import org.shmidusic.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

abstract public class AbstractHandler implements KeyListener
{
	// constants
	final public static int ctrl = KeyEvent.CTRL_MASK;
	final public static KeyEvent k = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants

	private IComponent context = null;

	// mouse
	protected Point mouseLocation = new Point(0,0);

	public AbstractHandler(IComponent context) {
		this.context = context;
	}

	abstract public LinkedHashMap<Combo, ContextAction> getMyClassActionMap();

	// implemented methods
	final public void keyPressed(KeyEvent e)
	{
		Explain result = this.handleKey(new Combo(e));
		if (!result.isSuccess() && !result.isImplicit()) {
			JOptionPane.showMessageDialog(getContext().getFirstAwtParent(), result.getExplanation());
		}
	}
	final public void keyTyped(KeyEvent e) {}
	final public void keyReleased(KeyEvent e) {}

	private Queue<JSONObject> undoQueue = new LinkedList<>();

	final public Explain handleKey(Combo combo) {
		Explain result = null;

		if (getContext().getFocusedChild() != null) {
			result = getContext().getFocusedChild().getHandler().handleKey(combo);
		}

		if ((result == null || !result.isSuccess()) && getMyClassActionMap().containsKey(combo)) {

			result = getMyClassActionMap().get(combo).redo(getContext());
			Main.window.updateMenuBar();
		}

		return result != null ? result : new Explain(false, "No Action For This Combination").setImplicit(true);
	}

	public IComponent getContext() {
		return this.context;
	}
}
