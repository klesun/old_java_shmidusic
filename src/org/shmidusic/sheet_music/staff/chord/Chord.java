package org.shmidusic.sheet_music.staff.chord;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;
import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.stuff.tools.INote;
import org.apache.commons.math3.fraction.Fraction;

import org.json.JSONObject;

public class Chord extends AbstractModel
{
	private Field<Boolean> isDiminendo = new Field<>("isDiminendo", false, this).setPaintingLambda(ChordPainter::diminendoPainting);
	public Field<String> slog = new Field<>("slog", "", this).setPaintingLambda(ChordPainter::slogPainting).setOmitDefaultFromJson(true);
	public Arr<Note> noteList = new Arr<>("noteList", new TreeSet<>(), this, Note.class);

	// getters/setters

	public TreeSet<Note> getNoteSet() {
		return (TreeSet) noteList.get();
	}

	public Stream<Note> noteStream(Predicate<Note> filterLambda) {
		return getNoteSet().stream().filter(filterLambda);
	}

	// TODO: add parameter to Chord: "explicitLength" and use it instead of our fake-note-pauses
	/** @return stream of real notes... maybe should store pause list separately? */
	public Stream<Note> noteStream() {
		return noteStream(n -> !n.isPause());
	}

	public long getEarliestKeydown() {
		Note note = this.getNoteSet().stream().reduce(null, (a, b) -> a != null && a.keydownTimestamp < b.keydownTimestamp ? a : b);
		return note != null ? note.keydownTimestamp : 0;
	}

	public Note findByTuneAndChannel(int tune, int channel) {
		return this.getNoteSet().stream().filter(n -> n.tune.get() == tune && n.getChannel() == channel).findFirst().orElse(null);
	}

	public int getShortestTime(int tempo) {
		return Note.getTimeMilliseconds(getFraction(), tempo);
	}

	public Fraction getFraction() {
		return getShortest().map(INote::getRealLength).orElse(new Fraction(0));
	}

	// field getters/setters

	public String getSlog() { return this.slog.get(); }
	public Chord setSlog(String value) { this.slog.set(value); return this; }

	public Boolean getIsDiminendo() { return isDiminendo.get(); }
	public void setIsDiminendo(Boolean value) { isDiminendo.set(value); }

    public Chord setExplicitLength(Fraction length) {
        addNewNote(0, 0).setLength(length);

        removeRedundantPauseIfAny();
        return this;
    }

	// event handles

	public Note addNewNote(INote source) {
		Note newNote = addNewNote(source.getTune(), source.getChannel()).setLength(source.getLength());
		newNote.isTriplet.set(source.isTriplet());
        removeRedundantPauseIfAny();

		return newNote;
	}

	public Note addNewNote(int tune, int channel) {
		return add(new Note(tune, channel)
			.setKeydownTimestamp(System.currentTimeMillis()));
	}

	public Note addNewNote(JSONObject newNoteJs) {
		return add(new Note().reconstructFromJson(newNoteJs)
			.setKeydownTimestamp(System.currentTimeMillis()));
	}

	synchronized public Note add(Note note) {
		return noteList.add(note);
	}

	synchronized public void remove(Note note) {
		noteList.remove(note);
	}

    public void removeRedundantPauseIfAny()
    {
        getShortest().ifPresent(
            n -> noteList.get().stream()
                .filter(k -> k.getRealLength().equals(n.getRealLength()) && !k.isPause())
                .findAny().ifPresent(
                    k -> noteList.get().stream().filter(Note::isPause)
                        .collect(Collectors.toList())
                        .forEach(this::remove)
        ));
    }

    private Optional<Note> getShortest() {
        return noteList.get().stream().sorted((a, b) -> a.isLongerThan(b) ? 1 : -1).findFirst();
    }
}
