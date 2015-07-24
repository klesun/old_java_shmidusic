package stuff.Midi;

import model.Explain;
import blockspace.staff.accord.Accord;
import blockspace.staff.Staff;
import org.apache.commons.math3.fraction.Fraction;
import stuff.tools.jmusic_integration.INota;

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
		}
		return true;
	}

	private Explain play() {
		if (!staff.getAccordList().isEmpty()) {
			if (runningProcess != null) { interrupt(); }
			runningProcess = new PlaybackTimer(staff.getConfig());
			Fraction sumFraction = new Fraction(0);

			staff.moveFocus(-1);
			int startFrom = staff.getFocusedIndex() + 1;
			for (Accord accord : staff.getAccordList().subList(startFrom, staff.getAccordList().size())) {

				playAccord(accord, sumFraction, runningProcess);

				runningProcess.addTask(sumFraction, () -> {
					staff.moveFocus(1);
//					staff.moveFocusWithPlayback(1, false);
					staff.getParentSheet().checkCam();
				});
				sumFraction = new Fraction(sumFraction.doubleValue() + accord.getFraction().doubleValue());
			}
			runningProcess.addTask(sumFraction.add(1), this::interrupt);
			runningProcess.start();
			return new Explain(true);
		} else {
			return new Explain(false, "staff is empty");
		}
	}

	private static void playAccord(Accord accord, Fraction start, PlaybackTimer scheduler)
	{
		accord.notaStream(n -> true).forEach(n -> playNota(n, start, scheduler));
	}

	private static void playNota(INota nota, Fraction start, PlaybackTimer scheduler)
	{
		DeviceEbun.openNota(nota);

		try { Thread.sleep(nota.getTimeMilliseconds(true)); }
		catch (InterruptedException e) {}

		DeviceEbun.closeNota(nota);

		opentNotas.remove(nota);
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