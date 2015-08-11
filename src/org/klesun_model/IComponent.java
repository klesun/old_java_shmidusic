package org.klesun_model;

import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.staff_panel.MainPanel;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.staff.chord.nota.Nota;
import org.sheet_midusic.staff.staff_panel.SheetMusicPanel;
import org.sheet_midusic.staff.staff_panel.StaffComponent;

import java.awt.*;
import java.awt.event.FocusListener;
import java.util.*;

// TODO: rename to just IComponent
public interface IComponent {

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
}
