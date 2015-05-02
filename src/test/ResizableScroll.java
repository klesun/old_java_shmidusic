package test;
import Model.Panels.SheetPanel;
import Model.Panels.Storyspace;
import OverridingDefaultClasses.Scroll;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class ResizableScroll extends Scroll {

	private boolean drag = false;
	private Point dragLocation  = new Point();

	public ResizableScroll(Component content) {
		super(content);

		setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
		setPreferredSize(new Dimension(200, 200));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				content.requestFocus();
				// TODO: window.setFocusedIndex(indexOf(this))

				drag = true;
				dragLocation = e.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				drag = false;
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (drag) {
					int dx = (int) (e.getPoint().getX() - dragLocation.getX());
					int dy = (int) (e.getPoint().getY() - dragLocation.getY());

					if (SwingUtilities.isLeftMouseButton(e)) {
						if (dragLocation.getX() > getWidth() - 10 && dragLocation.getY() > getHeight() - 10) {
							setSize(getWidth() + dx, getHeight() + dy);
							validate();
							dragLocation = e.getPoint();
						} else {
							setLocation(getX() + dx, getY() + dy);
						}
					} else if (getParent() instanceof Storyspace) {
						((Storyspace) getParent()).moveCam(dx, dy);
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (e.getX() > getWidth() - 10 && e.getY() > getHeight() - 10) {
					setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
				} else {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});

		this.setLocation(200, 150);
		this.setSize(300, 300);
	}

//	public void paintComponent(Graphics g) {
//		g.setColor(Color.red);
//		g.fillRect(0, 0, getWidth(), getHeight());
//	}

}