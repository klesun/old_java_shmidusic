package blockspace.staff;

import blockspace.staff.accord.Accord;
import model.AbstractPainter;
import org.apache.commons.math3.fraction.Fraction;
import stuff.tools.jmusic_integration.INota;

import java.awt.*;
import java.util.*;
import java.util.stream.IntStream;

public class StaffPainter extends AbstractPainter
{
	public StaffPainter(Staff context, Graphics g, int x, int y) {
		// this trick won't do when Painter becames child of Staff
		super(context, g, x + context.getMarginX() + 3 * context.dx(), y + context.getMarginY()); // 3dx - violin/bass keys and Config
	}

	@Override
	public void draw(Boolean completeRepaint)
	{
		Staff s = (Staff)context;

		Staff.TactMeasurer tactMeasurer = new Staff.TactMeasurer(s);

		int i = 0;
		for (java.util.List<Accord> row : s.getAccordRowList()) {

			int y = i * Staff.SISDISPLACE * dy(); // bottommest y nota may be drawn on

			// normal Nota height lines
			for (int j = 0; j < 11; ++j) {
				if (j == 5) continue;
				int lineY = y + j * dy() * 2;
				drawLine(- 3 * dx(), lineY, s.getWidth() - s.getMarginX() * 6, lineY, Color.BLUE);
			}

			// hidden Nota height lines fot way too high Nota-s
			for (int j = -3; j >= -5; --j) { // -3 - Mi; -5 -si
				int lineY = y + j * dy() * 2;
				drawLine(- 3 * dx(), lineY, s.getWidth() - s.getMarginX() * 6, lineY, Color.LIGHT_GRAY);
			}

			int j = 0;
			for (Accord accord : row) {
				int x = j * (2 * dx());

				drawModel(accord, x, y - 12 * dy(), completeRepaint);
				if (tactMeasurer.inject(accord)) {
					drawTactLine(x + dx() * 2, y, tactMeasurer);
				}

				++j;
			}

			if (completeRepaint) {
				drawImage(s.getImageStorage().getViolinKeyImage(), - 3 * dx(), y - 3 * dy());
				drawImage(s.getImageStorage().getBassKeyImage(), - 3 * dx(), 11 * dy() + y);
			}

			++i;
		}

		drawModel(s.getConfig(), -dx(), 0, completeRepaint);
	}

	private void drawTactLine(int x, int baseY, Staff.TactMeasurer tactMeasurer)
	{
		Color lineColor = tactMeasurer.sumFraction.equals(new Fraction(0))
			? Color.BLACK
			: Color.RED;

		drawLine(x, baseY - dy() * 5, x, baseY + dy() * 20, lineColor);
		Color numberColor = new Color(0, 161, 62);
		drawString(tactMeasurer.tactCount + "", x, baseY - dy() * 6, numberColor);
	}
}