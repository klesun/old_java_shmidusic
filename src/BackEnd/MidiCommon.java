package BackEnd;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;



/** Utility methods for MIDI examples.
 */
public class MidiCommon
{
    /**	TODO:
     todo: flag long
     */
    public static int listDevicesAndExit(boolean bForInput,
                                          boolean bForOutput)
    {
        return listDevicesAndExit(bForInput, bForOutput, false);
    }



    public static int listDevicesAndExit(boolean bForInput,
                                          boolean bForOutput,
                                          boolean bVerbose)
    {
    	int count = 0;
        if (bForInput && !bForOutput)
        {
            out("Available MIDI IN Devices:");
        }
        else if (!bForInput && bForOutput)
        {
            out("Available MIDI OUT Devices:");
        }
        else
        {
            out("Available MIDI Devices:");
        }

        MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < aInfos.length; i++)
        {
            try
            {
                MidiDevice	device = MidiSystem.getMidiDevice(aInfos[i]);
                boolean		bAllowsInput = (device.getMaxTransmitters() != 0);
                boolean		bAllowsOutput = (device.getMaxReceivers() != 0);
                if ((bAllowsInput && bForInput) ||
                        (bAllowsOutput && bForOutput))
                {
                    if (bVerbose)
                    {
                        out("" + i + "  "
                                + (bAllowsInput?"IN ":"   ")
                                + (bAllowsOutput?"OUT ":"    ")
                                + aInfos[i].getName() + ", "
                                + aInfos[i].getVendor() + ", "
                                + aInfos[i].getVersion() + ", "
                                + aInfos[i].getDescription());
                    }
                    else
                    {
                        out("" + i + "  " + aInfos[i].getName());
                        ++count;
                    }
                }
            }
            catch (MidiUnavailableException e)
            {
                // device is obviously not available...
                // out(e);
            }
        }
        if (aInfos.length == 0)
        {
            out("[No devices available]");
        }
        return count;
    }

    public static MidiDevice.Info getMidiDeviceInfo(String strDeviceName, boolean bForOutput)
    {
        MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < aInfos.length; i++)
        {
            if (aInfos[i].getName().equals(strDeviceName))
            {
                try
                {
                    MidiDevice device = MidiSystem.getMidiDevice(aInfos[i]);
                    boolean	bAllowsInput = (device.getMaxTransmitters() != 0);
                    boolean	bAllowsOutput = (device.getMaxReceivers() != 0);
                    if ((bAllowsOutput && bForOutput) || (bAllowsInput && !bForOutput))
                    {
                        return aInfos[i];
                    }
                }
                catch (MidiUnavailableException e)
                {
                    // TODO:
                }
            }
        }
        return null;
    }
    
    public static MidiDevice.Info getMidiDeviceInfo(int number, boolean bForOutput)
    {
        MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < aInfos.length; i++)
        {
            if (i == number)
            {
                try
                {
                    MidiDevice device = MidiSystem.getMidiDevice(aInfos[i]);
                    boolean	bAllowsInput = (device.getMaxTransmitters() != 0);
                    boolean	bAllowsOutput = (device.getMaxReceivers() != 0);
                    if ((bAllowsOutput && bForOutput) || (bAllowsInput && !bForOutput))
                    {
                        return aInfos[i];
                    }
                }
                catch (MidiUnavailableException e)
                {
                    // TODO:
                }
            }
        }
        return null;
    }

    private static void out(String strMessage)
    {
        System.out.println(strMessage);
    }
}