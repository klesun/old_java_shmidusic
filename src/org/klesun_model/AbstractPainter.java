package org.klesun_model;

// this will be helper class for MidianaComponent-s

import org.sheet_midusic.stuff.OverridingDefaultClasses.TriConsumer;
import org.sheet_midusic.stuff.graphics.Constants;
import org.sheet_midusic.stuff.graphics.ShapeProvider;
import org.klesun_model.field.Field;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.stuff.OverridingDefaultClasses.Pnt;
import org.sheet_midusic.stuff.OverridingDefaultClasses.Straight;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

// even though it's abstract, his inheritors does not override nothing, they just use it's methods.
// TODO: transform it into helper instead of abstract-hujact ?
abstract public class AbstractPainter { // like Picasso!

	final protected MidianaComponent context;
	final private Graphics2D g;
	final private int x, y;

	public AbstractPainter(MidianaComponent context, Graphics2D g, int x, int y) {
		this.context = context;
		this.g = g;
		this.x = x;
		this.y = y;
	}

	final protected void fillRect(Rectangle r, Color c) {
		performWithColor(c, () -> g.fillRect(this.x + r.x, this.y + r.y, r.width, r.height));
	}
	final protected void drawRect(Rectangle r, Color c) {
		performWithColor(c, () -> g.drawRect(this.x + r.x, this.y + r.y, r.width, r.height));
	}
	final protected void drawDot(Pnt p, double r, Color c) {
		performWithColor(c, () -> g.fill(new Ellipse2D.Double(this.x + p.x, this.y + p.y, r * 2, r * 2)));
	}

	final protected void drawLine(Straight line) {
		drawLine(line.p1, line.p2);
	}
	final protected void drawLine(Pnt p1, Pnt p2) {
		drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	final protected void drawLine(double x0, double y0, double x1, double y1) {
		drawLine(x0, y0, x1, y1, Color.BLACK);
	}
	final protected void drawLine(double x0, double y0, double x1, double y1, Color color) {
		performWithColor(color, () -> g.draw(new Line2D.Double(x + x0, y + y0, x + x1, y + y1)));
	}

	final protected void drawString(String str, int x0, int y0, Color c) {
		performWithColor(c, () -> g.drawString(str, x + x0, y + y0));
	}
	final protected void drawString(String str, Rectangle rect, Color c) {
		Rectangle absoluteRect = new Rectangle(rect);
		absoluteRect.translate(x,y);
		performWithColor(c, () -> fitTextIn(absoluteRect, str, g));
	}

	final protected void drawModel(TriConsumer<Graphics2D, Integer, Integer> paintLambda, int x0, int y0) {
		paintLambda.accept(g, x + x0, y + y0);
	}
	final protected void drawImage(Image image, int x0, int y0) {
		g.drawImage(image, x + x0, y + y0, null);
	}

	/** draws parabola, that would fit into the rectangle */
	public void drawParabola(Rectangle r) {
		r.x += this.x;
		r.y += this.y;

		Color tmpColor = g.getColor();
		Stroke tmpStroke = g.getStroke();
		g.setColor(Color.MAGENTA);
		g.setStroke(new BasicStroke(2));

		double sharpness = r.getHeight() / Math.pow(r.getWidth() / 2.0, 2);
		int baseX = r.x + r.width / 2;
		int baseY = r.y + r.height;

		int lastX = - r.width / 2;
		int lastY = (int)(lastX * lastX * sharpness);

		for (int x0 = - r.width / 2 + 1; x0 < r.width / 2; ++x0) {
			int y0 = (int)(x0 * x0 * sharpness);
			g.drawLine(baseX + lastX, baseY - lastY, baseX + x0, baseY - y0);

			lastX = x0;
			lastY = y0;
		}

		g.setColor(tmpColor);
		g.setStroke(tmpStroke);
	}

	protected void drawFields() {
		java.util.List<Field> drawableList = context.getModel().getModelHelper().getFieldStorage().stream()
				.filter(f -> f.hasPaintingLambda()).collect(Collectors.toList());

		int w = dx() * 2;
		int dy = dy(); // TODO: it should be TOTAL_SPACE_FOR_THEM / THEIR_COUNT one day

		for (int i = 0; i < drawableList.size(); ++i) {
			Rectangle r = new Rectangle(x, y + dy, w, dy);
			if (drawableList.get(i).changedSinceLastRepaint || true) {
				drawableList.get(i).repaint(g, r);
			}
		}
	}

	final protected ShapeProvider getShapeProvider() {
		return new ShapeProvider(context.getSettings(), g, context.getImageStorage());
	}

	final protected BiConsumer<Double, Double> relative(BiConsumer<Double, Double> paintLambda) {
		return (x, y) -> paintLambda.accept(x + this.x, y + this.y);
	}

	private void performWithColor(Color c, Runnable lambda) {
		Color tmp = g.getColor();
		g.setColor(c);
		lambda.run();
		g.setColor(tmp);
	}

	protected static void fitTextIn(Rectangle rect, String text, Graphics g) {

		Function<Integer, Double> toInches = pixels -> pixels * 1.25;
		Function<Double, Double> toPixels = inches -> inches * 0.8;

		double fontSize = toInches.apply(rect.height);

		Font font = Constants.PROJECT_FONT.deriveFont((float)fontSize);
		int width = g.getFontMetrics(font).stringWidth(text);
		fontSize = Math.min(fontSize * rect.width / width, fontSize);

		Font wasFont = g.getFont();
		g.setFont(Constants.PROJECT_FONT.deriveFont((float) fontSize));
		g.drawString(text, rect.x, rect.y + toPixels.apply(fontSize).intValue());
		g.setFont(wasFont);
	}

	final protected int dx() { return context.getSettings().getStepWidth(); }
	final protected int dy() { return context.getSettings().getStepHeight(); }
}
