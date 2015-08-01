package gui;

// i'm tired of being attached to pixels of image files
// let's draw Nota-s and signs with vectors!

import model.AbstractPainter;
import stuff.OverridingDefaultClasses.Straight;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.function.Consumer;

public class ShapeProvider
{
	final private Settings settings;
	final private Graphics2D g;
	final private ImageStorage imageStorage;

	public ShapeProvider(Settings settings, Graphics2D g, ImageStorage imageStorage)
	{
		this.settings = settings;
		this.g = g;
		this.imageStorage = imageStorage;
	}

	/** @params x, y - center of desired sign to be drawn */
	public void drawSharpSign(double x, double y)
	{
		double dx = settings.getStepWidth();
		double dy = settings.getStepHeight();

		Consumer<Straight> line = s -> g.draw(new Line2D.Double(x + s.p1.x, y + s.p1.y, x + s.p2.x, y + s.p2.y));

		g.setStroke(new BasicStroke((float)dy / 5));

		// "||"
		double x0 = - dx / 12, x1 = + dx / 12;
		double y0 = - 2 * dy, y1 = + 2 * dy;
		line.accept(new Straight(x0, y0, x0, y1));
		line.accept(new Straight(x1, y0, x1, y1));

		g.setStroke(new BasicStroke((float)dy * 2 / 5)); // dy == 5 -> stroke == 2

		// "//"
		x0 = - dx / 8; x1 = + dx / 8;
		y0 = - dy; y1 = - dy * 3 / 2; // i made it asymetric intentionally
		line.accept(new Straight(x0, y0, x1, y1));
		line.accept(new Straight(x0, y0 + 2 * dy, x1, y1 + 2 * dy));

		g.setStroke(new BasicStroke((float)dy / 5));
	}

	/** @params x, y - center of desired sign to be drawn */
	public void drawBecarSign(double x, double y)
	{
		double dx = settings.getStepWidth();
		double dy = settings.getStepHeight();

		Consumer<Straight> line = s -> g.draw(new Line2D.Double(x + s.p1.x, y + s.p1.y, x + s.p2.x, y + s.p2.y));

		g.setStroke(new BasicStroke((float)dy / 5));

		// "||"
		double x0 = - dx / 8, x1 = + dx / 8;
		double y0 = + dy + dy / 2, y1 = - dy - dy / 2;
		line.accept(new Straight(x0, - 3 * dy, x0, y0));
		line.accept(new Straight(x1, + 3 * dy, x1, y1));

		g.setStroke(new BasicStroke((float)dy * 2 / 5)); // dy == 5 -> stroke == 2

		// "//"
		line.accept(new Straight(x0, y0, x1, + dy));
		line.accept(new Straight(x1, y1, x0, - dy));

		g.setStroke(new BasicStroke((float)dy / 5));
	}

	/** @params x, y - center of desired sign to be drawn */
	public void drawFlatSign(double x, double y)
	{
		double dy = settings.getStepHeight();
		double dx = settings.getStepWidth();

		// image height = 5 * dy()
		// image width = dx() / 2
		g.drawImage(imageStorage.getFlatImage(), (int) (x - dx / 4), (int)(y - 4 * dy) + 1, null); // 4 * dy - shifting so we worked from center of flat sign
	}
}
