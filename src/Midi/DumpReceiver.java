package Midi;

import Model.Containers.Panels.MusicPanelHandler;

import	javax.sound.midi.MidiMessage;
import	javax.sound.midi.ShortMessage;
import	javax.sound.midi.Receiver;

public class DumpReceiver implements Receiver {
	
	public static MusicPanelHandler eventHandler;
	
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
			// Handle instrument change/pitch-bend/tune/etc // Actually, useless
		}
	}
}
