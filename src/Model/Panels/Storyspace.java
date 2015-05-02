package Model.Panels;

import Model.AbstractHandler;
import Model.AbstractModel;
import Model.Combo;
import Model.IHandlerContext;
import test.ResizableScroll;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class Storyspace extends JPanel implements IHandlerContext {

	private Point dragLocation = null;
	private Window window = null;

	public Storyspace(Window window) {
		this.window = window;
		setLayout(null);
		setFocusable(true);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocus();
				dragLocation = e.getPoint();
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int dx = (int) (e.getPoint().getX() - dragLocation.getX());
				int dy = (int) (e.getPoint().getY() - dragLocation.getY());
				moveCam(dx, dy);
				dragLocation = e.getPoint();
			}
		});

		addKeyListener(new AbstractHandler(this) {
			@Override
			protected void init() {
				addCombo(ctrl, k.VK_M).setDo(((Storyspace) this.getContext())::addMusicBlock);
				addCombo(ctrl, k.VK_T).setDo(((Storyspace) this.getContext())::addTextBlock);
				addCombo(ctrl, k.VK_I).setDo(((Storyspace) this.getContext())::addImageBlock);
			}
		});

		this.setBackground(Color.DARK_GRAY);
	}

	// event handles

	public void moveCam(int dx, int dy) {
		Arrays.asList(getComponents()).stream().forEach(component
			-> component.setLocation(component.getX() + dx, component.getY() + dy));
	}

	public void addMusicBlock(Combo combo) {
		this.add(new ResizableScroll(new SheetPanel(window)));
		this.validate();
	}

	public void addTextBlock(Combo combo) {
		JTextArea textarea = new JTextArea();
		textarea.setLineWrap(true);
		this.add(new ResizableScroll(textarea));
		this.validate();
	}

	public void addImageBlock(Combo combo) {
		this.add(new ResizableScroll(new ImagePanel()));
		this.validate();
	}

	@Override
	public AbstractModel getFocusedChild() { return null; }

	@Override
	public IHandlerContext getModelParent() {
		return null;
	}
}
