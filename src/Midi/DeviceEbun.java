package Midi;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import static Model.Staff.Staff.CHANNEL;

import Gui.KeyEventHandler;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;


public class DeviceEbun {
	public static MidiDevice midiInputDevice = null;
	public static MidiDevice midiOutputDevice = null;

	public static Receiver theirReceiver = null;
    private static Receiver secondaryReceiver = null;

	public static void openInDevice(KeyEventHandler eventHandler) {
		System.out.println("Opening input device...");
		int count = MidiCommon.listDevicesAndExit(true, false);
		MidiCommon.listDevicesAndExit(false,true,false);
		MidiDevice.Info	info;
		if ( count > 1 ) {
			info = MidiCommon.getMidiDeviceInfo(1, false); // 99% cases it is the midi-port we need
			System.out.println("Selected port: " + info.getName() + " " + info.getDescription()+" "+info.toString());
			MidiDevice device = null;
			try {
				device = MidiSystem.getMidiDevice(info);
				device.open();
				device.getTransmitter().setReceiver(new DumpReceiver(eventHandler));

				midiInputDevice = device;
			} catch (MidiUnavailableException e) { out("Port is already being used by other program or something like that"); }
		} else {
			out(MORAL_SUPPORT_MESSAGE);
		}
	}

	public static void openOutDevice() {
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

	public static void changeOutDevice() {
		Receiver tmp = theirReceiver;
		theirReceiver = secondaryReceiver;
		secondaryReceiver = tmp;
	}

	private static void out(String strMessage) { System.out.println(strMessage); }
	private static String MORAL_SUPPORT_MESSAGE = "You kinda don't have MIDI IN device, so you can only type notas from qwerty-keyboard. Pity you.";
	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
}
