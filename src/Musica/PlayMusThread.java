package Musica;

import Model.Staff;
import javax.sound.midi.*;

import Model.Staff.aMode;
import Model.Accord.Nota.Nota;
import Midi.DeviceEbun;
import Model.Accord.Accord;
import Model.StaffHandler;
import java.awt.event.KeyEvent;

import java.util.ArrayList;

public class PlayMusThread extends Thread {
    public static OneShotThread[][] opentNotas = new OneShotThread[192][10];

	boolean stop = false;
    Receiver sintReceiver = DeviceEbun.sintReceiver;
	private StaffHandler eventHandler = null;
		
	public PlayMusThread(StaffHandler eventHandler){ 
		this.eventHandler = eventHandler;
	}

    @Override
    public void run() {
        int time;
        
    	DeviceEbun.stop = false;
    	aMode tmpMode = eventHandler.getContext().mode;
    	eventHandler.getContext().mode = aMode.playin; 
		eventHandler.getContext().moveFocus(0);
		int accordsLeft = eventHandler.getContext().getAccordList().size() - eventHandler.getContext().getFocusedIndex() - 1;
    	for (int i = 0; i <= accordsLeft && DeviceEbun.stop == false; ++i) {
			KeyEvent generatedEvent = new KeyEvent(eventHandler.getContext().parentSheetMusic, 0, 0, KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT, 'h'); // держалки держать не будет
			eventHandler.handleKey(generatedEvent);
            if (eventHandler.getContext().getFocusedAccord() != null) {
				time = eventHandler.getContext().getFocusedAccord().getShortest().getTimeMiliseconds();
				try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
			}
			eventHandler.getContext().parentSheetMusic.repaint(); // some hack, cause i wanna it to be precise with sound
			eventHandler.getContext().parentSheetMusic.parentWindow.keyHandler.shouldRepaint = false;
        }
        DeviceEbun.stop = true;
    	eventHandler.getContext().parentSheetMusic.checkCam();
    	eventHandler.getContext().mode = tmpMode;
    }

	public static void playAccord(Accord accord)
	{
    	for (Nota tmp: accord.getNotaList()) {
    		if (accord.getParentStaff().getChannelFlag(tmp.channel)) {
				playNotu(tmp);
			}
    	}    	
    }
    
    public static int playNotu(Nota nota){
		int channel = nota.channel;
        int tune = nota.tune;
        if (opentNotas[tune][channel] != null) {
            opentNotas[tune][channel].interrupt();
            try {opentNotas[tune][channel].join();} catch (Exception e) {System.out.println("Не дождались");}
            opentNotas[tune][channel] = null;
        }
    	OneShotThread thr = new OneShotThread(nota);
        opentNotas[tune][channel] = thr;
        kri4alki.add(opentNotas[tune][channel]);
    	thr.start();
    	return 0;
    }

    public static ArrayList<OneShotThread> kri4alki = new ArrayList<OneShotThread>();
    public static void shutTheFuckUp() {
        for (OneShotThread tmp: kri4alki) {
            if (tmp.isAlive()) tmp.interrupt();
        }
        kri4alki.removeAll(kri4alki);
    }
}
