package org.shmidusic.stuff.midi;

import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.sheet_music.staff.chord.nota.Nota;
import org.shmidusic.stuff.midi.standard_midi_file.Track;
import org.shmidusic.stuff.midi.standard_midi_file.event.NoteOff;
import org.shmidusic.stuff.midi.standard_midi_file.event.NoteOn;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.tools.INota;

import java.util.Map;

public class SmfScheduler implements IMidiScheduler
{
	final private Map<Integer, Track> trackDict;
	final private StaffConfig config;

	public SmfScheduler(Map<Integer, Track> trackDict, StaffConfig config) {
		this.trackDict = trackDict;
		this.config = config;
	}

	public void addNoteTask(Fraction when, INota nota) {
		if (!trackDict.containsKey(nota.getChannel())) {
			trackDict.put(nota.getChannel(), new Track());
		}
		trackDict.get(nota.getChannel()).addEvent(new NoteOn(nota, time(when)));
		trackDict.get(nota.getChannel()).addEvent(new NoteOff(nota, time(when.add(nota.getRealLength()))));
	}

	private int time(Fraction f) {
		return Nota.getTimeBeats(f, config.getTempo());
	}
}
