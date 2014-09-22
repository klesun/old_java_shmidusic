package Test;

import javax.sound.midi.*;
import BackEnd.MidiCommon;

public class MidiNote
{
	private static boolean DEBUG = true;
	public static void main(String[] args)
	{
		String	strDeviceName = "CMI8738 [hw:1,0,0]";
		int	nChannel = 15;
		int nKey = 76;
		int nVelocity = 76;
		int nDuration = 3000;

		MidiDevice outputDevice = null;
		Receiver receiver = null;
		if (strDeviceName != null) {
			MidiDevice.Info	info = MidiCommon.getMidiDeviceInfo(strDeviceName, true);
			try	{ (outputDevice = MidiSystem.getMidiDevice(info)).open(); } catch (MidiUnavailableException e) { if (DEBUG) out(e); }
			try	{ receiver = outputDevice.getReceiver(); } catch (MidiUnavailableException e) { if (DEBUG) out(e); }  }
		
		ShortMessage instrMess = null;
		ShortMessage onMessage = null;
		ShortMessage offMessage = null;
		try	{
			instrMess = new ShortMessage();
			onMessage = new ShortMessage();
			offMessage = new ShortMessage();
			instrMess.setMessage(ShortMessage.PROGRAM_CHANGE, nChannel, 16, 0);
			receiver.send(instrMess, -1);
			onMessage.setMessage(ShortMessage.NOTE_ON, nChannel, nKey, nVelocity);
			offMessage.setMessage(ShortMessage.NOTE_OFF, nChannel, nKey, 0);
		} catch (InvalidMidiDataException e) { if (DEBUG) { out(e); }  }

		receiver.send(onMessage, -1);
		try	{ Thread.sleep(nDuration); } catch (InterruptedException e)	{ if (DEBUG) out(e); }
		receiver.send(offMessage, -1);

		receiver.close();
		if (outputDevice != null) outputDevice.close();
	}

	private static void out(Throwable t) { t.printStackTrace(); }
}