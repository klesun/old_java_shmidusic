package org.sheet_midusic.staff.chord;

import org.json.JSONObject;
import org.klesun_model.AbstractHandler;
import org.klesun_model.IComponent;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.chord.nota.Nota;
import org.sheet_midusic.staff.chord.nota.NoteComponent;
import org.sheet_midusic.staff.staff_config.KeySignature;
import org.sheet_midusic.staff.staff_panel.StaffComponent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ChordComponent extends MidianaComponent
{
	final public Chord chord;
	private Set<NoteComponent> noteComponents = new HashSet<>();

	public ChordComponent(Chord chord, IComponent parent)
	{
		super(parent);
		this.chord = chord;
		chord.notaList.get().forEach(this::addComponent);
	}

	public NoteComponent addNewNota(int tune, int channel) {
		return addComponent(chord.addNewNota(tune, channel));
	}

	public NoteComponent addNewNota(JSONObject newNotaJs) {
		return addComponent(chord.addNewNota(newNotaJs));
	}

	private NoteComponent addComponent(Nota note)
	{
		NoteComponent noteComp = new NoteComponent(note, this);
		noteComponents.add(noteComp);
		return noteComp;
	}

	public void remove(Nota note)
	{
		chord.remove(note);
		noteComponents.remove(findChild(note));
	}

	public StaffComponent getParentComponent() // may conflict with java's Component method
	{
		return (StaffComponent)this.getModelParent();
	}

	@Override
	public MidianaComponent getFocusedChild() {
		return chord.getFocusedChild() != null ? findChild(chord.getFocusedChild()): null;
	}

	public NoteComponent findChild(Nota note)
	{
		return noteComponents.stream().filter(p -> p.note == note).findAny().get();
	}

	public Stream<NoteComponent> childStream()
	{
		return noteComponents.stream().sorted((c1, c2) -> c1.note.compareTo(c2.note));
	}

	@Override
	protected AbstractHandler makeHandler() {
		return new ChordHandler(this);
	}

	public int drawOn(Graphics2D g, int x, int y, KeySignature siga) {
		new ChordPainter(this, g, x, y).draw(siga);
		return -100;
	}
}
