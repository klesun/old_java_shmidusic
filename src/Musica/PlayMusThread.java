package Musica;

import javax.sound.midi.*;

import Musica.NotnyStan.aMode;
import Pointerable.IAccord;
import Pointerable.Nota;
import Pointerable.Pointer;
import Pointerable.Pointerable;
import Tools.DeviceEbun;
import Tools.KeyEventHandler;

import java.util.ArrayList;

public class PlayMusThread extends Thread {
    public static OneShotThread[] openNotes = new OneShotThread[192];

	boolean stop = false;
    Receiver sintReceiver = DeviceEbun.sintReceiver;
	private static NotnyStan stan;
	private KeyEventHandler eventHandler = null;
	
	public PlayMusThread(KeyEventHandler eventHandler, NotnyStan stan){ 
		this.eventHandler = eventHandler;
		this.stan = stan; 
	}

	final static int msIns = 1000;
	
    @Override
    public void run() {
        int time;
        
    	DeviceEbun.stop = false;
    	aMode tmpMode = Pointer.stan.mode;
    	Pointer.stan.mode = aMode.playin;    	 
    	do {
            if (Pointer.pointsAt instanceof IAccord) {
            	IAccord accord = (IAccord)Pointer.pointsAt;
//				if (nota.isTriol) {
//					for (int i=0;i<3;++i) {
//						time = playAccordDivided(nota, 3);
//						try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
//						if (i==2) break; // Простите, поздно, хочу спать, лень псифсать правилллбно
//						Pointer.move(1);
//						this.stan.drawPanel.repaint();
//						if (Pointer.pointsAt instanceof Nota) { nota = (Nota)Pointer.pointsAt; } else { System.out.println("Таки этот день настал: фантомка (или пауза) между триоли"); };
//					}
//					continue;
//				}
				time = playAccord(accord);
				this.eventHandler.albert.repaint();
				try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
			}
        } while (DeviceEbun.stop == false && Pointer.move(1));
        DeviceEbun.stop = true;
    	Pointer.stan.drawPanel.checkCam();
    	Pointer.stan.mode = tmpMode;
    }

	public static int playAccord(IAccord accord)
	{
		return playAccordDivided(accord, 1);
	}

    public static int playAccordDivided(IAccord accord, int divi) {
    	int time = Short.MAX_VALUE;
    	for (Nota tmp: accord.getNotaList()) {
			// убрать костыль нахуй! стан не должен появляться у этого объекта абы когда
    		if ((stan == null) || stan.getChannelFlag(tmp.channel)) playNotu(tmp, divi);
    		time = Math.min( time, (short)( msIns*tmp.numerator/NotnyStan.DEFAULT_ZNAM*4/NotnyStan.tempo*60 / divi ) );
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
