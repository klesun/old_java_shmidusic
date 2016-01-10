package org.shmidusic.sheet_music.staff.chord.note;

import org.klesun_model.Explain;
import org.klesun_model.IKeyHandler;
import org.shmidusic.sheet_music.staff.MidianaComponent;
import org.shmidusic.sheet_music.staff.chord.ChordComponent;
import org.shmidusic.sheet_music.staff.staff_config.KeySignature;

import java.awt.*;

public class NoteComponent extends MidianaComponent
{
	final public Note note;

	// TODO: seconds parameter should be IComponent
	public NoteComponent(Note note, ChordComponent parent) {
		super(parent);
		this.note = note;
	}

	public ChordComponent getParentComponent()
	{
		return (ChordComponent) getModelParent();
	}

	@Override
	public Note getModel() {
		return this.note;
	}

	@Override
	public MidianaComponent getFocusedChild() {
		return null;
	}

	@Override
	protected IKeyHandler makeHandler() {
		return new NoteHandler(this);
	}

	public void drawOn(Graphics2D g, int x, int y, KeySignature siga) {
		new NotePainter(this, g, x, y).draw(siga);
	}

	public Explain triggerIsSharp()
	{
		return new Explain(note.isEbony(), "Note is not ebony!").runIfSuccess(note::triggerIsSharp);
	}
}
