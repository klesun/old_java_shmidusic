package org.shmidusic.stuff.midi;

import javax.sound.midi.*;

import org.klesun_model.Explain;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.stuff.tools.Logger;
import org.shmidusic.stuff.tools.INote;

import java.util.TreeSet;


public class DeviceEbun {

	// data1 of ShortMessage.CONTROL_CHANGE
	final static int PAN = 10;
	final static short CONTROL_CHANGE_VOLUME = 7;
	final static int RESET_ALL_CONTROLLERS = 121;

	// the first byte (status)
	final private static int DRUM_NOTE_ON = 0x99;
	final private static int DRUM_NOTE_OFF = 0x89;

	final private static int DRUM_CHANNEL = 10; 	// it's not actually really a channel. we can send normal messages to channel 10
	// and they would sound with grand piano or ororgan, but i disable this channel, cuz
	// i think it's simpler for user experience, but i may be wrong

	static DeviceEbun instance = null;

	// TODO: It's very bad that we have one set for both devices. Try pressing ctrl-d while something is sounding, it will be fun ^_^.
	private static TreeSet<INote> openNoteSet = new TreeSet<>();

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

				if (outputInfo != null) {

					try {
						MidiDevice midiOutputDevice = MidiSystem.getMidiDevice(outputInfo);
						midiOutputDevice.open();
						hardwareReceiver = midiOutputDevice.getReceiver();
						changeOutDevice();
					} catch (MidiUnavailableException e) {
						Logger.warning("Failed to use MIDI device as OUT. Its weird, cuz with input of this device everything is alright =D");
					}

				} else {
					// TODO: do things not 4erez zhopu here
					Logger.logForUser("Sorry. I dunno why, but your first MIDI IN device name does not match any MIDI OUT devices." + "" +
						" You can type sheet music with your device, but you wont be able to playback it to it");
				}

			} catch (MidiUnavailableException e) { Logger.logForUser("Midi-Port is already being used by other program or something like that; so no midi for you today"); }
		} else {
			Logger.logForUser(MORAL_SUPPORT_MESSAGE);
		}
	}

	public static void closeMidiDevices() {
		// close all opened Notes
		DeviceEbun.closeAllNotes();

		// close devices
		if (device != null) {
			device.close();
		}
		if (gervill != null) { gervill.close(); }
	}

	public static Boolean isPlaybackSoftware() {
		return isPlaybackSoftware;
	}

	private static Explain changeOutDevice() {
		return hardwareReceiver != null
			? new Explain((isPlaybackSoftware = !isPlaybackSoftware) || true)
			: new Explain(false, "Sorry. I wish i could change output device, but you have only software playback now, cuz hardware midi output device failed to load at the start of program.");
	}

	public static Receiver getPlaybackReceiver() {
		return isPlaybackSoftware ? softwareReceiver : hardwareReceiver;
	}

	private static String MORAL_SUPPORT_MESSAGE = "You kinda don't have MIDI IN device, so you can only type notes from qwerty-keyboard holding alt. Pity you.";

	// event handles

	public static Explain changeOutDevice(StaffConfig config) {
		Explain success = changeOutDevice();
		config.syncSyntChannels();
		return success;
	}

	public static void setVolume(int channel, int value) {
		sendMessage(ShortMessage.CONTROL_CHANGE, channel, CONTROL_CHANGE_VOLUME, value);
	}

	public static void setInstrument(int channel, int value) {
		sendMessage(ShortMessage.PROGRAM_CHANGE, channel, value, 0);
	}

	synchronized public static void openNote(INote note)
	{
		if (openNoteSet.contains(note)) {
			closeNote(note);
		}

		openNoteSet.add(note);
		if (note.getChannel() != DRUM_CHANNEL) {
			sendMessage(ShortMessage.NOTE_ON, note.getChannel(), note.getTune(), 127);
		} else {
			sendMessage(DRUM_NOTE_ON, note.getTune(), 127);
		}
	}

	// TODO: when a note was opened more than one time: blink
	// sounding and don't close till all are closed (like on the site)
	synchronized public static void closeNote(INote note)
	{
		openNoteSet.remove(note);
		if (note.getChannel() != DRUM_CHANNEL) {
			sendMessage(ShortMessage.NOTE_OFF, note.getChannel(), note.getTune(), 0);
		} else {
			sendMessage(DRUM_NOTE_OFF, note.getTune(), 0);
		}
	}

	synchronized public static void closeAllNotes() {
		// we do it through new set to avoid concurrent modification
		new TreeSet<>(openNoteSet).forEach(DeviceEbun::closeNote);
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
