package org.sheet_midusic.staff.chord.nota;

import org.klesun_model.AbstractHandler;
import org.klesun_model.IComponent;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.chord.ChordComponent;
import org.sheet_midusic.staff.staff_config.KeySignature;

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
	public MidianaComponent getFocusedChild() {
		return null;
	}

	@Override
	protected AbstractHandler makeHandler() {
		return new NotaHandler(this);
	}

	public int drawOn(Graphics2D g, int x, int y, KeySignature siga) {
		new NotaPainter(this, g, x, y).draw(siga);
		return -100;
	}
}
