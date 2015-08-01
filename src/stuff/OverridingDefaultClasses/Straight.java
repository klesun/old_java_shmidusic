package stuff.OverridingDefaultClasses;

// Even it's called straight, it actually is what we russians call "отрезок"
// Just line expressed through two points

import java.awt.*;

public class Straight {
	final public Pnt p1;
	final public Pnt p2;

	public Straight(Pnt p1, Pnt p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	public Straight(double x1, double y1, double x2, double y2) {
		this(new Pnt(x1, y1), new Pnt(x2, y2));
	}

	public Straight plus(Point vector) {
		return new Straight(p1.plus(vector), p2.plus(vector));
	}

	@Override
	public String toString() {
		return "Straight: [" + p1 + ", " + p2 + "]";
	}
}
