package org.shmidusic.sheet_music.staff.chord.note;

import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.sheet_music.staff.staff_config.KeySignature;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.klesun_model.PaintHelper;
import org.shmidusic.stuff.OverridingDefaultClasses.Pnt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

public class NotePainter
{
	final private PaintHelper h;
	final private NoteComponent context;

	public NotePainter(NoteComponent context, Graphics2D g, int x, int y) {
		this.context = context;
		this.h = new PaintHelper(g,x,y);
	}

	public void draw(KeySignature siga)
	{
		NoteComponent comp = context;
		Note n = comp.note;

		double noteCenterY = 7 * h.dy();

		if (!siga.myTuneQueue().contains(n.tune.get() % 12)) {
			if (n.isEbony()) {
				// draw flat cuz i decided to draw flat in comment from KeySignature class
				BiConsumer<Double, Double> paintEbony = h.getShapeProvider()::drawFlatSign;
				h.relative(paintEbony).accept(h.dx() * 3 / 4.0, noteCenterY);
			} else {
				// draw straight
				BiConsumer<Double, Double> paintBecar = h.getShapeProvider()::drawBecarSign;
				h.relative(paintBecar).accept(h.dx() * 3 / 4.0, noteCenterY);
			}
		}

		int colorChannel = n.getIsMuted() || n.isPause() ? 9 : n.getChannel();
		BufferedImage tmpImg;
		if (n.isTooShort()) {
			tmpImg = ImageStorage.inst().getTooShortImage();
		} else if (n.isTooLong()) {
			tmpImg = ImageStorage.inst().getTooLongImage();
		} else {
			tmpImg = ImageStorage.inst().getNoteImg(n.getCleanLength(), colorChannel);
		}

		if (n.getIsLinkedToNext()) {
			h.drawParabola(new Rectangle(h.dx() * 3 / 2, (int) noteCenterY, h.dx() * 2, h.dy() * 2));
		}

		h.drawImage(tmpImg, h.dx(), 0);

		if (n.isTriplet()) {
			Rectangle rect = new Rectangle(h.dx() /4, 6 * h.dy(), h.dx() / 2, h.dy() * 2);
			h.drawString("3", rect, ImageStorage.getColorByChannel(n.channel.get()));
		}

		for (int i = 0; i < n.getDotCount(); ++i) {
			double r = h.dy() / 2;
			int x = (int)(h.dx() * 5/3 + r * 4 * i / n.getDotCount());
			h.drawDot(new Pnt(x, noteCenterY), r, Color.BLACK);
		}

		if (n.ivoryIndex(siga) % 2 == 1) {
			h.drawLine(h.dx() * 2/3, noteCenterY, h.dx() * 2, noteCenterY);
		}
	}
}
