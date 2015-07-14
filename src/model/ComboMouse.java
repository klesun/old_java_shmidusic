package model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ComboMouse {
	
	private Point point = null;
	
	public int dx;
	public int dy;

	public Boolean leftButton;
	public Boolean rightButton;

	private IModel origin;

	public ComboMouse(MouseEvent e, Point vectorSource) {
		dx = (int) (e.getPoint().getX() - vectorSource.getX());
		dy = (int) (e.getPoint().getY() - vectorSource.getY());
		
		point = e.getPoint();
		leftButton = SwingUtilities.isLeftMouseButton(e);
		rightButton = SwingUtilities.isRightMouseButton(e);
	}
	
	public ComboMouse(MouseEvent e) { this(e, e.getPoint()); }

	public Point getPoint() { return this.point; }
	public IModel getOrigin() { return origin; }
	public ComboMouse setOrigin(IModel value) { origin = value; return this; }
	
}
