package Stuff.Midi;

import Gui.ImageStorage;
import Model.ActionResult;
import Model.Combo;
import Storyspace.Staff.Accord.Accord;
import Storyspace.Staff.Accord.Nota.Nota;
import Storyspace.Staff.Staff;
import Storyspace.Staff.StaffConfig.StaffConfig;
import Stuff.Musica.PlayMusThread;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;

import java.awt.event.KeyEvent;

public class Playback {

	@Deprecated // instance MAZAFAKA
	public static Thread diminendoThread = null;

	final Staff staff;

	public Playback(Staff staff) {
		this.staff = staff;
	}

	// just an alias to shorten call in action manager
	public static ActionResult<PlaybackTimer> playStaff(Staff staff) {
		return new Playback(staff).play();
	}

	public ActionResult<PlaybackTimer> play() {
		if (!staff.getAccordList().isEmpty()) {
			PlaybackTimer timer = new PlaybackTimer(getTimerPeriod());
			long currentAccordIteration = 0;
			int startFrom = staff.getFocusedIndex() == -1 ? 0 : staff.getFocusedIndex();
			for (Accord accord : staff.getAccordList().subList(startFrom, staff.getAccordList().size())) {
				timer.addTask(currentAccordIteration, () -> {
					staff.getHandler().handleKey(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT));
					// i dunno why, but image lags
//					staff.moveFocusWithPlayback(1, false);
//					staff.getParentSheet().repaint();
				});
				currentAccordIteration += toTimerIterations(accord.getShortestFraction());
			}
			timer.start();
			return new ActionResult<>(timer); // returning it so it could be interrupted from another action
		} else {
			return new ActionResult<>("Staff is empty");
		}
	}

	// TODO: Maybe one day we could also put Nota closing into timer too
	private int getTimerPeriod() {
		int tempo = staff.getConfig().getTempo();
		return Nota.getTimeMilliseconds(getTimerStepFraction(), tempo); // 1/16
	}

	private static int toTimerIterations(Fraction fraction) {
		Fraction result = fraction.divide(getTimerStepFraction());
		if (result.getDenominator() != 1) {
			Logger.fatal("got Nota length that cant be divided to our smallest measure unit [" + fraction + "] NOW WAI!");
			return -100;
		} else {
			return result.intValue();
		}
	}

	private static Fraction getTimerStepFraction() {
		return ImageStorage.getSmallestPossibleNotaLength().divide(3); // 3 - cuz triplet
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