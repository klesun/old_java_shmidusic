package blockspace.staff;

import gui.ImageStorage;
import gui.Settings;
import model.AbstractHandler;
import model.AbstractModel;
import model.IComponentModel;

import java.awt.*;
import java.awt.event.FocusListener;

abstract public class MidianaComponent extends AbstractModel implements IComponentModel {

	private AbstractHandler eventHandler = null;

	abstract public MidianaComponent getFocusedChild();
	abstract protected AbstractHandler makeHandler();
	abstract  public void drawOn(Graphics surface, int x, int y, Boolean completeRepaint); // TODO: renmae to paintComponent() for compatibility with AWT components

	public MidianaComponent(IComponentModel parent) {
		super(parent);
		this.eventHandler = this.makeHandler();
	}

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

	public StaffPanel getPanel() {
		return (StaffPanel)this.getFirstAwtParent();
	}

	final public Settings getSettings() {
		return getPanel().getSettings();
	}
	final public ImageStorage getImageStorage() { return getPanel().getScroll().getModelParent().getImageStorage(); }
	final public int dx() { return getSettings().getStepWidth(); }
	final public int dy() { return getSettings().getStepHeight(); }

}
