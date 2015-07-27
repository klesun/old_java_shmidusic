package main;

import com.sun.deploy.util.SystemUtils;
import stuff.Midi.DeviceEbun;
import stuff.tools.Logger;

public class Main
{
	/* TODO: maybe rename midiana to something like
	 * SheetMidiMusic,
	 * SheetMidusic (and portrait of woman with snakes from head on main page)
	 * ShmiditMusic
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