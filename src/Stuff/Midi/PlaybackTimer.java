package Stuff.Midi;

import Gui.ImageStorage;
import BlockSpacePkg.StaffPkg.Accord.Nota.Nota;
import BlockSpacePkg.StaffPkg.StaffConfig.StaffConfig;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaybackTimer {

	// TODO: запили лучше свой таймер и сверяй не по номеру итерации, а по системному времени !!!
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//  Понел ёпта ?
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	final private StaffConfig config;
	private Thread timerThread = null;

	private Boolean stop = false;

	// please, note that with this implementation we may have only one task per iteration. i hope it's exactly what we need
	private Map<Fraction, Runnable> tasks = new HashMap<>();

	public PlaybackTimer(StaffConfig config) {
		this.config = config;
	}

	public void addTask(Fraction fraction, Runnable task) {
		this.tasks.put(fraction, task);
	}

	public void start() {
		this.timerThread = new Thread(() -> {
			long startTime = System.currentTimeMillis();
			while (!tasks.isEmpty() && !stop) {
				long now = System.currentTimeMillis();
				Set<Fraction> keys = tasks.keySet().stream()
					.filter(f -> startTime + toMillis(f) <= now)
					.collect(Collectors.toSet());
				for (Fraction key : keys) {
					new Thread(tasks.remove(key)).start();
				}
				try { Thread.sleep(getTimerPeriod()); }
				catch (InterruptedException exc) { Logger.FYI("Playback finished"); }
			}
		});
		this.timerThread.start();
	}

	synchronized public void interrupt() {
		this.stop = true;
		if (this.timerThread != null) {
			this.timerThread.interrupt();
		}
	}

	private long toMillis(Fraction f) {
		int tempo = config.getTempo();
		return Nota.getTimeMilliseconds(f, tempo);
	}

	private long getTimerPeriod() {
		Fraction step = ImageStorage.getSmallestPossibleNotaLength().divide(3); // 3 - cuz triplet
		return toMillis(step);
	}
}