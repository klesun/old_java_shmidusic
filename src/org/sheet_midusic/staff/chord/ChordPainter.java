package org.sheet_midusic.staff.chord;

import org.klesun_model.AbstractPainter;
import org.sheet_midusic.staff.chord.nota.Nota;
import org.sheet_midusic.staff.chord.nota.NoteComponent;
import org.sheet_midusic.staff.staff_config.KeySignature;
import org.sheet_midusic.stuff.OverridingDefaultClasses.Pnt;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TriConsumer;
import org.sheet_midusic.stuff.graphics.ImageStorage;

import java.awt.*;
import java.util.function.Consumer;

public class ChordPainter extends AbstractPainter {

	public ChordPainter(ChordComponent context, Graphics2D g, int x, int y) {
		super(context, g, x, y);
	}

	public void draw(KeySignature siga)
	{
		ChordComponent comp = (ChordComponent)context;
		Chord a = comp.chord;

		for (int i = 0; i < a.getNotaSet().size(); ++i) {
			Nota nota = a.notaList.get(i);
			int notaY = getLowestPossibleNotaY() - dy() * nota.ivoryIndex(siga);
			int notaX = i > 0 && a.notaList.get(i - 1).ivoryIndex(siga) == nota.ivoryIndex(siga)
				? dx() / 3 // TODO: draw them flipped
				: 0;

			if (comp.getFocusedIndex() != -1 && nota == comp.getFocusedChild().note) {
				fillRect(new Rectangle(0, notaY + 6 * dy(), 2 * dx(), 2 * dy()), new Color(0,255,0,127));
			}

			NoteComponent noteComp = comp.findChild(nota);
			drawModel((g, x, y) -> noteComp.drawOn(g, x, y, siga), notaX, notaY);
		}

		if (comp.getParentComponent().getFocusedChild() == comp) {
			drawImage(ImageStorage.inst().getPointerImage(), dx(), 0);
		}

		drawFields();
	}

	private int getLowestPossibleNotaY() {
		return 50 * dy();
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
