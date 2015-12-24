package org.shmidusic.stuff.musica;

import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.Main;
import org.klesun_model.Explain;
import org.shmidusic.sheet_music.staff.Staff;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.midi.DeviceEbun;
import org.shmidusic.stuff.midi.IMidiScheduler;
import org.shmidusic.stuff.midi.SmfScheduler;
import org.shmidusic.stuff.tools.INote;

import java.util.function.Consumer;

public class Playback {

	@Deprecated // instance MAZAFAKA
	public static Thread diminendoThread = null;

	final private StaffComponent staffComp;
	private PlaybackTimer runningProcess = null;

	public Playback(StaffComponent staffComp) {
		this.staffComp = staffComp;
	}

	public void trigger() {
		interrupt();
		if (this.runningProcess == null) {
			play();
		}
	}

	public Boolean interrupt() {
		if (this.runningProcess != null) {
			this.runningProcess.interrupt();
			this.runningProcess = null;
		}
		DeviceEbun.closeAllNotes();
		return true;
	}

	private Explain play() {
		Staff staff = staffComp.staff;
		if (!staff.getChordList().isEmpty()) {
			if (runningProcess != null) { interrupt(); }
			if (staff.getConfig().useHardcoreSynthesizer.get()) {
				// TODO: try to do it with two PlaybackTimer-s - one for music and other for repaint requests
				runningProcess = new PlaybackTimer.KlesunthesizerTimer(staff.getConfig());
			} else {
				runningProcess = new PlaybackTimer(staff.getConfig());
			}

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

		for (Chord chord : staffComp.staff.getChordList().subList(startFrom, staffComp.staff.getChordList().size())) {
			final Fraction finalStart = sumFraction;

			chord.noteStream(n -> true).forEach(n -> playNote(n, finalStart, scheduler));
			onAccord.accept(sumFraction);
			sumFraction = sumFraction.add(chord.getFraction());
		}
	}

	private static void playNote(INote note, Fraction start, IMidiScheduler scheduler)
	{
		if (note.getTune() != 0) { // 0 means pause in my world
			if (!Main.isLinux || scheduler instanceof SmfScheduler || true) { /** @debug */
				scheduler.addNoteTask(start, note);
			} else {
				// making sound lag a bit, so it fitted lagging graphics ^_^
				// TODO: maybe move this hack into preferences with parameter one day...
				scheduler.addNoteTask(start.add(new Fraction(1, 16)), note);
			}
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