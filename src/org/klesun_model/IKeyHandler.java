package org.klesun_model;

// the IKeyHandler always comes in bound with Model's IComponent the idea of IKeyHandler is
// such that the Model root Handler's handleKey() is called when we got the key
// event from GUI; IKeyHandler checks the deepest focused child, whether it listens
// for this key, if not - it's parent, if it's neither - it's parent, and so on, til
// we get to the root we started from, and, if the root does not listen neither - we ignore event

// we handle event ONLY ONCE - in the deepest focused child (+ root) we found

import org.shmidusic.Main;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;

public interface IKeyHandler
{
	final static int ctrl = KeyEvent.CTRL_MASK;
	final static KeyEvent k = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants

	default Explain handleKey(Combo combo)
	{
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

	// we use this to (de)generate [File,Menu,Edit]-like menu that would
	// (with few decorative exclusions) completely correspond whole model key mapping.
	// the sad thing is, due to Java not supporting abstract class methods, we generate
	// every IModel field structure on the fly upon instance creation - so we call this from fake instances
	LinkedHashMap<Combo, ContextAction> getMyClassActionMap();

	IComponent getContext();
}
