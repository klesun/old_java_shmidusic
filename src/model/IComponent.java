package model;

import blockspace.Block;
import blockspace.BlockSpace;
import blockspace.Image.ImagePanel;
import blockspace.article.Article;
import blockspace.article.Paragraph;
import blockspace.staff.Staff;
import blockspace.staff.StaffPanel;
import blockspace.staff.accord.Chord;
import blockspace.staff.accord.nota.Nota;

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
		while (!(context instanceof Component) && context != null) { // circular import? yes...
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
		if (this.getClass() == BlockSpace.class) {
			return Arrays.asList(new Block(new ImagePanel((BlockSpace)this), (BlockSpace)this));
		} else if (this.getClass() == Block.class) {
			Block scroll = (Block)this;
			return Arrays.asList(new StaffPanel(scroll.getModelParent()), new Article(scroll.getModelParent()), new ImagePanel(scroll.getModelParent()));
		} else if (this.getClass() == Article.class) {
			return Arrays.asList(new Paragraph((Article)this));
		} else if (this.getClass() == StaffPanel.class) {
			return Arrays.asList(new Staff((StaffPanel)this));
		} else if (this.getClass() == Staff.class) {
			return Arrays.asList(new Chord((Staff)this));
		} else if (this.getClass() == Chord.class) {
			return Arrays.asList(new Nota((Chord)this));
		} else {
			return new ArrayList<>();
		}
	}
}
