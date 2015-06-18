package Stuff.OverridingDefaultClasses;

import java.awt.*;

public class Pnt extends Point {

	public Pnt(int x, int y) {
		super(x, y);
	}

	public Pnt plus(Point p) {
		Pnt newPoint = new Pnt(this.x, this.y);
		newPoint.translate(p.x, p.y);
		return newPoint;
	}

	public Pnt plus(int x, int y) {
		return this.plus(new Point(x, y));
	}
}
