package Musica;

import Model.Staff;
import javax.sound.midi.*;

import Model.Staff.aMode;
import Model.Accord.Nota.Nota;
import Midi.DeviceEbun;
import Model.Accord.Accord;
import Model.StaffHandler;

import java.util.ArrayList;

public class PlayMusThread extends Thread {
    public static OneShotThread[] openNotas = new OneShotThread[192];

	boolean stop = false;
    Receiver sintReceiver = DeviceEbun.sintReceiver;
	private static Staff stan;
	private StaffHandler eventHandler = null;
	
	public PlayMusThread(StaffHandler eventHandler){ 
		this.eventHandler = eventHandler;
		this.stan = eventHandler.getContext(); // избавься от этого статичного стана наконец!
	}

	final static int msIns = 1000;
	
    @Override
    public void run() {
        int time;
        
    	DeviceEbun.stop = false;
    	aMode tmpMode = eventHandler.getContext().mode;
    	eventHandler.getContext().mode = aMode.playin; 
		eventHandler.getContext().moveFocus(0);
    	do {
            if (eventHandler.getContext().getFocusedAccord() != null) {
				time = eventHandler.getContext().getFocusedAccord().getShortest().getTimeMiliseconds();
				try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
			}
        } while (DeviceEbun.stop == false && eventHandler.getContext().moveFocus(1));
        DeviceEbun.stop = true;
    	eventHandler.getContext().parentSheetMusic.checkCam();
    	eventHandler.getContext().mode = tmpMode;
    }

	public static void playAccord(Accord accord)
	{
    	for (Nota tmp: accord.getNotaList()) {
    		if (accord.parentStaff.getChannelFlag(tmp.channel)) {
				playNotu(tmp);
			}
    	}    	
    }
    
    public static int playNotu(Nota nota){
        int tune = nota.tune;
        if (openNotas[tune] != null) {
            openNotas[tune].interrupt();
            try {openNotas[tune].join();} catch (Exception e) {System.out.println("Не дождались");}
            openNotas[tune] = null;
        }
    	OneShotThread thr = new OneShotThread(nota);
        openNotas[tune] = thr;
        kri4alki.add(openNotas[tune]);
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
