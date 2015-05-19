package Model;

import java.awt.*;
import java.awt.event.FocusListener;

public interface IComponentModel extends IModel {

	Helper getModelHelper();

	// <editor-fold desc="these methods will be always overridden by Component">

	void setCursor(Cursor cursor);
	void requestFocus();
//	Component getParent();
	void addFocusListener(FocusListener focusListener);

	// </editor-fold>

}
