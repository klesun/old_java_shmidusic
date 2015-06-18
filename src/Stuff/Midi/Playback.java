package Stuff.Midi;

import Storyspace.Staff.Accord.Accord;

public class Playback {

	static Playback instance = null;

	public Thread diminendoThread = null;

	public static Playback inst() {
		if (instance == null) {
			instance = new Playback();
		}
		return instance;
	}

	public void resetDiminendo() {
		if (diminendoThread != null) {
			diminendoThread.interrupt();
			this.diminendoThread = null;
		}
		DeviceEbun.setVolume(127);
	}
}
