package Main;

import Gui.ImageStorage;
import Stuff.Midi.DeviceEbun;
import Stuff.Tools.Logger;

import java.io.*;
public class Main {

	public static MajesticWindow window = null;

	public static void main(String[] args){

		/** @debug */
		resetTimer("Starting program");

		/** @debug */
		resetTimer("Loaded images from disk");

		ImageStorage.inst().refreshImageSizes();

		/** @debug */
		resetTimer("Refreshed image sizes");

		DeviceEbun.openMidiDevices();

		/** @debug */
		resetTimer("Opened Midi devices");

		window = new MajesticWindow();

		/** @debug */
		resetTimer("Created window");

		window.setVisible(true);

		/** @debug */
		resetTimer("Made window visible");
	}

	/** @debug */
	private static Long time = null;

	/** @debug */
	private static void resetTimer(String msg) {
		if (time == null) {
			time = System.nanoTime();
		}

		long newTime = System.nanoTime();
		System.out.println("==== " + (newTime - time)/1e9 + " - " + msg);
		time = newTime;
	}
}