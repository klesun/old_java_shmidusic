package Storyspace.Staff;

import Gui.ImageStorage;
import Gui.Settings;
import Model.AbstractHandler;
import Model.AbstractModel;
import Model.IComponentModel;
import Model.IModel;

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
		getFirstPanelParent().setCursor(cursor);
	}
	@Override
	final public void requestFocus() {
		// TODO: soon. something like parent.setFocusedIndex(indexOf(this)); parent.requestFocus();
	}
	@Override
	final public void addFocusListener(FocusListener focusListener) {
		// TODO: soon. something like setFocusedIndex(indexO(this)) { old.focusListener.handleLostFocus(); blablabla; focusListener.handleGainedFocus(); }
	}

	public StaffPanel getFirstPanelParent() {
		IModel context = this;
		while (!(context instanceof StaffPanel) && context != null) { // circular import? yes...
			context = context.getModelParent();
		}
		return (StaffPanel)context;
	}

	final public Settings getSettings() {
		return getFirstPanelParent().getSettings();
	}
	final public ImageStorage getImageStorage() { return getFirstPanelParent().getScroll().getModelParent().getImageStorage(); }
	final public int dx() { return getSettings().getStepWidth(); }
	final public int dy() { return getSettings().getStepHeight(); }

}
