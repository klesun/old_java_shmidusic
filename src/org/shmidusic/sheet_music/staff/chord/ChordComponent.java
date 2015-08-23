package org.shmidusic.sheet_music.staff.chord;

import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONObject;
import org.klesun_model.AbstractHandler;
import org.klesun_model.Explain;
import org.klesun_model.IComponent;
import org.klesun_model.IModel;
import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.chord.nota.Nota;
import org.shmidusic.sheet_music.staff.chord.nota.NoteComponent;
import org.shmidusic.sheet_music.staff.staff_config.KeySignature;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.stuff.graphics.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class ChordComponent extends JComponent implements IComponent
{
	final public Chord chord;
	private Set<NoteComponent> noteComponents = new HashSet<>();

	final private IComponent parent;
	final private ChordHandler handler = new ChordHandler(this);

	int focusedIndex = -1;

	public ChordComponent(Chord chord, IComponent parent)
	{
		this.parent = parent;
		this.chord = chord;
		chord.notaList.get().forEach(this::addComponent);

		this.addMouseListener(handler);
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
		recalcTacts();

		return noteComp;
	}

	public void remove(Nota note)
	{
		int index = chord.getNotaSet().headSet(note).size();
		if (index <= getFocusedIndex()) { setFocusedIndex(getFocusedIndex() - 1); }

		chord.remove(note);
		noteComponents.remove(findChild(note));

		if (chord.getNotaSet().size() == 0) {
			getParentComponent().removeChord(chord);
		}

		recalcTacts();
	}

	public void setFocusedIndex(int value) {
		value = value >= chord.getNotaSet().size() ? chord.getNotaSet().size() - 1 : value;
		value = value < -1 ? -1 : value;
		this.focusedIndex = value;
		repaint();
	}

	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public void recalcTacts() {
		repaint();
		getParentComponent().refreshTacts(getParentComponent().staff.getChordList().indexOf(chord));
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

	@Override
	protected void paintComponent(Graphics g)
	{
		new ChordPainter(this, (Graphics2D)g, 0, 0).draw(determineSignature());
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Settings.inst().getStepWidth() * 2, Settings.inst().getStepHeight() * Staff.SISDISPLACE);
	}

	private KeySignature determineSignature()
	{
		KeySignature siga = getParentComponent().staff.getConfig().getSignature();
		getParentComponent().staff.findTact(chord).whenSuccess(tact -> {
			for (int i = 0; i < tact.accordList.indexOf(chord); ++i) {
				siga.consume(tact.accordList.get(i));
			}
		}); // it may not be success when we delete chords

		return siga;
	}

	/** @return timestamp in seconds */
	public Double determineStartTimestamp()
	{
		StaffConfig config = getParentComponent().staff.getConfig();
		Explain<Tact> opt = getParentComponent().staff.findTact(chord);

		if (opt.isSuccess()) {
			Tact tact = opt.getData();
			Fraction precedingChords = new LinkedList<>(tact.accordList.get())
				.subList(0, tact.accordList.indexOf(chord))
				.stream().map(c -> c.getFraction())
				.reduce(Fraction::add).orElse(new Fraction(0));

			Fraction startFraction = config.getTactSize().multiply(tact.tactNumber.get()).add(precedingChords);
			if (!tact.getIsCorrect()) {
				startFraction = startFraction.add(tact.getPrecedingRest());
			}

			return Nota.getTimeMilliseconds(startFraction, config.getTempo()) / 1000.0;
		} else {
			return - 100.0;
		}
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
	public NoteComponent getFocusedChild() {
		return getFocusedIndex() > -1 ? findChild(chord.notaList.get(getFocusedIndex())) : null;
	}

	@Override
	public AbstractHandler getHandler() {
		return this.handler;
	}

	// =======================
	// Event Handles
	// =======================

	public void triggerIsDiminendo() {
		chord.setIsDiminendo(!chord.getIsDiminendo());
		repaint();
	}

	public Explain moveFocus(int n)
	{
		Explain result;

		if (getFocusedIndex() + n > chord.getNotaSet().size() - 1 || getFocusedIndex() + n < 0) {
			this.setFocusedIndex(-1);
			result = new Explain<>(false, "End Of chord");
		} else {
			if (this.getFocusedIndex() + n < -1) {
				this.setFocusedIndex(chord.getNotaSet().size() - 1);
			} else {
				this.setFocusedIndex(this.getFocusedIndex() + n);
			}
			result = new Explain<>(true);
		}

		return result;
	}
}
