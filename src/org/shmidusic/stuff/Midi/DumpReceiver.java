package org.shmidusic.stuff.Midi;

import org.shmidusic.staff.StaffHandler;
import org.shmidusic.stuff.tools.Logger;

import	javax.sound.midi.MidiMessage;
import	javax.sound.midi.ShortMessage;
import	javax.sound.midi.Receiver;
import java.util.Arrays;

public class DumpReceiver implements Receiver {

	// TODO: make it SheeMusicHandler and final
	public static StaffHandler eventHandler;
	
	public DumpReceiver() {}
	
	public void close() {}

	public void send(MidiMessage message, long timestamp) {
		timestamp /= 1000; // from microseconds to milliseconds as i can judge

		Integer tune = ((ShortMessage) message).getData1();
	    int forca = ((ShortMessage)message).getData2();
	
	    if (tune > 32 && tune < 100) { // maybe 128 ?
			if (this.eventHandler != null) {
				this.eventHandler.handleMidiEvent( tune, forca, (int)timestamp );
			}
	    } else {
			Logger.warning("Ignored midi-message: [" + Arrays.toString(message.getMessage()) +  "]");
		}
	}
}
