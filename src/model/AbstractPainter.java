package model;

// this will be helper class for MidianaComponent-s

import model.field.Field;
import blockspace.staff.MidianaComponent;
import stuff.OverridingDefaultClasses.Pnt;

import java.awt.*;
import java.util.stream.Collectors;

abstract public class AbstractPainter { // like Picasso!

	final protected MidianaComponent context;
	final private Graphics g;
	final private int x, y;

	public AbstractPainter(MidianaComponent context, Graphics g, int x, int y) {
		this.context = context;
		this.g = g;
		this.x = x;
		this.y = y;
	}

	abstract public void draw(Boolean completeRepaint);

	final protected void fillRect(Rectangle r, Color c) {
		performWithColor(c, () -> g.fillRect(this.x + r.x, this.y + r.y, r.width, r.height));
	}
	final protected void drawRect(Rectangle r, Color c) {
		performWithColor(c, () -> g.drawRect(this.x + r.x, this.y + r.y, r.width, r.height));
	}
	final protected void drawDot(Point p, int r, Color c) {
		performWithColor(c, () -> g.fillOval(this.x + p.x, this.y + p.y, r * 2, r * 2));
	}

	final protected void drawLine(Straight line) {
		drawLine(line.p1, line.p2);
	}
	final protected void drawLine(Point p1, Point p2) {
		drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	final protected void drawLine(int x0, int y0, int x1, int y1) {
		drawLine(x + x0, y + y0, x + x1, y + y1, Color.BLACK);
	}
	final protected void drawLine(int x0, int y0, int x1, int y1, Color color) {
		performWithColor(color, () -> g.drawLine(x + x0, y + y0, x + x1, y + y1));
	}

	final protected void drawString(String str, int x0, int y0, Color c) {
		performWithColor(c, () -> g.drawString(str, x + x0, y + y0));
	}
	final protected void drawModel(MidianaComponent model, int x0, int y0, Boolean completeRepaint) {
		model.drawOn(g, x + x0, y + y0, completeRepaint);
	}
	final protected void drawImage(Image image, int x0, int y0) {
		g.drawImage(image, x + x0, y + y0, null);
	}

	protected void drawFields(Boolean completeRepaint) {
		java.util.List<Field> drawableList = context.getModelHelper().getFieldStorage().stream()
				.filter(f -> f.hasPaintingLambda()).collect(Collectors.toList());

		int w = dx() * 2;
		int dy = dy(); // TODO: it should be TOTAL_SPACE_FOR_THEM / THEIR_COUNT one day

		for (int i = 0; i < drawableList.size(); ++i) {
			Rectangle r = new Rectangle(x, y + dy, w, dy);
			if (drawableList.get(i).changedSinceLastRepaint || completeRepaint) {
				drawableList.get(i).repaint(g, r);
			}
		}
	}

	private void performWithColor(Color c, Runnable lambda) {
		Color tmp = g.getColor();
		g.setColor(c);
		lambda.run();
		g.setColor(tmp);
	}

	final protected int dx() { return context.getSettings().getStepWidth(); }
	final protected int dy() { return context.getSettings().getStepHeight(); }

	public static class Straight {
		final public Pnt p1;
		final public Pnt p2;

		public Straight(Pnt p1, Pnt p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		public Straight plus(Point vector) {
			return new Straight(p1.plus(vector), p2.plus(vector));
		}

		@Override
		public String toString() {
			return "Straight: [" + p1 + ", " + p2 + "]";
		}
	}
}
