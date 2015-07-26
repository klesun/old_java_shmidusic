package main;

import stuff.Midi.DeviceEbun;
import stuff.tools.Logger;

public class Main
{
	/* TODO: maybe rename midiana to something like
	 * SheetMidiMusic,
	 * SheetMidusic (and portrait of woman with snakes from head on main page)
	 * ShmiditMusic
	 */

	public static MajesticWindow window = null;

	public static void main(String[] args){

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