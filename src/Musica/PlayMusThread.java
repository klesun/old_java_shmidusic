package Musica;

import Model.Combo;
import Model.Staff.Staff;
import Model.Staff.Accord.Nota.Nota;
import Model.Staff.Accord.Accord;

import java.awt.event.KeyEvent;

import java.util.ArrayList;

public class PlayMusThread extends Thread {
    public static OneShotThread[][] opentNotas = new OneShotThread[192][10];

	private Staff staff = null;

	public static boolean stop = true;
	public static void stopMusic(){
		stop = true;
	}
		
	public PlayMusThread(Staff staff) {
		this.staff = staff;
	}

    @Override
    public void run() {

		// TODO: i may be a paranoic, but i definitely feel, that timing (sound) is wrong for about 20-30 milliseconds
		// like if it would count not by current time, but waiting for some time between operations
		// so i suggest do it without creating new threads. just a loop, store in array sounding notas
		// and datetimes, when they should be off and check every epsilon time... but maybe not >_<

    	stop = false;

		// for some reason has huge delay between sound and canvas repainting
		if (staff.getFocusedAccord() != null) { this.playAccord(staff.getFocusedAccord()); }
		int accordsLeft = staff.getAccordList().size() - staff.getFocusedIndex() - 1;
		Combo nextAccord = new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_RIGHT);
    	for (int i = 0; stop == false && i <= accordsLeft; ++i) {
			if (staff.getFocusedAccord() != null) {
				int time = staff.getFocusedAccord().getShortestTime();
				try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна" + e); }
				if (stop) { break; }
			}
			staff.getHandler().handleKey(nextAccord);
		}

		stop = true;
    }

	public static void playAccord(Accord accord) { accord.getNotaList().forEach(PlayMusThread::playNotu); }
    
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
