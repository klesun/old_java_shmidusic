package blockspace.staff.accord.nota;

import blockspace.staff.MidianaComponent;
import blockspace.staff.StaffConfig.KeySignature;
import blockspace.staff.StaffConfig.StaffConfig;
import gui.ImageStorage;
import gui.ShapeProvider;
import model.AbstractPainter;
import stuff.OverridingDefaultClasses.Pnt;
import stuff.OverridingDefaultClasses.Straight;
import stuff.tools.Fp;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

public class NotaPainter extends AbstractPainter
{
	public NotaPainter(Nota context, Graphics2D g, int x, int y) {
		super(context, g, x, y);
	}

	@Override
	public void draw(Boolean completeRepaint)
	{
		Nota n = (Nota)context;

		double notaCenterY = 7 * dy();

		KeySignature siga = n.getParentAccord().getParentStaff().getConfig().getSignature();
		if (!siga.myTuneQueue().contains(n.tune.get() % 12)) {
			if (n.isEbony()) {
				// draw bemol cuz i decided to draw bemols in comment from KeySignature class
				BiConsumer<Double, Double> paintEbony = getShapeProvider()::drawFlatSign;
				relative(paintEbony).accept(dx() * 3 / 4.0, notaCenterY);
			} else {
				// draw becar
				BiConsumer<Double, Double> paintEbony = getShapeProvider()::drawBecarSign;
				relative(paintEbony).accept(dx() * 3 / 4.0, notaCenterY);
			}
		}

		int colorChannel = n.getIsMuted() || n.isPause() ? 9 : n.getChannel();
		BufferedImage tmpImg;
		if (n.isTooShort()) {
			tmpImg = n.getImageStorage().getTooShortImage();
		} else if (n.isTooLong()) {
			tmpImg = n.getImageStorage().getTooLongImage();
		} else {
			tmpImg = n.getImageStorage().getNotaImg(n.getCleanLength(), colorChannel);
		}

		if (n.getIsLinkedToNext()) {
			drawParabola(new Rectangle(dx() * 3 / 2, (int) notaCenterY, dx() * 2, dy() * 2));
		}

		drawImage(tmpImg, n.getNotaImgRelX(), 0);

		if (n.isTriplet.get()) {
			Rectangle rect = new Rectangle(dx() /4, 6 * dy(), dx() / 2, dy() * 2);
			drawString("3", rect, ImageStorage.getColorByChannel(n.channel.get()));
		}

		for (int i = 0; i < n.getDotCount(); ++i) {
			int x = dx() * 5/3 + dx() * i / n.getDotCount();
			drawDot(new Pnt(x, notaCenterY), dy() / 2, Color.BLACK);
		}

		if (n.ivoryIndex() % 2 == 1) {
			drawLine(dx() * 2/3, notaCenterY, dx() * 2, notaCenterY);
		}
	}
}
