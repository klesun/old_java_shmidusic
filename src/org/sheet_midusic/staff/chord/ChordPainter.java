package org.sheet_midusic.staff.chord;

import org.klesun_model.AbstractPainter;
import org.sheet_midusic.staff.chord.nota.Nota;
import org.sheet_midusic.stuff.OverridingDefaultClasses.Pnt;

import java.awt.*;
import java.util.function.Consumer;

public class ChordPainter extends AbstractPainter {

	public ChordPainter(Chord context, Graphics2D g, int x, int y) {
		super(context, g, x, y);
	}

	@Override
	public void draw(Boolean completeRepaint) {

		Chord a = (Chord)context;

		for (int i = 0; i < a.getNotaSet().size(); ++i) {
			Nota nota = a.notaList.get(i);
			int notaY = a.getLowestPossibleNotaY() - dy() * nota.ivoryIndex();
			int notaX = i > 0 && a.notaList.get(i - 1).ivoryIndex() == nota.ivoryIndex()
				? dx() / 3 // TODO: draw them flipped
				: 0;

			if (nota == a.getFocusedNota()) {
				Pnt point = new Pnt(notaX + dx() * 2, notaY + 6 * dy());
				drawDot(point, dy(), Color.RED);
			}

			drawModel(nota, notaX, notaY, false);
		}

		if (a.getParentStaff().getFocusedAccord() == a) {
			drawImage(a.getImageStorage().getPointerImage(), dx(), 0);
		}

		drawFields(completeRepaint);
	}

	public static Consumer<Graphics> diminendoPainting(Rectangle r, Boolean value) {
		return g -> {
			double stretch = 0.5;
			g.setColor(Color.BLACK);
			int x1 = (int)(r.x + r.width * stretch / 2);
			int x2 = (int)(r.x - r.width * stretch / 2) + r.width;

			if (value) {
				g.drawLine(x1, r.y, x2, r.y + r.height / 2);
				g.drawLine(x1, r.y + r.height, x2, r.y + r.height / 2);
			}
		};
	}

	public static Consumer<Graphics> slogPainting(Rectangle r, String value) {
		return g -> {
			if (g.getFontMetrics(g.getFont()).stringWidth(value) > 0) {
				g.setColor(Color.BLACK);
				fitTextIn(r, value, g);
			}
		};
	}
}
