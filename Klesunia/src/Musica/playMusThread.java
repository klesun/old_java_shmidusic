package Musica;

import javax.sound.midi.*;

import GraphTmp.DrawPanel;
import Musica.NotnyStan.aMode;
import Tools.Pointer;
import Tools.Pointerable;

public class playMusThread extends Thread {
	boolean stop = false;
	Pointer ptr;
    Receiver sintReceiver = NotnyStan.sintReceiver;
    DrawPanel Albert;
    NotnyStan stan;
	
	public playMusThread(NotnyStan stan){
		Albert = stan.drawPanel;
		this.stan = stan;
	}

	final static int msIns = 1000;
	
    @Override
    public void run() {
        int time;
        
    	stan.stop = false;
    	aMode tmpMode = stan.mode;
    	stan.mode = aMode.playin;    	 
    	do {
            Pointerable tmp = ptr.curNota;
            if (ptr.curNota.isTriol) {
                for (int i=0;i<3;++i) {
                    time = playAccordDivided(tmp, 3);
                    try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
                    if (i==2) break; // Простите, поздно, хочу спать, лень псифсать правилллбно
                    ptr.move(1);
                    tmp = ptr.curNota;
                }
                continue;

            }
        	time = playAccordDivided(tmp, 1);
            try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
        } while (stan.stop == false && ptr.move(1) == 0);
        stan.stop = true;
    	stan.drawPanel.checkCam();
    	stan.mode = tmpMode;
    }
    public static int playAccordDivided(Pointerable  ptr, int divi) {
    	if ( ptr instanceof Nota == false) return 0;
    	Nota tmp = (Nota)ptr;
    	int time = Short.MAX_VALUE;
    	while (tmp != null) {
    		playNotu(tmp, divi);
    		time = Math.min( time, (short)( msIns*tmp.cislic/tmp.znamen*4/NotnyStan.tempo*60 / divi ) );
    		// 4 - будем брать четвертную как основную, 60 - потому что темпо измеряется в ударах в минуту, а у нас секунды (вообще, даже, миллисекунды)
    		tmp = tmp.accord;
    	}
    	return time;
    	
    }
    
    public static int playNotu(Nota nota, int divi){
    	OneShotThread thr;
    	thr = new OneShotThread(nota, divi);
    	thr.start();
    	return 0;
    }
}
