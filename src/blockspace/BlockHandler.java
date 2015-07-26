package blockspace;

import model.*;
import stuff.OverridingDefaultClasses.TruMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class BlockHandler extends AbstractHandler {

	final private static int MIN_WIDTH = 50;
	final private static int MIN_HEIGHT = 50;

	private static TruMap<Combo, ContextAction<Block>> actionMap = new TruMap<>();
	static {
		actionMap
			.p(new Combo(ctrl, k.VK_F), mkAction(Block::switchFullscreen).setCaption("Switch Fullscreen"))
			.p(new Combo(ctrl, k.VK_F2), mkAction(c -> c.setTitle(JOptionPane.showInputDialog(c, "Type new name for container: ", c.getTitle()))).setCaption("Rename"))
			.p(new Combo(ctrl, k.VK_DELETE), mkAction(c -> c.getModelParent().removeModelChild(c)).setCaption("Delete"))
			;
	}

	public BlockHandler(Block context) {
		super(context);
		context.addKeyListener(this);
		context.addMouseListener(this);
		context.addMouseMotionListener(this);

		// design

		context.content.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) { context.gotFocus(); }
			public void focusLost(FocusEvent e) { context.lostFocus(); }
		});

		context.getModelParent().addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (context.isFullscreen()) { context.fitToScreen(); }
			}
		});
	}

	@Override
	public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
		return actionMap;
	}

	private static ContextAction<Block> mkAction(Consumer<Block> lambda) {
		ContextAction<Block> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	@Override
	public Boolean mouseDraggedFinal(ComboMouse mouse) {

		if (getContext().isFullscreen()) {
			return true;
		} else {
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
			} else {
				return false;
			}
		}
	}

	@Override
	public Boolean mousePressedFinal(ComboMouse combo) { return combo.leftButton || combo.rightButton || getContext().isFullscreen(); }

	@Override
	public Boolean mouseMovedFinal(ComboMouse combo) {
		Point point = combo.getPoint();

		if (getContext().isFullscreen()) {
			return true;
		} else {
			if (point.getX() > getContext().getWidth() - 10 && point.getY() > getContext().getHeight() - 10) {
				getContext().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
				return true;
			} else {
				getContext().setCursor(Cursor.getDefaultCursor());
				return false;
			}
		}
	}

	@Override
	public Block getContext() { return Block.class.cast(super.getContext()); }
}
