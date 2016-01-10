package org.shmidusic.sheet_music.staff.chord;

import org.klesun_model.PaintHelper;
import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.sheet_music.staff.chord.note.NoteComponent;
import org.shmidusic.sheet_music.staff.staff_config.KeySignature;
import org.shmidusic.stuff.graphics.ImageStorage;

import java.awt.*;

public class ChordPainter
{
	final private PaintHelper h;
	final private ChordComponent context;

	public ChordPainter(ChordComponent context, Graphics2D g, int x, int y) {
		this.context = context;
		this.h = new PaintHelper(g,x,y);
	}

	public void draw(KeySignature siga)
	{
		ChordComponent comp = context;
		Chord a = comp.chord;

		if (comp.isPartOfSelection()) {
			h.fillRect(new Rectangle(h.dx() * 2, Staff.SISDISPLACE * h.dy()), new Color(0,0,255,64));
		}

		for (int i = 0; i < a.noteList.size(); ++i) {
			Note note = a.noteList.get(i);
			int noteY = getLowestPossibleNoteY() - h.dy() * note.ivoryIndex(siga);
			int noteX = i > 0 && a.noteList.get(i - 1).ivoryIndex(siga) == note.ivoryIndex(siga)
				? h.dx() / 3 // TODO: draw them flipped
				: 0;

			if (comp.getFocusedIndex() != -1 && note == comp.getFocusedChild().note) {
				h.fillRect(new Rectangle(0, noteY + 6 * h.dy(), 2 * h.dx(), 2 * h.dy()), new Color(0,255,0,127));
			}

			NoteComponent noteComp = comp.findChild(note);
			h.drawModel((g, x, y) -> noteComp.drawOn(g, x, y, siga), noteX, noteY);
			siga.consume(noteComp.note);
		}

		if (comp.getParentComponent().getFocusedChild() == comp) {
			h.drawImage(ImageStorage.inst().getPointerImage(), h.dx(), 0);
		}
	}

	private int getLowestPossibleNoteY() {
		return 50 * h.dy();
	}
}
