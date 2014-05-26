import BackEnd.DumpReceiver;
import BackEnd.MidiCommon;
import GraphTmp.GraphMusica;
import Musica.NotnyStan;

import javax.sound.midi.*;

import java.io.*;

public class Main {
	
	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
    public static void main(String[] args){
    	System.out.println("Hello");
    	int count = MidiCommon.listDevicesAndExit(true, false);
    	MidiDevice.Info	info;
    	if ( count > 1 ) {
    		info = MidiCommon.getMidiDeviceInfo(1, false);
    	} else {
	    	System.out.println("Основываясь на информации выше, введите номер любимого Миди-устройства");
	    	System.out.println(
	    			"(Если там только " +
	    			"Real Time Sequencer, ну что сказать, мне вас жаль. Убедитесь, что у вас в компе есть Миди-порт (" +
	    			"можете побаловаться с Bios, мне один раз помогло (В биосе стояло MPU-301, я поменял на MPU-401 и " +
	    			"всё заработало) ) или " +
	    			"аудио-карта с миди портом (на неё нужны драйвера, как правило, они точно есть на Windows XP). Ну," +
	    			"и конечно же, да прибудет с вами Гугл");
	    	
	    	String str = "1";
	    	try {
	            str = br.readLine();
	            // Сделать здесь что-нибудь
	        } catch (IOException ex) {System.out.println("Ошибка ввода!");}
	    	info = MidiCommon.getMidiDeviceInfo(Integer.parseInt(str), false);
    	}
        MidiDevice	device = null;
        try { 	device = MidiSystem.getMidiDevice(info);
            	device.open(); }
        catch (MidiUnavailableException e) { 
        	System.out.println("Жопа короче");
        }
    	
        NotnyStan stan = new NotnyStan(device);            

        Receiver r = null;
        r = new DumpReceiver(stan);
        try { Transmitter t = device.getTransmitter();
            t.setReceiver(r); }
        catch (MidiUnavailableException e) { out("В жопу трансмиттер устройства к ресиверу не подключается:");
            out(e);
            device.close();
            System.exit(1); }
        out("now running; type \"exit\" to exit");

        GraphMusica app = new GraphMusica(stan); 
        app.setVisible(true); 

        //System.out.println("Ща считаю");
        //MainProcessor mainProcessor = new MainProcessor(device, r, app, stan);
        //mainProcessor.begin();
             
    }

    private static void out(String strMessage) {
        System.out.println(strMessage);
    }

    private static void out(Throwable t) {
        t.printStackTrace();
    }


}
