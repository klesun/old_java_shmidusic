package org.shmidusic.sheet_music.staff.chord;

import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONObject;
import org.klesun_model.AbstractHandler;
import org.klesun_model.Explain;
import org.klesun_model.IComponent;
import org.klesun_model.IModel;
import org.klesun_model.field.IField;
import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.sheet_music.staff.chord.note.NoteComponent;
import org.shmidusic.sheet_music.staff.chord.note.NoteHandler;
import org.shmidusic.sheet_music.staff.staff_config.KeySignature;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.stuff.graphics.Settings;
import org.shmidusic.stuff.midi.DeviceEbun;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
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
		chord.noteList.forEach(this::addComponent);

		this.addMouseListener(handler);
	}

	public NoteComponent addNewNote(int tune, int channel)
	{
		NoteComponent noteComp = addComponentAndRepaint(chord.addNewNote(tune, channel));

		// TODO: we better check "if input is not midi device"
		if (DeviceEbun.isPlaybackSoftware()) {
			NoteHandler.play(noteComp);
		}

		return noteComp;
	}

	public NoteComponent addNewNote(Note note) {
		return addComponentAndRepaint(chord.add(note.setKeydownTimestamp(System.currentTimeMillis())));
	}

    private NoteComponent addComponentAndRepaint(Note note)
    {
        NoteComponent cmp = addComponent(note);
        recalcTacts();
        return cmp;
    }

	private NoteComponent addComponent(Note note)
	{
		NoteComponent noteComp = new NoteComponent(note, this);
		noteComponents.add(noteComp);
		return noteComp;
	}

	public void remove(Note note)
	{
		int index = chord.noteList.indexOf(note);
		if (index <= getFocusedIndex()) { setFocusedIndex(getFocusedIndex() - 1); }

		chord.remove(note);
		noteComponents.remove(findChild(note));

		if (chord.noteList.size() == 0) {
			getParentComponent().removeChord(chord);
		}

		recalcTacts();
	}

	public void setFocusedIndex(int value) {
		value = value >= chord.noteList.size() ? chord.noteList.size() - 1 : value;
		value = value < -1 ? -1 : value;
		this.focusedIndex = value;
		repaint();
	}

	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public void recalcTacts() {
		repaint();
		getParentComponent().refreshTacts();
	}

	public NoteComponent findChild(Note note)
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
		getParentComponent().staff.findTact(chord).ifPresent(tact -> {
            for (int i = 0; i < tact.chordList.indexOf(chord); ++i) {
                siga.consume(tact.chordList.get(i));
            }
        }); // it may not be success when we delete chords

		return siga;
	}

	/** @return timestamp in seconds */
	public Double determineStartTimestamp()
	{
        StaffConfig config = getParentComponent().staff.getConfig();
        Fraction chordStart = getParentComponent().staff.findChordStart(chord).orElse(new Fraction(-100));

        return Note.getTimeMilliseconds(chordStart, config.getTempo()) / 1000.0;
	}

	public boolean isPartOfSelection()
	{
		StaffComponent s = getParentComponent();
		int index = s.staff.chordList.indexOf(this.chord);

		return s.isSelectionActive() && (
			(index > s.getSelectionStart() && index <= s.staff.getFocusedIndex()) ||
			(index > s.staff.getFocusedIndex() && index <= s.getSelectionStart())
		);
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
		return getFocusedIndex() > -1 ? findChild(chord.noteList.get(getFocusedIndex())) : null;
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

		if (getFocusedIndex() + n > chord.noteList.size() - 1 || getFocusedIndex() + n < 0) {
			this.setFocusedIndex(-1);
			result = new Explain<>(false, "End Of chord");
		} else {
			if (this.getFocusedIndex() + n < -1) {
				this.setFocusedIndex(chord.noteList.size() - 1);
			} else {
				this.setFocusedIndex(this.getFocusedIndex() + n);
			}
			result = new Explain<>(true);
		}

		return result;
	}
}
