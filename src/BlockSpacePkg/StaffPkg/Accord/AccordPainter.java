package BlockSpacePkg.StaffPkg.Accord;

import Model.AbstractPainter;
import BlockSpacePkg.StaffPkg.Accord.Nota.Nota;

import java.awt.*;

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

		int eraseToY = getNotaY(a.getNotaList().stream().max((n1, n2) -> n1.compareTo(n2)).get(), oneOctaveLower) + context.getSettings().getNotaHeight();

		fillRect(new Rectangle(0, 0, dx() * 2, eraseToY), Color.WHITE);

		for (int i = 0; i < a.getNotaList().size(); ++i) {
			Nota nota = a.notaList.get(i);
			int notaY = getNotaY(nota, oneOctaveLower);
			int notaX = i > 0 && a.notaList.get(i - 1).getAbsoluteAcademicIndex() == nota.getAbsoluteAcademicIndex()
				? dx() / 3 // TODO: draw them flipped
				: 0;

			if (nota == a.getFocusedNota()) {
				drawDot(nota.getAncorPoint().plus(notaX + dx() / 2, notaY - dy()), dy(), Color.RED);
			}

			drawModel(nota, notaX, notaY, false); // TODO: if it was true, we wouldn't get here, but it kinda sux now i think - pass the flag here from Accord!
			if (nota.isStriked() ^ oneOctaveLower) {
				drawLine(getTraitCoordinates(nota).plus(new Point(notaX, notaY)));
			}
		}

		if (a.getParentStaff().getFocusedAccord() == a) {
			drawImage(a.getImageStorage().getPointerImage(), dx(), 0);
		}

		drawFields();

		// gonna use Field::painting instead

//		drawString(a.getSlog(), 0, Constants.FONT_HEIGHT, Color.BLACK);

//		if (a.getIsDiminendo()) {
//			drawLine(dx() / 2, dy() * 4, dx() * 3/2, dy() * 6);
//			drawLine(dx() / 2, dy() * 8, dx() * 3/2, dy() * 6);
//		}
	}

	private int getNotaY(Nota nota, Boolean oneOctaveLower) {
		Accord a = (Accord)context;
		return a.getLowestPossibleNotaY() - dy() * nota.getAbsoluteAcademicIndex() + (oneOctaveLower ? 7 * dy() : 0);
	}

	private Straight getTraitCoordinates(Nota nota) {
		int r = context.getSettings().getNotaWidth() * 12 / 25;
		return new Straight(nota.getAncorPoint().plus(-r, 0), nota.getAncorPoint().plus(r, 0));
	}
}
