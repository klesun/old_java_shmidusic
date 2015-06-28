package Main;

import Gui.ImageStorage;
import Stuff.Midi.DeviceEbun;
import Stuff.Tools.Logger;

public class Main {

	public static MajesticWindow window = null;

	public static void main(String[] args){

		window = new MajesticWindow();

		// TODO: show these opening status messages in window while user is waiting to entertain him

		/** @debug */
		Logger.resetTimer("Starting program");

		/** @debug */
		Logger.resetTimer("Loading images from disk");

		new ImageStorage();

		/** @debug */
		Logger.resetTimer("Refreshed image sizes");

		DeviceEbun.openMidiDevices();

		/** @debug */
		Logger.resetTimer("Opened Midi devices");

		window.init();

		/** @debug */
		Logger.resetTimer("Created window");
	}
}