package Stuff.Midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import Main.Main;
import Model.Combo;


public class DeviceEbun {
	public static MidiDevice midiInputDevice = null;
	public static MidiDevice midiOutputDevice = null;

	public static Receiver theirReceiver = null;
    private static Receiver secondaryReceiver = null;

	public static void openMidiDevices() {
		DeviceEbun.openInDevice();
		DeviceEbun.openOutDevice();
	}

	private static void openInDevice() {
		System.out.println("Opening input device...");
		int count = MidiCommon.listDevicesAndExit(true, false);
		MidiCommon.listDevicesAndExit(false,true,false);
		MidiDevice.Info	info;
		if ( count > 1 ) { // if 1 - software real-time-sequencer; if 2 - + real midi device
			info = MidiCommon.getMidiDeviceInfo(1, false); // 99% cases it is the midi-port we need
			System.out.println("Selected port: " + info.getName() + " " + info.getDescription()+" "+info.toString());
			MidiDevice device = null;
			try {
				device = MidiSystem.getMidiDevice(info);
				device.open();
				device.getTransmitter().setReceiver(new DumpReceiver());

				midiInputDevice = device;
			} catch (MidiUnavailableException e) { out("Midi-Port is already being used by other program or something like that; so no midi for you today"); }
		} else {
			out(MORAL_SUPPORT_MESSAGE);
		}
	}

	// opens real midi device ONLY if was called AFTER openInDevice()!
	private static void openOutDevice() {
		MidiDevice.Info info;

        // opening emulated MIDI OUT
        info = MidiCommon.getMidiDeviceInfo("Gervill", true);
        try {
            midiOutputDevice = MidiSystem.getMidiDevice(info);
            midiOutputDevice.open();
            secondaryReceiver = theirReceiver = midiOutputDevice.getReceiver();
        } catch (MidiUnavailableException e) { out("Не отдался нам Gervill " + e); System.exit(1); }

        // opening real MIDI OUT device
		if (midiInputDevice != null) {
			info = MidiCommon.getMidiDeviceInfo(midiInputDevice.getDeviceInfo().getName(), true);
			try {
				midiOutputDevice = MidiSystem.getMidiDevice(info);
				midiOutputDevice.open();
				theirReceiver = midiOutputDevice.getReceiver();
			} catch (MidiUnavailableException e) { out("Failed to use MIDI device as OUT"); }
		}
	}

	private static void out(String strMessage) { System.out.println(strMessage); }
	private static String MORAL_SUPPORT_MESSAGE = "You kinda don't have MIDI IN device, so you can only type notas from qwerty-keyboard. Pity you.";

	// event handles

	public static void changeOutDevice(Combo combo) {
		MidiCommon.listDevicesAndExit(false, true, false);

		Receiver tmp = theirReceiver;
		theirReceiver = secondaryReceiver;
		secondaryReceiver = tmp;
		Main.window.fullscreenStaffPanel.getStaff().getConfig().syncSyntChannels();
	}
}
