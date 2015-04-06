package Musica;

import javax.sound.midi.*;

import Model.Combo;
import Model.Staff.Staff;
import Model.Staff.Staff.aMode;
import Model.Staff.Accord.Nota.Nota;
import Midi.DeviceEbun;
import Model.Staff.Accord.Accord;
import Model.Staff.StaffHandler;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.Set;

public class PlayMusThread extends Thread {
    public static OneShotThread[][] opentNotas = new OneShotThread[192][10];

	private StaffHandler eventHandler = null;

	public static boolean stop = true;
	public static void stopMusic(){
		stop = true;
	}
		
	public PlayMusThread(StaffHandler eventHandler){ 
		this.eventHandler = eventHandler;
	}

    @Override
    public void run() {

		// TODO: i may be a paranoic, but i definitely feel, that timing (sound) is wrong for about 20-30 milliseconds
		// like if it would count not by current time, but waiting for some time between operations
		// so i suggest do it without creating new threads. just a loop, store in array sounding notas
		// and datetimes, when they should be off and check every epsilon time... but maybe not >_<

    	stop = false;
    	aMode tmpMode = eventHandler.getContext().mode;
    	eventHandler.getContext().mode = aMode.playin;

		// for some reason has huge delay between sound and canvas repainting
		if (eventHandler.getContext().getFocusedAccord() != null) { this.playAccord(eventHandler.getContext().getFocusedAccord()); }
		int accordsLeft = eventHandler.getContext().getAccordList().size() - eventHandler.getContext().getFocusedIndex() - 1;
		Combo nextAccord = new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT);
    	for (int i = 0; stop == false && i <= accordsLeft; ++i) {
			if (eventHandler.getContext().getFocusedAccord() != null) {
				int time = eventHandler.getContext().getFocusedAccord().getShortestTime();
				try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна" + e); }
				if (stop) { break; } // fuck you i'm unicorn
			}
			eventHandler.handleKey(nextAccord);
		}
		stop = true;
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
		if (!nota.getIsMuted()) {
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
		}
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
