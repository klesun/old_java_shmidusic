package test;
import Gui.SheetPanel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class ResizableScrollPane extends JScrollPane {

	private boolean drag = false;
	private Point dragLocation  = new Point();

	public ResizableScrollPane(SheetPanel content) {
		super(content);
		Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3);
		setBorder(border);
		setPreferredSize(new Dimension(200, 200));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				content.parentWindow.sheetPanel.setFocusedIndex(content.focusedIndex); // человек очень-очень сложно мыслит
				// TODO: window.setFocusedIndex(indexOf(this))
				drag = true;
				dragLocation = e.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				drag = false;
			}
		});
		JScrollPane huj = this;
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (drag) {
					int dx = (int)(e.getPoint().getX() - dragLocation.getX());
					int dy = (int)(e.getPoint().getY() - dragLocation.getY());
					if (dragLocation.getX() > getWidth() - 10 && dragLocation.getY() > getHeight() - 10) {
						setSize(getWidth() + dx, getHeight() + dy);
						validate();
						dragLocation = e.getPoint();
					} else { setLocation(getX() + dx, getY() + dy);	}
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
	}

//	public void paintComponent(Graphics g) {
//		g.setColor(Color.red);
//		g.fillRect(0, 0, getWidth(), getHeight());
//	}

}