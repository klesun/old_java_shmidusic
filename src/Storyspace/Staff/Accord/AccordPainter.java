package Storyspace.Staff.Accord;

import Gui.Settings;
import Model.AbstractPainter;
import Storyspace.Staff.Accord.Nota.Nota;
import Stuff.OverridingDefaultClasses.Pnt;
import Stuff.Tools.Fp;

import java.awt.*;
import java.util.*;

public class AccordPainter extends AbstractPainter {

	public AccordPainter(Accord context, Graphics g, int x, int y) {
		super(context, g, x, y);
	}

	@Override
	public void draw() {

		Accord a = (Accord)context;

		Boolean oneOctaveLower;
		if (oneOctaveLower = a.isHighestBotommedToFitSystem()) {
			drawString("8va", 0, 4 * dy(), Color.BLUE);
		}

		for (int i = 0; i < a.getNotaList().size(); ++i) {
			Nota nota = a.getNotaList().get(i);
			int notaY = a.getLowestPossibleNotaY() - dy() * nota.getAbsoluteAcademicIndex() + (oneOctaveLower ? 7 * dy() : 0);
			int notaX = i > 0 && a.getNotaList().get(i - 1).getAbsoluteAcademicIndex() == nota.getAbsoluteAcademicIndex()
				? dx() / 3 // TODO: draw them flipped
				: 0;

			if (nota == a.getFocusedNota()) {
				drawDot(nota.getAncorPoint().plus(notaX + dx() / 2, notaY - dy()), Settings.getStepHeight(), Color.RED);
			}

			drawModel(nota, notaX, notaY);
			if (nota.isStriked() ^ oneOctaveLower) {
				drawLine(getTraitCoordinates(nota).plus(new Point(notaX, notaY)));
			}
		}

//		surface.setColor(Color.BLACK);
//		surface.drawString(a.getSlog(), x, y + Constants.FONT_HEIGHT); // dead a bit

		if (a.getIsDiminendo()) {
			drawLine(dx() / 2, dy() * 4, dx() * 3/2, dy() * 6);
			drawLine(dx() / 2, dy() * 8, dx() * 3/2, dy() * 6);
		}
	}

	private Straight getTraitCoordinates(Nota nota) {
		int r = Settings.getNotaWidth() * 12 / 25;
		return new Straight(nota.getAncorPoint().plus(-r, 0), nota.getAncorPoint().plus(r, 0));
	}
}
