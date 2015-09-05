package org.shmidusic.stuff.test;

// with current process of device opening we fail when chrome reuqestMIDIAccess()-ed, but musescore keeps working >_<

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

public class MidiPortAvailability
{
	public static void main(String[] args) {
		MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < aInfos.length; i++)
		{
			try
			{
				MidiDevice	device = MidiSystem.getMidiDevice(aInfos[i]);
				boolean		bAllowsInput = (device.getMaxTransmitters() != 0);
				boolean		bAllowsOutput = (device.getMaxReceivers() != 0);

				System.out.println("" + i + "  "
					+ (bAllowsInput?"IN ":"   ")
					+ (bAllowsOutput?"OUT ":"    ")
					+ aInfos[i].getName() + ", "
					+ aInfos[i].getVendor() + ", "
					+ aInfos[i].getVersion() + ", "
					+ aInfos[i].getDescription());
			}
			catch (MidiUnavailableException e)
			{
				System.out.println("Unavailable device");
			}
		}
	}
}
