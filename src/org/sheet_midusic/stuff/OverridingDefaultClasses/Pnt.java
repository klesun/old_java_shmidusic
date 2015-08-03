package org.sheet_midusic.stuff.OverridingDefaultClasses;

import java.awt.*;

public class Pnt
{
	final public double x;
	final public double y;

	public Pnt(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	public Point legacy() {
		return new Point((int)x, (int)y);
	}

	public Pnt plus(Point p) {
		return new Pnt(x + p.x, y + p.y);
	}

	public Pnt plus(int x, int y) {
		return this.plus(new Point(x, y));
	}
}
