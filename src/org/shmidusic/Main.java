package org.shmidusic;

//import com.sun.deploy.util.SystemUtils;
import org.shmidusic.stuff.midi.DeviceEbun;
import org.shmidusic.stuff.tools.Logger;

public class Main
{
	/* TODO: maybe rename midiana to something like
	 * midiana
	 * sheet-midusic
	 * shmidusic
	 * shmidi
	 */

	public static Boolean isLinux = false;
	public static MajesticWindow window = null;

	public static void main(String[] args)
	{
		// TODO: i'm tired of those non-final properties - do things through instance!

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
