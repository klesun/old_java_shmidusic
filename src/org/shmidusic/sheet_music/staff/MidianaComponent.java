package org.shmidusic.sheet_music.staff;

import org.shmidusic.MainPanel;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.shmidusic.stuff.graphics.Settings;
import org.klesun_model.AbstractHandler;
import org.klesun_model.IComponent;

import java.awt.*;
import java.awt.event.FocusListener;

@Deprecated // sorry, forgot the reason
abstract public class MidianaComponent implements IComponent
{

	final private IComponent parent;
	final private AbstractHandler eventHandler;

	abstract public MidianaComponent getFocusedChild();
	abstract protected AbstractHandler makeHandler();
	/** @return int - position of bottomest drawn pixel */

	// TODO: separate Model from Event handler, i wanna be able to instantiate Note without Staff!
	public MidianaComponent(IComponent parent) {
		this.parent = parent;
		this.eventHandler = this.makeHandler();
	}

	// TODO: rename to getComponentParent()
	public IComponent getModelParent() { return this.parent; }
	public AbstractHandler getHandler() { return this.eventHandler; }

	final public Settings getSettings() {
		return Settings.inst();
	}
	final public ImageStorage getImageStorage() { return ImageStorage.inst(); }
	final public int dx() { return getSettings().getStepWidth(); }
	final public int dy() { return getSettings().getStepHeight(); }

}
