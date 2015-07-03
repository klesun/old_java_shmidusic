package Storyspace.Staff.Accord;

import Gui.Constants;
import Gui.ImageStorage;
import Gui.Settings;
import Model.AbstractPainter;
import Model.Field.Field;
import Storyspace.Staff.Accord.Nota.Nota;
import Storyspace.Staff.MidianaComponent;
import Stuff.OverridingDefaultClasses.Pnt;
import Stuff.Tools.Fp;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AccordPainter extends AbstractPainter {

	// it should be only in AbstractPainter
	@Deprecated final private Graphics gDeprecated;
	@Deprecated final private int xDeprecated;
	@Deprecated final private int yDeprecated;


	public AccordPainter(Accord context, Graphics g, int x, int y) {
		super(context, g, x, y);

		// REMOVE ASAP !!!
		this.gDeprecated = g;
		this.xDeprecated = x;
		this.yDeprecated = y;
	}

	@Override
	public void draw() {

		Accord a = (Accord)context;

		Boolean oneOctaveLower;
		if (oneOctaveLower = a.isHighestBotommedToFitSystem()) {
			drawString("8va", 0, 4 * dy(), Color.BLUE);
		}

		int eraseToY = getNotaY(a.getNotaList().stream().max((n1, n2) -> n1.compareTo(n2)).get(), oneOctaveLower) + Settings.getNotaHeight();

		fillRect(new Rectangle(0, 0, dx() * 2, eraseToY), Color.WHITE);

		for (int i = 0; i < a.getNotaList().size(); ++i) {
			Nota nota = a.notaList.get(i);
			int notaY = getNotaY(nota, oneOctaveLower);
			int notaX = i > 0 && a.notaList.get(i - 1).getAbsoluteAcademicIndex() == nota.getAbsoluteAcademicIndex()
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

		if (a.getParentStaff().getFocusedAccord() == a) {
			drawImage(ImageStorage.inst().getPointerImage(), dx(), 0);
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

	@Deprecated // it should be only in AbstractPainter
	private void drawModel(Nota model, int x0, int y0) { model.drawOn(gDeprecated, xDeprecated + x0, yDeprecated + y0); }

	@Deprecated // it should be only in AbstractPainter
	private void drawFields() {
		List<Field> drawableList = context.getModelHelper().getFieldStorage().stream().filter(f -> f.hasPaintingLambda()).collect(Collectors.toList());

		int w = dx() * 2;
		int dy = dy(); // TODO: it should be TOTAL_SPACE_FOR_THEM / THEIR_COUNT one day

		for (int i = 0; i < drawableList.size(); ++i) {
			Rectangle r = new Rectangle(xDeprecated, yDeprecated + dy, w, dy);
			drawableList.get(i).repaintIfNeeded(gDeprecated, r);
		}
	}

	private Straight getTraitCoordinates(Nota nota) {
		int r = Settings.getNotaWidth() * 12 / 25;
		return new Straight(nota.getAncorPoint().plus(-r, 0), nota.getAncorPoint().plus(r, 0));
	}
}
