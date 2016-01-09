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

public class Chord extends AbstractModel
{
	public Field<String> tactNumber = add("tactNumber", "-1.-1"); // just decorational field for json readability
	private Field<Boolean> isDiminendo = add("isDiminendo", false);
	public Field<String> slog = add("slog", "").setOmitDefaultFromJson(true);
	public Arr<Note> noteList = add("noteList", new TreeSet<>(), Note.class);

	// getters/setters
	public Stream<Note> noteStream(Predicate<Note> filterLambda) {
		return noteList.stream().filter(filterLambda);
	}

	// TODO: add parameter to Chord: "explicitLength" and use it instead of our fake-note-pauses
	/** @return stream of real notes... maybe should store pause list separately? */
	public Stream<Note> noteStream() {
		return noteStream(n -> !n.isPause());
	}

	public long getEarliestKeydown() {
		Note note = this.noteList.stream().reduce(null, (a, b) -> a != null && a.keydownTimestamp < b.keydownTimestamp ? a : b);
		return note != null ? note.keydownTimestamp : 0;
	}

	public Note findByTuneAndChannel(int tune, int channel) {
		return this.noteList.stream().filter(n -> n.tune.get() == tune && n.getChannel() == channel).findFirst().orElse(null);
	}

	public int getShortestTime(int tempo) {
		return Note.getTimeMilliseconds(getFraction(), tempo);
	}

	public Fraction getFraction() {
		return getShortest().map(INote::getLength).orElse(new Fraction(0));
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
        removeRedundantPauseIfAny();

		return newNote;
	}

	public Note addNewNote(int tune, int channel) {
		return add(new Note(tune, channel)
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
            n -> noteList.stream()
                .filter(k -> k.getLength().equals(n.getLength()) && !k.isPause())
                .findAny().ifPresent(
                    k -> noteList.stream().filter(Note::isPause)
                        .collect(Collectors.toList())
                        .forEach(this::remove)
        ));
    }

    private Optional<Note> getShortest() {
        return noteList.stream().sorted((a, b) -> a.isLongerThan(b) ? 1 : -1).findFirst();
    }
}
