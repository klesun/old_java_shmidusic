package org.shmidusic.stuff.musica;

import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.Main;
import org.klesun_model.Explain;
import org.shmidusic.sheet_music.staff.Staff;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.sheet_music.staff.chord.ChordComponent;
import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.sheet_music.staff.chord.note.NoteComponent;
import org.shmidusic.stuff.midi.DeviceEbun;
import org.shmidusic.stuff.midi.IMidiScheduler;
import org.shmidusic.stuff.midi.SmfScheduler;
import org.shmidusic.stuff.tools.INote;
import org.shmidusic.stuff.tools.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Playback {

	@Deprecated // instance MAZAFAKA
	public static Thread diminendoThread = null;

	final private StaffComponent staffComp;
	private PlaybackTimer runningProcess = null;

	public Playback(StaffComponent staffComp) {
		this.staffComp = staffComp;
	}

	public void trigger() {
		if (this.runningProcess == null) {
			DeviceEbun.closeAllNotes();
			play();
		} else {
			interrupt();
		}
	}

	public Boolean interrupt() {
		if (this.runningProcess != null) {
			DeviceEbun.closeAllNotes();
			this.runningProcess.interrupt();
			this.runningProcess = null;
		}
		return true;
	}

	private Explain play() {
		Staff staff = staffComp.staff;
		if (!staff.getChordList().isEmpty()) {

			runningProcess = staff.getConfig().useHardcoreSynthesizer.get()
					? new PlaybackTimer.KlesunthesizerTimer(staff.getConfig())
					: new PlaybackTimer(staff.getConfig());

			staffComp.moveFocus(-1);
			int startFrom = staff.getFocusedIndex() + 1;

			streamTo(runningProcess, startFrom, now -> runningProcess.addTask(now, () -> staffComp.moveFocus(1)));

			runningProcess.appendTask(new Fraction(1), this::interrupt);
			runningProcess.start();
			return new Explain(true);
		} else {
			return new Explain(false, "staff is empty");
		}
	}

	public void streamTo(IMidiScheduler scheduler) {
		streamTo(scheduler, 0, f -> {});
	}

	private void streamTo(IMidiScheduler scheduler, int startFrom, Consumer<Fraction> onAccord)
	{
		Fraction sumFraction = new Fraction(0);
		Set<Note> openedLinks = new HashSet<>();

		for (Chord chord : staffComp.staff.getChordList().subList(startFrom, staffComp.staff.getChordList().size())) {

			final Fraction noteStart = sumFraction;

			chord.noteStream().forEach(note -> {
				if (note.getTune() != 0) { // 0 means pause in my world

					// TODO: we don't handle a case here when a note with same tune
					// and channel happened during linked sounding cuz we just ignore
					// linked note length, what should be remastered. it's a rare case,
					// so low priority and i can't think how it can be done... something
					// like storing supposed time with Note in the set... i'm not sure we
					// even need this, cuz it's same as if we played a do when the do is
					// already sounding with normal lengths (it's impossible, midi would screw up)
					// TODO: move this todo to a task on github

					if (!openedLinks.contains(note)) {
						scheduler.addNoteOnTask(noteStart, note.tune.get(), note.getChannel());
						if (note.isLinkedToNext.get()) {
							openedLinks.add(note);
						}
					}

					if (!note.isLinkedToNext.get()) {
						scheduler.addNoteOffTask(noteStart.add(note.getRealLength()), note.tune.get(), note.getChannel());
						openedLinks.remove(note);
					}
				}
			});
			onAccord.accept(sumFraction);
			sumFraction = sumFraction.add(chord.getFraction());
		}

		if (openedLinks.size() > 0) {
			Logger.warning("Got unclosed linked notes: " + Arrays.toString(openedLinks.toArray()));
		}
	}

	@Deprecated // instance MAZAFAKA
	public static void resetDiminendo() {
		if (diminendoThread != null) {
			diminendoThread.interrupt();
			diminendoThread = null;
		}
		DeviceEbun.setVolume(0, 127);
	}
}