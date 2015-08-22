package org.shmidusic.staff.chord.nota;

import org.klesun_model.AbstractHandler;
import org.klesun_model.Explain;
import org.shmidusic.staff.MidianaComponent;
import org.shmidusic.staff.chord.ChordComponent;
import org.shmidusic.staff.staff_config.KeySignature;

import java.awt.*;

public class NoteComponent extends MidianaComponent
{
	final public Nota note;

	// TODO: seconds parameter should be IComponent
	public NoteComponent(Nota note, ChordComponent parent) {
		super(parent);
		this.note = note;
	}

	public ChordComponent getParentComponent()
	{
		return (ChordComponent)getModelParent();
	}

	@Override
	public Nota getModel() {
		return this.note;
	}

	@Override
	public MidianaComponent getFocusedChild() {
		return null;
	}

	@Override
	protected AbstractHandler makeHandler() {
		return new NotaHandler(this);
	}

	public void drawOn(Graphics2D g, int x, int y, KeySignature siga) {
		new NotaPainter(this, g, x, y).draw(siga);
	}

	public Explain triggerIsSharp()
	{
		return new Explain(note.isEbony(), "Note is not ebony!").runIfSuccess(note::triggerIsSharp);
	}
}