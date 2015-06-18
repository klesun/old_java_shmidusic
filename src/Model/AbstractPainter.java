package Model;

// this will be helper class for MidianaComponent-s

import Gui.Settings;
import Storyspace.Staff.MidianaComponent;
import Stuff.OverridingDefaultClasses.Pnt;

import java.awt.*;

abstract public class AbstractPainter {

	protected MidianaComponent context;
	private Graphics g;
	private int x, y;

	public AbstractPainter(MidianaComponent context, Graphics g, int x, int y) {
		this.context = context;
		this.g = g;
		this.x = x;
		this.y = y;
	}

	abstract public void draw();

	final protected void drawDot(Point p, int r, Color c) {
		performWithColor(c, () -> g.fillOval(this.x + p.x, this.y + p.y, r * 2, r * 2));
	}
	final protected void drawLine(Straight line) { drawLine(line.p1, line.p2); }
	final protected void drawLine(Point p1, Point p2) { drawLine(p1.x, p1.y, p2.x, p2.y); }
	final protected void drawLine(int x0, int y0, int x1, int y1) { g.drawLine(x + x0, y + y0, x + x1, y + y1); }
	final protected void drawString(String str, int x0, int y0, Color c) {
		performWithColor(c, () -> g.drawString(str, x + x0, y + y0));
	}
	final protected void drawModel(MidianaComponent model, int x0, int y0) {
		model.drawOn(g, x + x0, y + y0);
	}

	private void performWithColor(Color c, Runnable lambda) {
		Color tmp = g.getColor();
		g.setColor(c);
		lambda.run();
		g.setColor(tmp);
	}

	final protected int dx() { return Settings.getStepWidth(); }
	final protected int dy() { return Settings.getStepHeight(); }

	final protected class Straight {
		final public Pnt p1;
		final public Pnt p2;

		public Straight(Pnt p1, Pnt p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		public Straight plus(Point vector) {
			return new Straight(p1.plus(vector), p2.plus(vector));
		}
	}
}
