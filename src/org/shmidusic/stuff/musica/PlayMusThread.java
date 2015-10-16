package org.shmidusic.stuff.musica;

import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.stuff.midi.DeviceEbun;
import org.apache.commons.math3.fraction.Fraction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
final public class PlayMusThread {

	final private static long DIMINDENDO_STEP_TIME = Note.getTimeMilliseconds(new Fraction(1, 16), 120); // 0.0625 sec

    public static Map<Note, Thread> opentNotes = new ConcurrentHashMap<>();

	public static boolean stop = true;

	private PlayMusThread() {}

	@Deprecated // move it to Playback class
	public static void playAccord(Chord chord) {
		Playback.resetDiminendo();
		chord.getNoteSet().forEach(PlayMusThread::playNote);
		if (chord.getIsDiminendo()) {
			runDiminendoThread(chord.getShortestTime(120), 127, 0); //yeah, yeah, TODO: i should've pased here property of StaffConfig
		}
	}

	// TODO: say NO to a thread for each single Note in Playback
	public static void playNote(Note newNote)
	{
		if (!newNote.isPause() && !newNote.getIsMuted()) {
			Thread oldThread = opentNotes.get(newNote);

			if (oldThread != null) {
				oldThread.interrupt();
				try {
					oldThread.join();
				} catch (Exception e) {
					System.out.println("Не дождались");
				}
			}

			runNoteThread(newNote);
		}
    }

	public static void shutTheFuckUp() {
		opentNotes.values().stream().filter(thread -> thread.isAlive()).forEach(java.lang.Thread::interrupt);
		opentNotes.clear();
    }

	private static void runNoteThread(Note note) {
		Thread thread = new Thread(() -> {
			DeviceEbun.openNote(note);

			try { Thread.sleep(note.getTimeMilliseconds(120)); } //yeah, yeah, TODO: i should've pased here property of StaffConfig
			catch (InterruptedException e) {}

			DeviceEbun.closeNote(note);

			opentNotes.remove(note);
		});

		opentNotes.put(note, thread);
		thread.start();
	}

	private static void runDiminendoThread(int time, int from, int to) {
		Playback.diminendoThread = new Thread(() -> {
			long startMilliseconds = System.currentTimeMillis();
			long endMilliseconds = System.currentTimeMillis() + time;

			for (long curMillis = startMilliseconds; curMillis < endMilliseconds; curMillis = System.currentTimeMillis()) {
				long volume = from + (to - from) * (curMillis - startMilliseconds) / time;
				DeviceEbun.setVolume(0, (int)volume);
				try { Thread.sleep(DIMINDENDO_STEP_TIME); } catch (InterruptedException exc) { break; }
			}
		});
		Playback.diminendoThread.start();
	}
}
