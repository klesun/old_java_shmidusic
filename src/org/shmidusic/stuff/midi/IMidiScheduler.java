package org.shmidusic.stuff.midi;

import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.tools.INote;

public interface IMidiScheduler
{
	// read "final"
	default void addNoteTask(Fraction when, INote note) {
		addNoteOnTask(when, note.getTune(), note.getChannel());
		addNoteOffTask(when.add(note.getLength()), note.getTune(), note.getChannel());
	}
	void addNoteOnTask(Fraction when, int tune, int channel);
	void addNoteOffTask(Fraction when, int tune, int channel);
}
