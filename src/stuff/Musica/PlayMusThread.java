package stuff.Musica;

import model.Combo;
import blockspace.staff.Staff;
import blockspace.staff.accord.nota.Nota;
import blockspace.staff.accord.Accord;
import stuff.Midi.DeviceEbun;
import stuff.Midi.Playback;
import org.apache.commons.math3.fraction.Fraction;

import java.awt.event.KeyEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayMusThread extends Thread {

	final private static long DIMINDENDO_STEP_TIME = Nota.getTimeMilliseconds(new Fraction(1, 16), 240); // 0.0625 sec

    public static Map<Nota, Thread> opentNotas = new ConcurrentHashMap<>();

	private Staff staff = null;

	public static boolean stop = true;
	public static void stopMusic(){
		stop = true;
	}
		
	public PlayMusThread(Staff staff) {
		this.staff = staff;
	}

    @Deprecated // use Playback::playStaff() instead
    public void run() {

		// TODO: i may be a paranoic, but i definitely feel, that timing (sound) is wrong for about 20-30 milliseconds
		// like if it would count not by current time, but waiting for some time between operations
		// so i suggest do it without creating new threads. just a loop, store in array sounding notas
		// and datetimes, when they should be off and check every epsilon time... but maybe not >_<

    	stop = false;

		// for some reason has huge delay between sound and canvas repainting
		if (staff.getFocusedAccord() != null) { this.playAccord(staff.getFocusedAccord()); }
		int accordsLeft = staff.getAccordList().size() - staff.getFocusedIndex() - 1;
    	for (int i = 0; stop == false && i <= accordsLeft; ++i) {
			if (staff.getFocusedAccord() != null) {
				int time = staff.getFocusedAccord().getShortestTime();
				try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна" + e); }
				if (stop) { break; }
			}

			staff.getHandler().handleKey(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT));
		}

		stop = true;
    }

	@Deprecated // move it to Playback class
	public static void playAccord(Accord accord) {
		Playback.resetDiminendo();
		accord.getNotaSet().forEach(PlayMusThread::playNotu);
		if (accord.getIsDiminendo()) {
			runDiminendoThread(accord.getShortestTime(), 127, 0);
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
