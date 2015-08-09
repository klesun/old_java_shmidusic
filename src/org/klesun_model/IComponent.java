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

	// retarded language
	default java.util.List<IComponent> makeFakePossibleChildListForClassMethods() {
		/*if (this.getClass() == BlockSpace.class) {
			return Arrays.asList(new Block(new ImagePanel((BlockSpace)this), (BlockSpace)this));
		} else if (this.getClass() == Block.class) {
			Block scroll = (Block)this;
			return Arrays.asList(new MainPanel(scroll.getModelParent()), new Article(scroll.getModelParent()), new ImagePanel(scroll.getModelParent()));
		} else if (this.getClass() == Article.class) {
			return Arrays.asList(new Paragraph((Article)this));
		} else */if (this.getClass() == MainPanel.class) {
			return Arrays.asList(new SheetMusicPanel((MainPanel)this));
		} else if (this.getClass() == SheetMusicPanel.class) {
			return Arrays.asList(new StaffComponent(new Staff((MainPanel)this.getModelParent())));
		} else if (this.getClass() == StaffComponent.class) {
			return Arrays.asList(new Chord((StaffComponent)this));
		} else if (this.getClass() == Chord.class) {
			return Arrays.asList(new Nota((Chord)this));
		} else {
			return new ArrayList<>();
		}
	}
}
