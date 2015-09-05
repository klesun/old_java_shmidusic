
package org.shmidusic.stuff.test;

import org.shmidusic.sheet_music.staff.chord.nota.Nota;
import org.shmidusic.stuff.midi.MidiCommon;
import org.shmidusic.stuff.tools.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class PitchBendAndVolumeTest {
	
	static MidiDevice midiDevice = null;
	static Receiver theirReceiver = null;
	
	static int PAN = 10;
	static int VOLUME = 7;
	static int RESET_ALL_CONTROLLERS = 121;
	
	public static void main(String[] args) {
		openOutDevice();

		testCrescendo();

		midiDevice.close();
		theirReceiver.close();
	}

	private static void testDrums() {
		sendMessage(0x99, 0x23, 0x40);
		try { Thread.sleep(1000); } catch (InterruptedException exc) { Logger.fatal(exc, "zhopa"); }
		sendMessage(0x99, 0x23, 0x00);
	}

	private static void testCrescendo() {
		int dt = 64;
		int[][] huj = {
			{74, 16},
			{72, 16},
			{71, 16},
			{69, 16},
			{68, 32},
			{69, 16},
			{71, 16},
			{72, 64},
		};

		setInstrument(81);

		int tune = b(Nota.TI);
		int tune2 = b(Nota.MI) + Nota.OCTAVA;

		resetAllControllers();

		openNota(tune, 127);
		try { Thread.sleep(1000); } catch (InterruptedException exc) {}

		for (int i = 0; i < 4; ++i) {
			doCrescendo(0, 127, 1000);
		}

		closeNota(tune);
		openNota(tune2, 63);

		try { Thread.sleep(1000); } catch (InterruptedException exc) {}
		resetAllControllers();
		closeNota(tune2);
	}

	private static int b(int tune) {
		return tune - 1;
	}

	private static void doCrescendo(int from, int to, int timeMili) {
		if (from >= to) { return; }

		int dt = timeMili / (to - from);
		
		for (int i = from; i < to; ++i) {
			try { Thread.sleep(dt); } catch (InterruptedException exc) {}
			setVolume(i);
		}
	}
	
	private static void closeNota(int n) {
		sendMessage(ShortMessage.NOTE_OFF, 0, n, 0);
	}
	
	private static void closeAllNotas() {
		for (int i = 36; i < 128; ++i) {
			closeNota(i);
		}
	}
	
	private static void openNota(int n, int volume) {
		sendMessage(ShortMessage.NOTE_ON, 0, n, volume);
	}
	
	private static void setVibrato(int n) {
		sendMessage(ShortMessage.CHANNEL_PRESSURE, 0, n, 0);
	}
	
	private static void setInstrument(int n) {
		sendMessage(ShortMessage.PROGRAM_CHANGE, 0, n, 0);
	}

	// ShortMessage.CONTROL_CHANGE
	
	
	private static void resetAllControllers() {
		sendMessage(ShortMessage.CONTROL_CHANGE, 0, RESET_ALL_CONTROLLERS, 0);
	}
	
	private static void setPan(int n) {
		sendMessage(ShortMessage.CONTROL_CHANGE, 0, PAN, n);
	}
	
	private static void setVolume(int n) {
		sendMessage(ShortMessage.CONTROL_CHANGE, 0, VOLUME, n);
	}
	
	private static void sendMessage(int status, int channel, int data1, int data2) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(status, channel, data1, data2);
		} catch (InvalidMidiDataException exc) { System.out.println("Zhopa " + status + " " + data1 + " " + data2);
		}
		theirReceiver.send(message, -1);
	}

	private static void sendMessage(int status, int data1, int data2) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(status, data1, data2);
		} catch (InvalidMidiDataException exc) { System.out.println("Zhopa " + status + " " + data1 + " " + data2); }
		theirReceiver.send(message, -1);
	}
	    
	public static int openOutDevice() {
		MidiCommon.listDevicesAndExit(true, true, true);
		MidiDevice.Info info = MidiCommon.getMidiDeviceInfo(2, true);
		
    	try {
    		midiDevice = MidiSystem.getMidiDevice(info);
    		midiDevice.open();
            theirReceiver = midiDevice.getReceiver();
        } catch (MidiUnavailableException e) { e.printStackTrace(); }

        return 0;
    }
}
