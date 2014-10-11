package Musica;

import javax.sound.midi.*;

import GraphTmp.DrawPanel;
import Musica.NotnyStan.aMode;
import Pointiki.Nota;
import Pointiki.Pointer;
import Pointiki.Pointerable;
import Tools.DeviceEbun;

import java.util.ArrayList;

public class playMusThread extends Thread {
    public static OneShotThread[] openNotes = new OneShotThread[192];

	boolean stop = false;
    Receiver sintReceiver = DeviceEbun.sintReceiver;
	private static NotnyStan stan;
	
	public playMusThread(NotnyStan stan){ this.stan = stan; }

	final static int msIns = 1000;
	
    @Override
    public void run() {
        int time;
        
    	DeviceEbun.stop = false;
    	aMode tmpMode = Pointer.stan.mode;
    	Pointer.stan.mode = aMode.playin;    	 
    	do {
            Pointerable tmp = Pointer.curNota;
            if (Pointer.curNota.isTriol) {
                for (int i=0;i<3;++i) {
                    time = playAccordDivided(tmp, 3);
                    try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
                    if (i==2) break; // Простите, поздно, хочу спать, лень псифсать правилллбно
                    Pointer.move(1);
                    tmp = Pointer.curNota;
                }
                continue;

            }
        	time = playAccordDivided(tmp, 1);
            try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
        } while (DeviceEbun.stop == false && Pointer.move(1));
        DeviceEbun.stop = true;
    	Pointer.stan.drawPanel.checkCam();
    	Pointer.stan.mode = tmpMode;
    }
    public static int playAccordDivided(Pointerable  ptr, int divi) {
    	if ( ptr instanceof Nota == false) return 0;
    	Nota tmp = (Nota)ptr;
    	int time = Short.MAX_VALUE;
    	while (tmp != null) {
			// убрать костыль нахуй! стан не должен появляться у этого объекта абы когда
    		if ((stan == null) || stan.getChannelFlag(tmp.channel)) playNotu(tmp, divi);
    		time = Math.min( time, (short)( msIns*tmp.cislic/tmp.znamen*4/NotnyStan.tempo*60 / divi ) );
    		// 4 - будем брать четвертную как основную, 60 - потому что темпо измеряется в ударах в минуту, а у нас секунды (вообще, даже, миллисекунды)
    		tmp = tmp.accord;
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
