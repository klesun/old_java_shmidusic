package Model;

import java.awt.*;
import java.awt.event.FocusListener;

public interface IComponentModel extends IModel {

	IComponentModel getFocusedChild();
	AbstractHandler getHandler();

	default Component getFirstAwtParent() {
		IModel context = this;
		while (!(context instanceof Component) && context != null) { // circular import? yes...
			context = context.getModelParent();
		}
		return (Component)context;
	}

	// <editor-fold desc="these methods will be always overridden by Component">

	void setCursor(Cursor cursor);
	void requestFocus();
//	Component getParent();
	void addFocusListener(FocusListener focusListener);

	// </editor-fold>

}
