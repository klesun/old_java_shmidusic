package org.shmidusic.sheet_music.staff;

import org.shmidusic.MainPanel;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.shmidusic.stuff.graphics.Settings;
import org.klesun_model.AbstractHandler;
import org.klesun_model.IComponent;

import java.awt.*;
import java.awt.event.FocusListener;

@Deprecated
abstract public class MidianaComponent implements IComponent
{

	final private IComponent parent;
	final private AbstractHandler eventHandler;

	abstract public MidianaComponent getFocusedChild();
	abstract protected AbstractHandler makeHandler();
	/** @return int - position of bottomest drawn pixel */

	// TODO: separate Model from Event handler, i wanna be able to instantiate Nota without Staff!
	public MidianaComponent(IComponent parent) {
		this.parent = parent;
		this.eventHandler = this.makeHandler();
	}

	// TODO: rename to getComponentParent()
	public IComponent getModelParent() { return this.parent; }
	public AbstractHandler getHandler() { return this.eventHandler; }

	@Override
	final public void setCursor(Cursor cursor) {
		// TODO: make it correct one day, if you need this (it could be the first step to invoke mouse into midiana, like changing cursor when hovering notas...)
		getFirstAwtParent().setCursor(cursor);
	}
	@Override
	final public void requestFocus() {
		// TODO: soon. something like parent.setFocusedIndex(indexOf(this)); parent.requestFocus();
	}
	@Override
	final public void addFocusListener(FocusListener focusListener) {
		// TODO: soon. something like setFocusedIndex(indexO(this)) { old.focusListener.handleLostFocus(); blablabla; focusListener.handleGainedFocus(); }
	}

	public MainPanel getPanel() {
		return (MainPanel)this.getFirstAwtParent();
	}

	final public Settings getSettings() {
		return Settings.inst();
	}
	final public ImageStorage getImageStorage() { return ImageStorage.inst(); }
	final public int dx() { return getSettings().getStepWidth(); }
	final public int dy() { return getSettings().getStepHeight(); }

}
