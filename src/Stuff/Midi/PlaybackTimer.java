package Stuff.Midi;

import Stuff.Tools.Logger;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class PlaybackTimer {
	private long iteration = 0;
	// TODO: запили лучше свой таймер и сверяй не по номеру итерации, а по системному времени !!!
	final private HardcoreTimer timer;

	// please, note that with this implementation we may have only one task per iteration. i hope it's exactly what we need
	private Map<Long, Runnable> tasks = new HashMap<>();

	public PlaybackTimer(int period) {
		this.timer = new HardcoreTimer(period, this::onTimer);
	}

	public void addTask(long iteration, Runnable task) {
		this.tasks.put(iteration, task);
	}

	public void start() {
		timer.start();
	}

	public void interrupt() {
		timer.stop();
	}

	// it still lags. i'm afraid swing timer callss events one after another, not on time or something synchronizes 'em => slows
	private void onTimer() {
		final long iteration = incrementIteration();
		if (tasks.isEmpty() || tasks.keySet().stream().noneMatch(t -> t >= iteration)) {
			interrupt();
		} else {
			if (tasks.containsKey(iteration)) {
				// BUAHAHAHAHA
				new Thread(tasks.get(iteration)).start();
				tasks.remove(iteration);
			}
		}
	}

	synchronized private long incrementIteration() {
		return this.iteration++;
	}

	// it sleeps in current thread >D
	private class HardcoreTimer {

		final private Runnable onTimer;
		final private int period;
		private Thread runningThread = null;

		private Boolean stop = false;

		public HardcoreTimer(int period, Runnable onTimer) {
			this.period = period;
			this.onTimer = onTimer;
		}

		public void start() {
			this.runningThread = new Thread(this::runIteration);
			this.runningThread.start();
		}

		// will be called in the lambda in runIteration()... likely
		synchronized public void stop() {
			this.stop = true;
			if (this.runningThread != null) {
				this.runningThread.interrupt();
			}
		}

		synchronized private Boolean wasStopped() {
			return this.stop;
		}

		private void runIteration() {
			onTimer.run();
			try { Thread.sleep(period); }
			catch (InterruptedException exc) { System.out.println("zhopa s jajcami"); }
			if (!wasStopped()) {
				// i think, just saying him, how much iterations it will take in constructor would be MUUUUUCH better...
				runIteration();
			}
		}
	}
}