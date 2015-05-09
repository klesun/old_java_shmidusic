package Model.Containers;

import Model.AbstractHandler;
import Model.ComboMouse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

public class ResizableScrollHandler extends AbstractHandler {

	final private static int MIN_WIDTH = 10;
	final private static int MIN_HEIGHT = 10;

	public ResizableScrollHandler(ResizableScroll context) {
		super(context);
		context.addKeyListener(this);
		context.addMouseListener(this);
		context.addMouseMotionListener(this);

		// design

		context.content.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) { context.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3)); }
			@Override
			public void focusLost(FocusEvent e) { context.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3)); }
		});
	}

	@Override
	protected void initActionMap() {}

	@Override
	public Boolean mouseDraggedFinal(ComboMouse mouse) {
		if (mouse.leftButton || mouse.rightButton) {
			if (mouseLocation.getX() > getContext().getWidth() - 10 && mouseLocation.getY() > getContext().getHeight() - 10) {
				// resize panel
				getContext().setSize(Math.max(getContext().getWidth() + mouse.dx, MIN_WIDTH), Math.max(getContext().getHeight() + mouse.dy, MIN_HEIGHT));
				getContext().validate();
			} else {
				// move panel
				getContext().setLocation(getContext().getX() + mouse.dx, getContext().getY() + mouse.dy);
			}
			return true;
		} else { return false; }
	}

	@Override
	public Boolean mouseMovedFinal(ComboMouse combo) {
		Point point = combo.getPoint();
		if (point.getX() > getContext().getWidth() - 10 && point.getY() > getContext().getHeight() - 10) {
			getContext().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			return true;
		} else {
			getContext().setCursor(Cursor.getDefaultCursor());
			return false;
		}
	}

	@Override
	public ResizableScroll getContext() { return ResizableScroll.class.cast(super.getContext()); }
}
