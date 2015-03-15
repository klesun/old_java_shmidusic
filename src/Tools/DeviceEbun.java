package Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import Midi.DumpReceiver;
import Midi.MidiCommon;
import Musica.NotnyStan;
import static Musica.NotnyStan.CHANNEL;
import Musica.PlayMusThread;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;


public class DeviceEbun {
	public static MidiDevice MidiInputDevice; 
	public static MidiDevice MidiOutputDevice;
	
	public static boolean openInDevice(KeyEventHandler eventHandler) {
		int count = MidiCommon.listDevicesAndExit(true, false);
        MidiCommon.listDevicesAndExit(false,true,false);
    	MidiDevice.Info	info;
    	if ( count > 1 ) {
    		info = MidiCommon.getMidiDeviceInfo(1, false);
    	} else {
	    	out("Nash ekstrasens ne ugadal vase MIDI-ustrojstvo. Poazlujsta, vvedite ego nomer iz spiska vise");
	    	out(MORAL_SUPPORT_MESSAGE);
	    	
	    	String str = "1";
	    	try {
	            str = br.readLine();
	            // Сделать здесь что-нибудь
	        } catch (IOException ex) {System.out.println("Ошибка ввода!");}
	    	info = MidiCommon.getMidiDeviceInfo(Integer.parseInt(str), false);
    	}
		System.out.println(info.getName()+" "+info.getDescription()+" "+info.toString());
        MidiDevice device = null;
        try { 	device = MidiSystem.getMidiDevice(info);
            	device.open(); }
        catch (MidiUnavailableException e) { 
        	out("Osibka, vi dolzni bili vvesti nomer MIDI IN ustrojstva, a vveli kakuju-to byaku");
        }

        Receiver r = null;
        r = new DumpReceiver(eventHandler);
        try { Transmitter t = device.getTransmitter();
            t.setReceiver(r); }
        catch (MidiUnavailableException e) { out("В жопу трансмиттер устройства к ресиверу не подключается:");
            out(e);
            device.close();
            System.exit(1); }
        
        MidiInputDevice = device;
        
        return true;
    }
	
    public static boolean stop = true;
	public static Receiver sintReceiver = null;
    public static void stopMusic(){
        stop = true;
    }
	
    private static Receiver secondaryReceiver = null;
    
	public static int openOutDevice(){
		MidiDevice.Info	info = MidiCommon.getMidiDeviceInfo(MidiInputDevice.getDeviceInfo().getName(), true);
    	try {
    		MidiOutputDevice = MidiSystem.getMidiDevice(info);
    		MidiOutputDevice.open();
            sintReceiver = MidiOutputDevice.getReceiver();
        } catch (MidiUnavailableException e) {
            out("Не открывается аут ваш"); }
        if (sintReceiver == null) {  out("Не отдался нам ресивер");
                                    System.exit(1); }
        secondaryReceiver = sintReceiver;
        
        info = MidiCommon.getMidiDeviceInfo("Gervill", true);
        try {
        	MidiOutputDevice = MidiSystem.getMidiDevice(info);
            MidiOutputDevice.open();
            sintReceiver = MidiOutputDevice.getReceiver();
        } catch (MidiUnavailableException e) {
			out("Не отдался нам Gervill " + e);
        }

        return 0;
    }
	
    public static void changeOutDevice() {
        Receiver tmp = sintReceiver;
        sintReceiver = secondaryReceiver;
        secondaryReceiver = tmp;
    }

	public static void changeInstrument(int instrument) throws InvalidMidiDataException
	{
		ShortMessage instrMess = new ShortMessage();
		instrMess.setMessage(ShortMessage.PROGRAM_CHANGE, CHANNEL, instrument, 0);
		sintReceiver.send(instrMess, -1);
	}

    private static void out(String strMessage) {
        System.out.println(strMessage);
    }

    private static void out(Throwable t) {
        t.printStackTrace();
    }
    
    private static String MORAL_SUPPORT_MESSAGE = 
			"(Если там только " +
			"Real Time Sequencer, ну что сказать, мне вас жаль. Убедитесь, что у вас в компе есть Миди-порт (" +
			"можете побаловаться с Bios, мне один раз помогло (В биосе стояло MPU-301, я поменял на MPU-401 и " +
			"всё заработало) ) или " +
			"аудио-карта с миди портом (на неё нужны драйвера, как правило, они точно есть на Windows XP). Ну," +
			"и конечно же, да прибудет с вами Гугл";
    
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
}
