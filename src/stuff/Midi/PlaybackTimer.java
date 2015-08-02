package stuff.Midi;

import com.google.common.collect.Sets;
import gui.ImageStorage;
import blockspace.staff.accord.nota.Nota;
import blockspace.staff.StaffConfig.StaffConfig;
import stuff.musica.Klesunthesizer;
import stuff.tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import stuff.tools.jmusic_integration.INota;

import java.util.*;
import java.util.stream.Collectors;

public class PlaybackTimer implements IMidiScheduler {

	final private StaffConfig config;
	private Thread timerThread = null;

	private Boolean stop = false;

	// please, note that with this implementation we may have only one task per iteration. i hope it's exactly what we need
	private Map<Fraction, List<Runnable>> tasks = new HashMap<>();

	public PlaybackTimer(StaffConfig config) {
		this.config = config;
	}

	public void addNoteTask(Fraction when, INota nota) {
		addTask(when, () -> DeviceEbun.openNota(nota));
		addTask(when.add(nota.getRealLength()), () -> DeviceEbun.closeNota(nota));
	}

	public void addTask(Fraction fraction, Runnable task)
	{
		if (!tasks.containsKey(fraction)) {
			tasks.put(fraction, new ArrayList<>());
		}
		this.tasks.get(fraction).add(task);
	}

	// adds task right after last with delta gap
	public void appendTask(Fraction delta, Runnable task) {
		addTask(Collections.max(tasks.keySet()).add(delta), task);
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
					List<Runnable> taskList = tasks.remove(key);
					new Thread(() -> taskList.forEach(Runnable::run)).start();
				}

				if (tasks.size() > 0) {

					Fraction nextOn = Collections.min(tasks.keySet());

					long sleepAnother = toMillis(nextOn) - System.currentTimeMillis();

					if (sleepAnother > 0) {
						try { Thread.sleep(sleepAnother); }
						catch (InterruptedException exc) { Logger.FYI("Playback finished"); }
					}
				}
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

	protected long toMillis(Fraction f) {
		int tempo = config.getTempo();
		return Nota.getTimeMilliseconds(f, tempo);
	}

	// it's bad
	public static class KlesunthesizerTimer extends PlaybackTimer
	{
		public KlesunthesizerTimer(StaffConfig config) {
			super(config);
		}

		@Override
		public void addNoteTask(Fraction when, INota nota) {
			addTask(when, () -> Klesunthesizer.send(nota.getTune(), (int)toMillis(nota.getRealLength())));
		}
	}
}