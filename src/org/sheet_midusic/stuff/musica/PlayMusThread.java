package org.sheet_midusic.stuff.musica;

import org.klesun_model.Combo;
import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.chord.nota.Nota;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.stuff.Midi.DeviceEbun;
import org.sheet_midusic.stuff.Midi.Playback;
import org.apache.commons.math3.fraction.Fraction;

import java.awt.event.KeyEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
final public class PlayMusThread {

	final private static long DIMINDENDO_STEP_TIME = Nota.getTimeMilliseconds(new Fraction(1, 16), 240); // 0.0625 sec

    public static Map<Nota, Thread> opentNotas = new ConcurrentHashMap<>();

	public static boolean stop = true;

	private PlayMusThread() {}

	@Deprecated // move it to Playback class
	public static void playAccord(Chord chord) {
		Playback.resetDiminendo();
		chord.getNotaSet().forEach(PlayMusThread::playNotu);
		if (chord.getIsDiminendo()) {
			runDiminendoThread(chord.getShortestTime(), 127, 0);
		}
	}

	// TODO: say NO to a thread for each single Nota in Playback
	public static void playNotu(Nota newNota){

		Nota oldNota = opentNotas.keySet().stream().filter(k -> k.equals(newNota)).findAny().orElse(null);
		Thread oldThread = opentNotas.get(newNota);

		if (oldThread == null || oldNota.linkedTo() != newNota) {

			if (oldThread != null) {
				oldThread.interrupt();
				try { oldThread.join(); }
				catch (Exception e) { System.out.println("Не дождались"); }
			}

			runNotaThread(newNota);

		} else if (oldNota.linkedTo() == newNota) {
			// updating key with new nota
			opentNotas.remove(newNota); // it won't overwrite key otherwise
			opentNotas.put(newNota, oldThread);
		}
    }

	public static void shutTheFuckUp() {
		opentNotas.values().stream().filter(thread -> thread.isAlive()).forEach(java.lang.Thread::interrupt);
		opentNotas.clear();
    }

	private static void runNotaThread(Nota nota) {
		Thread thread = new Thread(() -> {
			DeviceEbun.openNota(nota);

			try { Thread.sleep(nota.getTimeMilliseconds(true)); }
			catch (InterruptedException e) {}

			DeviceEbun.closeNota(nota);

			opentNotas.remove(nota);
		});

		opentNotas.put(nota, thread);
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
