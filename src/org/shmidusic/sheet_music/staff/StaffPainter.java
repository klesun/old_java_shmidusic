package org.shmidusic.sheet_music.staff;

import org.klesun_model.PaintHelper;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfigComponent;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.shmidusic.stuff.graphics.Settings;
import org.shmidusic.stuff.tools.INote;

import java.awt.*;

public class StaffPainter
{
	final StaffComponent context;
	final PaintHelper h;

	public StaffPainter(StaffComponent context, Graphics2D g, int x, int y) {
		this.context = context;
		int dx = Settings.inst().getStepWidth();
		this.h = new PaintHelper(g,
				x + context.staff.getMarginX() + 3 * dx, // 3dx - violin/bass keys and Config
				y + context.staff.getMarginY());
	}

	public void draw(Boolean completeRepaint)
	{
		StaffComponent comp = context;
		Staff s = comp.staff;

		Staff.TactMeasurer tactMeasurer = new Staff.TactMeasurer(s.getConfig().getTactSize());

		int i = 0;
		for (java.util.List<Chord> row : s.getAccordRowList(comp.getAccordInRowCount())) {

			int y = i * Staff.SISDISPLACE * h.dy(); // bottommest y note may be drawn on

			drawStaffLines(y);

			int j = 0;
			for (Chord chord : row) {
				int x = j * (2 * h.dx());

				if (tactMeasurer.inject(chord)) {
					drawTactLine(x + h.dx() * 2, y, tactMeasurer);
				}

				++j;
			}

			if (completeRepaint) {
				h.drawImage(ImageStorage.inst().getViolinKeyImage(), - 3 * h.dx(), y - 3 * h.dy());
				h.drawImage(ImageStorage.inst().getBassKeyImage(), - 3 * h.dx(), 11 * h.dy() + y);
			}

			++i;
		}

		StaffConfigComponent configComp = s.getConfig().makeComponent(comp);
		h.drawModel((g, xArg, yArg) -> configComp.drawOn(g, xArg, yArg), -h.dx(), 0);
	}

	private void drawTactLine(int x, int baseY, Staff.TactMeasurer tactMeasurer)
	{
		Color lineColor = tactMeasurer.sumFraction.equals(new Fraction(0))
			? Color.BLACK
			: Color.RED;

		h.drawLine(x, baseY - h.dy() * 5, x, baseY + h.dy() * 20, lineColor);
		Color numberColor = new Color(0, 161, 62);
		h.drawString(tactMeasurer.tactCount + "", x, baseY - h.dy() * 6, numberColor);
	}

	private void drawStaffLines(int y)
	{
		StaffComponent comp = (StaffComponent)context;
		Staff s = comp.staff;

		int tune = INote.nextIvoryTune(INote.nextIvoryTune(Note.FA + 12 * 2));

		// normal Note height lines
		for (int j = 0; j < 11; ++j) {
			tune = INote.prevIvoryTune(INote.prevIvoryTune(tune));
			if (j == 5) continue;
			int lineY = y + j * h.dy() * 2;
			h.drawLine(-3 * h.dx(), lineY, comp.getWidth() - s.getMarginX() * 6, lineY, new Color(128,128,255));

			int frameX = comp.getWidth() - s.getMarginX() * 6 + h.dx() / 6;
			Rectangle measureFrame = new Rectangle(frameX, lineY - h.dy() - 1, h.dx(), h.dy() * 2);
			h.drawString(tune + "", measureFrame, Color.BLUE);
		}

		// hidden Note height lines fot way too high Note-s
		for (int j = -3; j >= -5; --j) { // -3 - Mi; -5 -si
			int lineY = y + j * h.dy() * 2;
			h.drawLine(- 3 * h.dx(), lineY, comp.getWidth() - s.getMarginX() * 6, lineY, Color.LIGHT_GRAY);
		}
	}
}
