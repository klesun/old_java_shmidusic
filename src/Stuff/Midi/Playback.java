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

	final private Staff staff;
	private PlaybackTimer runningProcess = null;

	public Playback(Staff staff) {
		this.staff = staff;
	}

	public void trigger() {
		if (this.runningProcess != null) { interrupt(); }
		else { play(); }
	}

	public void interrupt() {
		if (this.runningProcess != null) {
			this.runningProcess.interrupt();
			this.runningProcess = null;
		}
	}

	private ActionResult<PlaybackTimer> play() {
		if (!staff.getAccordList().isEmpty()) {
			if (runningProcess != null) { interrupt(); }
			runningProcess = new PlaybackTimer(staff.getConfig());
			Fraction sumFraction = new Fraction(0);

			staff.moveFocus(-1);
			int startFrom = staff.getFocusedIndex() + 1;
			for (Accord accord : staff.getAccordList().subList(startFrom, staff.getAccordList().size())) {
				runningProcess.addTask(sumFraction, () -> {
					staff.moveFocusWithPlayback(1, false);
					staff.getParentSheet().checkCam();
				});
				sumFraction = sumFraction.add(accord.getShortestFraction());
			}
			runningProcess.addTask(sumFraction.add(1), this::interrupt);
			runningProcess.start();
			return new ActionResult<>(runningProcess); // returning it so it could be interrupted from another action
		} else {
			return new ActionResult<>("Staff is empty");
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