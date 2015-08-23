package org.shmidusic.stuff.Midi;

import org.shmidusic.Main;
import org.shmidusic.sheet_music.staff.chord.nota.Nota;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.stuff.musica.Klesunthesizer;
import org.shmidusic.stuff.tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.tools.jmusic_integration.INota;

import javax.swing.*;
import javax.swing.Timer;
import java.util.*;
import java.util.stream.Collectors;

public class PlaybackTimer implements IMidiScheduler {

	final private StaffConfig config;
	private Thread timerThread = null;

	private Boolean stop = false;

	// please, note that with this implementation we may have only one task per iteration. i hope it's exactly what we need
	private TreeMap<Fraction, List<Runnable>> tasks = new TreeMap<>();

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
		if (tasks.keySet().size() > 0) {
			addTask(Collections.max(tasks.navigableKeySet()).add(delta), task);
		}
	}

	public void start() {
		this.timerThread = new Thread(() -> {
			long startTime = System.currentTimeMillis();
			iterate(startTime);
		});
		this.timerThread.start();
	}

	private void iterate(long startTime)
	{
		if (!stop) {

			long now = System.currentTimeMillis();

			Fraction from = new Fraction(0);
			Fraction to = tasks.navigableKeySet().stream().filter(f -> startTime + toMillis(f) > now).findFirst().orElse(new Fraction(Integer.MAX_VALUE));
			Set<Fraction> keys = tasks.navigableKeySet().subSet(from, to);

			for (Fraction key : keys) {
				List<Runnable> taskList = tasks.remove(key);
				taskList.forEach(Runnable::run);
			}

			if (tasks.size() > 0) {

				Fraction nextOn = Collections.min(tasks.navigableKeySet());

				int sleepAnother = (int) (startTime + toMillis(nextOn) - System.currentTimeMillis());
				sleepAnother = sleepAnother > 0 ? sleepAnother : 0;

				Timer swingTimer = new Timer(sleepAnother, a -> iterate(startTime));
				swingTimer.setRepeats(false);
				swingTimer.start(); // be carefull. make sure tasks will be emptied eventually!
			}
		}
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