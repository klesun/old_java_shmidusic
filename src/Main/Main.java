package Main;

import Gui.ImageStorage;
import Stuff.Midi.DeviceEbun;
import Stuff.Tools.Logger;

import java.io.*;
public class Main {

	public static MajesticWindow window = null;

	public static void main(String[] args){

		window = new MajesticWindow();

		// TODO: show these opening status messages in window while user is waiting to entertain him

		/** @debug */
		Logger.resetTimer("Starting program");

		/** @debug */
		Logger.resetTimer("Loaded images from disk");

		ImageStorage.inst().refreshImageSizes();

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