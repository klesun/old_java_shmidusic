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
    public static OneShotThread[] openNotes = new OneShotThread[192];

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
    	do {
            if (eventHandler.getContext().getFocusedAccord() != null) {
				time = eventHandler.getContext().getFocusedAccord().getShortest().getTimeMiliseconds();
//            	Accord accord = eventHandler.getContext().getFocusedAccord();
//				time = playAccord(accord);
				try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
			}
        } while (DeviceEbun.stop == false && eventHandler.getContext().moveFocus(1));
        DeviceEbun.stop = true;
    	eventHandler.getContext().parentSheetMusic.checkCam();
    	eventHandler.getContext().mode = tmpMode;
    }

	public static int playAccord(Accord accord)
	{
		return playAccordDivided(accord, 1);
	}

    public static int playAccordDivided(Accord accord, int divi) {
		int minute = 60 * 1000;
    	int time = Short.MAX_VALUE;
    	for (Nota tmp: accord.getNotaList()) {
			// убрать костыль нахуй! стан не должен появляться у этого объекта абы когда
    		if ((stan == null) || stan.getChannelFlag(tmp.channel)) playNotu(tmp, divi);
    		time = Math.min( time, (short)( minute*tmp.numerator/Staff.DEFAULT_ZNAM*4/Staff.tempo / divi ) );
    		// 4 - будем брать четвертную как основную, 60 - потому что темпо измеряется в ударах в минуту, а у нас секунды (вообще, даже, миллисекунды)
    	}
    	return time;
    	
    }
    
    public static int playNotu(Nota nota, int divi){
        int tune = nota.tune;
        if (openNotes[tune] != null) {
            openNotes[tune].interrupt();
            try {openNotes[tune].join();} catch (Exception e) {System.out.println("Не дождались");}
            openNotes[tune] = null;
        }
    	OneShotThread thr = new OneShotThread(nota, divi);
        openNotes[tune] = thr;
        kri4alki.add(openNotes[tune]);
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
