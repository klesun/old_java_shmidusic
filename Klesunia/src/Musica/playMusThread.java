package Musica;

import javax.sound.midi.*;

import GraphTmp.DrawPanel;
import Musica.NotnyStan.aMode;
import Tools.MyPointer;
import Tools.Pointerable;

public class playMusThread extends Thread {
	boolean stop = false;
	MyPointer ptr;
    Receiver sintReceiver = NotnyStan.sintReceiver;
    DrawPanel Albert;
    NotnyStan stan;
	
	public playMusThread(NotnyStan stan){
		ptr = stan.ptr;
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
    		Pointerable nota = ptr.curNota;          
    		Pointerable tmp = nota;        	    		        	
        	time = playAccord(tmp);        
            try { Thread.sleep(time); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
        } while (stan.stop == false && ptr.move(1) == 0);   
    	stan.drawPanel.checkCam();
    	stan.mode = tmpMode;
    }
    public static int playAccord(Pointerable  ptr) {
    	if ( ptr instanceof Nota == false) return 0;
    	Nota tmp = (Nota)ptr;
    	int time = Short.MAX_VALUE;
    	while (tmp != null) {
    		playNotu(tmp);
    		time = Math.min( time, (short)( msIns*tmp.durCislic/tmp.durZnamen*4/NotnyStan.tempo*60 ) );
    		// 4 - будем брать четвертную как основную, 60 - потому что темпо измеряется в ударах в минуту, а у нас секунды (вообще, даже, миллисекунды)
    		tmp = tmp.accord;
    	}
    	return time;
    	
    }
    
    public static int playNotu(Nota nota){
    	// ��������� ���� � ������ ����
    	OneShotThread thr;
    	thr = new OneShotThread(nota);
    	thr.start();
    	return 0;
    }
}
