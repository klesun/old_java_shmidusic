package stuff.Midi;

import main.Main;
import model.Explain;
import blockspace.staff.accord.Accord;
import blockspace.staff.Staff;
import org.apache.commons.math3.fraction.Fraction;
import stuff.tools.jmusic_integration.INota;

import java.util.function.Consumer;

public class Playback {

	@Deprecated // instance MAZAFAKA
	public static Thread diminendoThread = null;

	final private Staff staff;
	private PlaybackTimer runningProcess = null;

	public Playback(Staff staff) {
		this.staff = staff;
	}

	public void trigger() {
		if (this.runningProcess != null) { interrupt(); }
		else { play(); }
	}

	public Boolean interrupt() {
		if (this.runningProcess != null) {
			this.runningProcess.interrupt();
			this.runningProcess = null;
			DeviceEbun.closeAllNotas();
		}
		return true;
	}

	private Explain play() {
		if (!staff.getAccordList().isEmpty()) {
			if (runningProcess != null) { interrupt(); }
			runningProcess = new PlaybackTimer(staff.getConfig());

			staff.moveFocus(-1);
			int startFrom = staff.getFocusedIndex() + 1;

			streamTo(runningProcess, startFrom, now -> runningProcess.addTask(now, () ->
			{
				staff.moveFocus(1);
				staff.getParentSheet().checkCam();
			}));

			runningProcess.appendTask(new Fraction(1), this::interrupt);
			runningProcess.start();
			return new Explain(true);
		} else {
			return new Explain(false, "staff is empty");
		}
	}

	public void streamTo(IMidiScheduler scheduler) {
		streamTo(scheduler, 0, f -> {}); // TODO: maybe better null
	}

	private void streamTo(IMidiScheduler scheduler, int startFrom, Consumer<Fraction> onAccord)
	{
		Fraction sumFraction = new Fraction(0);

		for (Accord accord: staff.getAccordList().subList(startFrom, staff.getAccordList().size())) {
			final Fraction finalStart = sumFraction;

			accord.notaStream(n -> true).forEach(n -> playNota(n, finalStart, scheduler));
			onAccord.accept(sumFraction);
			sumFraction = new Fraction(sumFraction.doubleValue() + accord.getFraction().doubleValue());
		}
	}

	private static void playNota(INota nota, Fraction start, IMidiScheduler scheduler)
	{
		if (!Main.isLinux) {
			scheduler.addNoteOnTask(start, nota);
			scheduler.addNoteOffTask(start.add(nota.getRealLength()), nota);
		} else {
			// making sound lag a bit, so it fitted lagging graphics ^_^
			// TODO: maybe move this hack into preferences with parameter one day...
			scheduler.addNoteOnTask(start.add(new Fraction(1, 16)), nota);
			scheduler.addNoteOffTask(start.add(new Fraction(1, 16)).add(nota.getRealLength()), nota);
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