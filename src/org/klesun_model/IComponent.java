package org.klesun_model;

import org.shmidusic.stuff.graphics.Settings;

import java.awt.*;
import java.awt.event.FocusListener;

public interface IComponent
{
	IModel getModel();
	@Deprecated // bad name and should not be needed (i'd like to watch into only one side)
	IComponent getModelParent();
	IComponent getFocusedChild();
	AbstractHandler getHandler();

	default Component getFirstAwtParent() {
		IComponent context = this;
		while (!(context instanceof Component) && context != null) {
			context = context.getModelParent();
		}
		return (Component)context;
	}

	// <editor-fold desc="these methods will be always overridden by Component">

	default Cursor getDefaultCursor() {
		return Cursor.getDefaultCursor();
	}

	void setCursor(Cursor cursor);
	void requestFocus();
//	Component getParent();
	void addFocusListener(FocusListener focusListener);

	// </editor-fold>

	default int dx() {
		return Settings.inst().getStepWidth();
	}
	default int dy() {
		return Settings.inst().getStepHeight();
	}
}
