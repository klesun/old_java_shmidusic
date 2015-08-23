package org.shmidusic.stuff.Midi;

import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.sheet_music.staff.chord.nota.Nota;
import org.jm.midi.Track;
import org.jm.midi.event.NoteOff;
import org.jm.midi.event.NoteOn;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.tools.jmusic_integration.INota;

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
