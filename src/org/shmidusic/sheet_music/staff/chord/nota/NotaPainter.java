package org.shmidusic.sheet_music.staff.chord.nota;

import org.shmidusic.sheet_music.staff.staff_config.KeySignature;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.klesun_model.AbstractPainter;
import org.shmidusic.stuff.OverridingDefaultClasses.Pnt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

public class NotaPainter extends AbstractPainter
{
	public NotaPainter(NoteComponent context, Graphics2D g, int x, int y) {
		super(context, g, x, y);
	}

	public void draw(KeySignature siga)
	{
		NoteComponent comp = (NoteComponent)context;
		Nota n = comp.note;

		double notaCenterY = 7 * dy();

		if (!siga.myTuneQueue().contains(n.tune.get() % 12)) {
			if (n.isEbony()) {
				// draw bemol cuz i decided to draw bemols in comment from KeySignature class
				BiConsumer<Double, Double> paintEbony = getShapeProvider()::drawFlatSign;
				relative(paintEbony).accept(dx() * 3 / 4.0, notaCenterY);
			} else {
				// draw becar
				BiConsumer<Double, Double> paintBecar = getShapeProvider()::drawBecarSign;
				relative(paintBecar).accept(dx() * 3 / 4.0, notaCenterY);
			}
		}

		int colorChannel = n.getIsMuted() || n.isPause() ? 9 : n.getChannel();
		BufferedImage tmpImg;
		if (n.isTooShort()) {
			tmpImg = ImageStorage.inst().getTooShortImage();
		} else if (n.isTooLong()) {
			tmpImg = ImageStorage.inst().getTooLongImage();
		} else {
			tmpImg = ImageStorage.inst().getNotaImg(n.getCleanLength(), colorChannel);
		}

		if (n.getIsLinkedToNext()) {
			drawParabola(new Rectangle(dx() * 3 / 2, (int) notaCenterY, dx() * 2, dy() * 2));
		}

		drawImage(tmpImg, dx(), 0);

		if (n.isTriplet.get()) {
			Rectangle rect = new Rectangle(dx() /4, 6 * dy(), dx() / 2, dy() * 2);
			drawString("3", rect, ImageStorage.getColorByChannel(n.channel.get()));
		}

		for (int i = 0; i < n.getDotCount(); ++i) {
			// TODO: for some reason it draws only one dot even for multidot hujot
			int x = dx() * 5/3 + dx() * i / n.getDotCount();
			drawDot(new Pnt(x, notaCenterY), dy() / 2, Color.BLACK);
		}

		if (n.ivoryIndex(siga) % 2 == 1) {
			drawLine(dx() * 2/3, notaCenterY, dx() * 2, notaCenterY);
		}
	}
}
