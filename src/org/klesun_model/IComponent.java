package org.klesun_model;

import org.sheet_midusic.stuff.graphics.Settings;

import java.awt.*;
import java.awt.event.FocusListener;

public interface IComponent
{
	IModel getModel();
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
