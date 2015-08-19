package org.sheet_midusic.staff.chord;

import org.json.JSONObject;
import org.klesun_model.AbstractHandler;
import org.klesun_model.IComponent;
import org.klesun_model.IModel;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.chord.nota.Nota;
import org.sheet_midusic.staff.chord.nota.NoteComponent;
import org.sheet_midusic.staff.staff_config.KeySignature;
import org.sheet_midusic.staff.staff_panel.StaffComponent;
import org.sheet_midusic.stuff.graphics.Settings;
import org.sheet_midusic.stuff.main.Main;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ChordComponent extends JComponent implements IComponent
{
	final public Chord chord;
	private Set<NoteComponent> noteComponents = new HashSet<>();

	final private IComponent parent;
	final private ChordHandler handler = new ChordHandler(this);

	public ChordComponent(Chord chord, IComponent parent)
	{
		this.parent = parent;
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

	public NoteComponent findChild(Nota note)
	{
		return noteComponents.stream().filter(p -> p.note == note).findAny().get();
	}

	public Stream<NoteComponent> childStream()
	{
		return noteComponents.stream().sorted((c1, c2) -> c1.note.compareTo(c2.note));
	}

	public StaffComponent getParentComponent() // may conflict with java's Component method
	{
		return (StaffComponent)this.getModelParent();
	}

	public int drawOn(Graphics2D g, int x, int y, KeySignature siga) {
		new ChordPainter(this, g, x, y).draw(siga);
		return -100;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		KeySignature siga = new KeySignature(0); // TODO: do something so we could get sigu from staff config
		new ChordPainter(this, (Graphics2D)g, 0, 0).draw(siga);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Settings.inst().getStepWidth() * 2, Settings.inst().getStepHeight() * Staff.SISDISPLACE);
	}

	// ========================
	// Implementing IComponent methods
	// ========================

	@Override
	public IModel getModel() {
		return this.chord;
	}

	@Override
	public IComponent getModelParent() {
		return this.parent;
	}

	@Override
	public MidianaComponent getFocusedChild() {
		return chord.getFocusedChild() != null ? findChild(chord.getFocusedChild()): null;
	}

	@Override
	public AbstractHandler getHandler() {
		return this.handler;
	}
}
