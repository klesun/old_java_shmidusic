package org.shmidusic.stuff.midi;

import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.stuff.midi.standard_midi_file.Track;
import org.shmidusic.stuff.midi.standard_midi_file.event.NoteOff;
import org.shmidusic.stuff.midi.standard_midi_file.event.NoteOn;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.tools.INote;

import java.util.Map;

public class SmfScheduler implements IMidiScheduler
{
	final private Map<Integer, Track> trackDict;
	final private StaffConfig config;

	public SmfScheduler(Map<Integer, Track> trackDict, StaffConfig config) {
		this.trackDict = trackDict;
		this.config = config;
	}

	public void addNoteTask(Fraction when, INote note) {
		if (!trackDict.containsKey(note.getChannel())) {
			trackDict.put(note.getChannel(), new Track());
		}
		trackDict.get(note.getChannel()).addEvent(new NoteOn(note, time(when)));
		trackDict.get(note.getChannel()).addEvent(new NoteOff(note, time(when.add(note.getRealLength()))));
	}

	private int time(Fraction f) {
		return Note.getTimeBeats(f, config.getTempo());
	}
}
