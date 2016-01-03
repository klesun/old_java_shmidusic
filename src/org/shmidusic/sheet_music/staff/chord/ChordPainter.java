package org.shmidusic.sheet_music.staff.chord;

import org.klesun_model.AbstractPainter;
import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.sheet_music.staff.chord.note.NoteComponent;
import org.shmidusic.sheet_music.staff.staff_config.KeySignature;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.shmidusic.stuff.graphics.Settings;

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

		if (comp.isPartOfSelection()) {
			fillRect(new Rectangle(dx() * 2, Staff.SISDISPLACE * dy()), new Color(0,0,255,64));
		}

		for (int i = 0; i < a.getNoteSet().size(); ++i) {
			Note note = a.noteList.get(i);
			int noteY = getLowestPossibleNoteY() - dy() * note.ivoryIndex(siga);
			int noteX = i > 0 && a.noteList.get(i - 1).ivoryIndex(siga) == note.ivoryIndex(siga)
				? dx() / 3 // TODO: draw them flipped
				: 0;

			if (comp.getFocusedIndex() != -1 && note == comp.getFocusedChild().note) {
				fillRect(new Rectangle(0, noteY + 6 * dy(), 2 * dx(), 2 * dy()), new Color(0,255,0,127));
			}

			NoteComponent noteComp = comp.findChild(note);
			drawModel((g, x, y) -> noteComp.drawOn(g, x, y, siga), noteX, noteY);
			siga.consume(noteComp.note);
		}

		if (comp.getParentComponent().getFocusedChild() == comp) {
			drawImage(ImageStorage.inst().getPointerImage(), dx(), 0);
		}

		drawFields();
	}

	private int getLowestPossibleNoteY() {
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
