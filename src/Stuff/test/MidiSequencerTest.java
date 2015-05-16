package Stuff.test;

import javax.sound.midi.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.*;

public class MidiSequencerTest {

	public static void main(String[] args) throws MidiUnavailableException {

//		playMidiExample();

		getMidiDevices();

//		try { recordAudioFromMidi(); }
//		catch (Exception e) { die(e); }
	}

	private static void getMidiDevices() throws MidiUnavailableException {
		MidiDevice.Info[] infoList = MidiSystem.getMidiDeviceInfo();

		for (int i=0; i < infoList.length; i++) {
			MidiDevice.Info deviceInfo = infoList[i];
			log("");
			log(i + ") " + deviceInfo.getName());
			log("Description: " + deviceInfo.getDescription());

			MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);
			log("Device: " + device);
			// короче, открываешь SoftSynthesizer
			// делаешь ему openStream()
			// ?????
			// profit!
		}
	}

	private static void recordAudioFromMidi() throws Exception {
		// preparing to record audio
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, getAudioFormat());
		if (!AudioSystem.isLineSupported(info)) {
			System.out.println("Line matching blablabla not supported");
			return;
		}

		TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
		targetLine.open(getAudioFormat(), targetLine.getBufferSize());

		// Create an in-memory output stream and
		// initial buffer to hold our samples
		ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		int frameSizeInBytes = getAudioFormat().getFrameSize();
		int bufferLengthInFrames = targetLine.getBufferSize() / 8;
		int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
		byte[] data = new byte[bufferLengthInBytes];

		// recording audio

		targetLine.start();

//		while (isRecording()) {
//			int numBytesRead = targetLine.read(data, 0, frameSizeInBytes);
//			if (numBytesRead == -1) { break; 	}
//			getOutputStream().write(data, 0,
//				numBytesRead);
//			}
//		targetLine.stop();
//
//		// flush and close the output stream
//		try {
//			getOutputStream().flush();
//			getOutputStream().close();
//		} catch (IOException e) { die(e);}
	}

	// copypasted from http://www2.sys-con.com/itsg/virtualcd/java/archives/0811/gorman/index.html
	// MIDI & Audio Sequencing with Java
	private static AudioFormat getAudioFormat() {
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		int rate = 44100;
		int sampleSize = 16;
		int channels = 1;
		boolean bigEndian = true;

		return new AudioFormat(encoding, rate, sampleSize, channels,
				(sampleSize / 8) * channels, rate, bigEndian);
	}

	private static void playMidiExample() throws MidiUnavailableException {
		Sequencer sequencer = null;
		sequencer = MidiSystem.getSequencer();
		System.out.println(sequencer.getClass().getName());

		InputStream inputStream = null;
		try { inputStream = new BufferedInputStream(new FileInputStream(new File("/home/klesun/progas/midiana/elfenLied.mid"))); }
		catch (FileNotFoundException e) { die(e); }

		sequencer.open();
		try { sequencer.setSequence(inputStream); }
		catch (IOException e) { e.printStackTrace(); Runtime.getRuntime().exit(666); }
		catch (InvalidMidiDataException e) { die(e); }

		// plays music
		sequencer.start();
		while (sequencer.isRunning()) {
			try { Thread.sleep(1000); }
			catch (InterruptedException e) { die(e); }
		}
		sequencer.close();
	}

	// useful functions

	private static void log(String str) {
		System.out.println(str);
	}

	private static void die(Exception e) {
		e.printStackTrace(); Runtime.getRuntime().exit(666);
	}
}
