package Storyspace;

import Model.AbstractHandler;
import Model.ComboMouse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StoryspaceScrollHandler extends AbstractHandler {

	final private static int MIN_WIDTH = 50;
	final private static int MIN_HEIGHT = 50;

	public StoryspaceScrollHandler(StoryspaceScroll context) {
		super(context);
		context.addKeyListener(this);
		context.addMouseListener(this);
		context.addMouseMotionListener(this);

		// design

		context.content.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) { context.gotFocus(); }
			public void focusLost(FocusEvent e) { context.lostFocus(); }
		});

		context.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				context.getVerticalScrollBar().setCursor(Cursor.getDefaultCursor());
			}
		});

		// removing stupid built-ins
		InputMap im = context.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke("UP"), "none");
		im.put(KeyStroke.getKeyStroke("DOWN"), "none");
	}

	@Override
	protected void initActionMap() {
		addCombo(ctrl, k.VK_DELETE).setDo(c -> { getContext().getModelParent().removeModelChild(getContext().content); });
		addCombo(0, k.VK_F2).setDo(c -> {
			getContext().setTitle(JOptionPane.showInputDialog(getContext(), "Type new name for panel: ", getContext().getTitle()));
		});
	}

	@Override
	public Boolean mouseDraggedFinal(ComboMouse mouse) {
		if (mouse.leftButton || mouse.rightButton) {
			if (mouseLocation.getX() > getContext().getWidth() - 10 && mouseLocation.getY() > getContext().getHeight() - 10) {
				// resize panel
				getContext().setSize(Math.max(getContext().getWidth() + mouse.dx, MIN_WIDTH), Math.max(getContext().getHeight() + mouse.dy, MIN_HEIGHT));
				getContext().validate();
			} else {
				// move panel
				Point point = mouse.getPoint();
				getContext().setLocation(getContext().getX() + mouse.dx, getContext().getY() + mouse.dy);
				point.translate(-mouse.dx, -mouse.dy);
			}
			return true;
		} else { return false; }
	}

	@Override
	public Boolean mousePressedFinal(ComboMouse combo) { return combo.leftButton || combo.rightButton; }

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
	public StoryspaceScroll getContext() { return StoryspaceScroll.class.cast(super.getContext()); }
}
