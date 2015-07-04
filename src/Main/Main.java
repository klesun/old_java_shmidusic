package Main;

import Gui.ImageStorage;
import Stuff.Midi.DeviceEbun;
import Stuff.Tools.Logger;

public class Main {

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