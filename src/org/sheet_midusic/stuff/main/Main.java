package org.sheet_midusic.stuff.main;

//import com.sun.deploy.util.SystemUtils;
import org.sheet_midusic.stuff.Midi.DeviceEbun;
import org.sheet_midusic.stuff.tools.Logger;

public class Main
{
	/* TODO: maybe rename midiana to something like
	 * shmidusic
	 * shmidi
	 */

	public static Boolean isLinux = false;

	public static MajesticWindow window = null;

	public static void main(String[] args) {

		// linux requires hacks for awt performance (maybe it's not awt's fault, fuck you nvidia)
		Main.isLinux = (System.getProperty("os.name").equals("Linux"));

		// TODO: it would probably load faster if window was hidden
		window = new MajesticWindow();

		/** @debug */
		Logger.resetTimer("Opening Midi devices");

		DeviceEbun.openMidiDevices();

		/** @debug */
		Logger.resetTimer("\nDone\n\nInitializing window");

		window.init();
	}
}
