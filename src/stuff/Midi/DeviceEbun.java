package stuff.Midi;

import javax.sound.midi.*;

import main.Main;
import model.Explain;
import blockspace.staff.accord.nota.Nota;
import blockspace.staff.StaffConfig.StaffConfig;
import blockspace.staff.StaffPanel;
import stuff.Musica.PlayMusThread;
import stuff.tools.Logger;


public class DeviceEbun {

	// data1 of ShortMessage.CONTROL_CHANGE
	final static int PAN = 10;
	final static int VOLUME = 7;
	final static int RESET_ALL_CONTROLLERS = 121;

	// the first byte (status)
	final private static int DRUM_NOTE_ON = 0x99;
	final private static int DRUM_NOTE_OFF = 0x89;

	final private static int DRUM_CHANNEL = 10; 	// it's not actually really a channel. we can send normal messages to channel 10
												// and they would sound with grand piano or ororgan, but i disable this channel, cuz
												// i think it's simpler for user experience, but i may be wrong

	static DeviceEbun instance = null;

	private static MidiDevice device;
	private static MidiDevice gervill;

	private static Receiver hardwareReceiver = null;
	private static Receiver softwareReceiver;
	private static Boolean isPlaybackSoftware;
	static {
		try {	gervill = MidiSystem.getMidiDevice(MidiCommon.getMidiDeviceInfo("Gervill", true));
				gervill.open();
				softwareReceiver = gervill.getReceiver();
				isPlaybackSoftware = true;
		} catch (MidiUnavailableException e) { Logger.fatal(e, "Не отдался нам Gervill "); }
	}

	public static DeviceEbun inst() {
		if (instance == null) {
			instance = new DeviceEbun();
		}
		return instance;
	}

	public static void openMidiDevices() {
		DeviceEbun.openHardwareDevice();
	}

	private static void openHardwareDevice() {
		Logger.logForUser("Opening input device...");
		int count = MidiCommon.listDevicesAndExit(true, false);
		MidiCommon.listDevicesAndExit(false, true, false);
		MidiDevice.Info	info;
		if ( count > 1 ) { // if 1 - software real-time-sequencer; if 2 - + real midi device
			info = MidiCommon.getMidiDeviceInfo(1, false); // 99% cases it is the midi-port we need
			Logger.logForUser("Selected port: " + info.getName() + " " + info.getDescription() + " " + info.toString());
			try {
				device = MidiSystem.getMidiDevice(info);
				device.open();
				device.getTransmitter().setReceiver(new DumpReceiver());

				// opening output device
				MidiDevice.Info outputInfo = MidiCommon.getMidiDeviceInfo(device.getDeviceInfo().getName(), true);
				try {
					MidiDevice midiOutputDevice = MidiSystem.getMidiDevice(outputInfo);
					midiOutputDevice.open();
					hardwareReceiver = midiOutputDevice.getReceiver();
					changeOutDevice();
				} catch (MidiUnavailableException e) { Logger.warning("Failed to use MIDI device as OUT. Its weird, cuz with input of this device everything is alright =D"); }

			} catch (MidiUnavailableException e) { Logger.logForUser("Midi-Port is already being used by other program or something like that; so no midi for you today"); }
		} else {
			Logger.logForUser(MORAL_SUPPORT_MESSAGE);
		}
	}

	public static void closeMidiDevices() {
		// close all opent Notas
		Main.window.blockSpace.getChildScrollList().stream()
				.filter(s -> s.content instanceof StaffPanel)
				.forEach(s -> ((StaffPanel) s.content).getStaff().getPlayback().interrupt());
		PlayMusThread.shutTheFuckUp();

		// close devices
		if (device != null) { device.close(); }
		if (gervill != null) { gervill.close(); }
	}

	public static Boolean isPlaybackSoftware() {
		return isPlaybackSoftware;
	}

	private static Explain changeOutDevice() {
		return hardwareReceiver != null
				? new Explain((isPlaybackSoftware = !isPlaybackSoftware) || true)
				: new Explain("Sorry. I wish i could change output device, but you have only software playback now, cuz hardware midi output device failed to load at the start of program.");
	}

	public static Receiver getPlaybackReceiver() {
		return isPlaybackSoftware ? softwareReceiver : hardwareReceiver;
	}

	private static String MORAL_SUPPORT_MESSAGE = "You kinda don't have MIDI IN device, so you can only type notas from qwerty-keyboard holding alt. Pity you.";

	// event handles

	public static Explain changeOutDevice(StaffConfig config) {
		Explain success = changeOutDevice();
		config.syncSyntChannels();
		return success;
	}

	public static void setVolume(int channel, int value) {
		sendMessage(ShortMessage.CONTROL_CHANGE, channel, VOLUME, value);
	}

	public static void openNota(Nota nota) {
		if (nota.getChannel() != DRUM_CHANNEL) {
			sendMessage(ShortMessage.NOTE_ON, nota.getChannel(), nota.tune.get(), nota.getVolume());
		} else {
			sendMessage(DRUM_NOTE_ON, nota.tune.get(), nota.getVolume());
		}
	}

	public static void closeNota(Nota nota) {
		if (nota.getChannel() != DRUM_CHANNEL) {
			sendMessage(ShortMessage.NOTE_OFF, nota.getChannel(), nota.tune.get(), 0);
		} else {
			sendMessage(DRUM_NOTE_OFF, nota.tune.get(), 0);
		}
	}

	private static void sendMessage(int status, int channel, int data1, int data2) {
		ShortMessage message = new ShortMessage();
		try { message.setMessage(status, channel, data1, data2); }
		catch (InvalidMidiDataException exc) { Logger.fatal(exc, "You are not right " + status + " " + channel + " " + data1 + " " + data2); }
		getPlaybackReceiver().send(message, -1);
	}

	private static void sendMessage(int status, int data1, int data2) {
		ShortMessage message = new ShortMessage();
		try { message.setMessage(status, data1, data2); }
		catch (InvalidMidiDataException exc) { Logger.fatal(exc, "You are not right " + status + " " + " " + data1 + " " + data2); }
		getPlaybackReceiver().send(message, -1);
	}
}
