package Pointiki;

import Musica.*;

public class Pointer {
	public static boolean SOUND_ON = true;
	public static boolean SOUND_OFF = false;
	
    public static int pos;
    public static int gpos = 0;
    public static NotnyStan stan = null;
    public static Pointerable curNota;
    public static Phantom beginNota;    
    public static int init(NotnyStan newStan){
        pos = -1;
        stan = newStan;
        curNota = stan.phantomka;
        return 0;
    }
    
    public static int moveOut(){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	pos = -1;
    	curNota = beginNota;
    	return 0;
    }
    
    public static int moveToBegin(){
        stan.checkValues(stan.phantomka);
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	curNota.underPtr = false;
    	pos = 0;    	
    	gpos = 0;
    	if (beginNota.next == null) return -1;
    	curNota = beginNota.next;
    	curNota.underPtr = true;
        return 0;
    }
    public static int moveToEnd(){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	curNota.underPtr = false;
    	while (move(1) != false);
    	curNota.underPtr = true;
        return 0;
    }
    
    public static int moveTo(int q){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	if ((q >= stan.noshuCount) || (q < -1)) {System.out.println("Stack overflow"); return -1;}    	
    	 
    	moveToBegin();
    	move(q);

        return 0;
    }
    public static int moveTo(Pointerable nota){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	moveToBegin();
    	while (nota != curNota  &&  move(1));
    	if (nota != curNota) return -1;
    	else return 0;
    }
    
    public static boolean move(int q) {
        return moveBo(q, false);
    }
    
    public static boolean move(int q, boolean tru) {
        return moveBo(q, tru);
    }
    
    public static boolean moveSis(int n) { // TODO: logic mistake... somewhere here
    	int stepCount = n * stan.drawPanel.stepInOneSys;
    	while (stepCount > 0) {
    		stepCount -= curNota.gsize*2;
    		moveRealtime(1, SOUND_OFF);
    	}
    	while (stepCount < 0) {
    		moveRealtime(-1, SOUND_OFF);
    		stepCount += curNota.gsize*2;
    	}
    	return true;
    }
    
    public static boolean moveRealtime(int q, boolean shouldISound){
    	while (q < 0) {
			if (curNota == null) return false;
			move(-1, shouldISound);
			q += 1;
    	}
    	while (q > 0) {
			if (curNota == null) return false;
			move(1, shouldISound);
			q -= 1;
    	}
    	return true;
    }
    
    public static boolean moveBo(int q, boolean withSound){
    	nNotiVAccorde = -1;
        pointsOneNotaInAccord = false;
    	
    	if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return false;
        }        
    	if ((pos+q >= stan.noshuCount) || (pos+q < -1)) return false;
        int delta=0;        
    	pos += q;
    	
        curNota.underPtr = false;
        while (q > 0) {
        	delta+=curNota.gsize;
        	curNota = curNota.next;
        	--q;
        }
        while (q < 0) {        	
        	curNota = curNota.prev;
        	delta-=curNota.gsize;
        	++q;
        }                
        gpos += delta;
        curNota.underPtr = true;
		accordinaNota = null;
        if (curNota instanceof Phantom) stan.checkValues((Phantom)curNota);

        
        if (withSound) playMusThread.playAccordDivided(curNota, 1);
        //stan.drawPanel.checkCam();
        stan.drawPanel.repaint();
		System.out.print(curNota.toString());
        return true;
    }
    
    public static boolean isAfter(Pointerable iskomajaNota){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return false;
        }
    	for ( Pointerable tmp = curNota; tmp != null; tmp = tmp.next ) {
    		if (tmp == iskomajaNota) return false; 
    	}
    	return true;
    }
    
    public static int nNotiVAccorde = -1;					// -1 - весь аккорд
    public static boolean pointsOneNotaInAccord = false;	
    public static Nota accordinaNota = null;				// Типа нота, на которую сейчас указывает Поинтер (не аккорд!)
    public static void resetAcc() {
		accordinaNota = null;
    	Pointer.nNotiVAccorde = -1;
	}
	public static void nextAcc() {    
    	if (curNota instanceof Nota == false) {
    		if (curNota instanceof Phantom) ((Phantom)curNota).chooseNextParam();
    		return;
    	}
    	if (nNotiVAccorde >= 0){
    		if (accordinaNota.accord != null) {
    			accordinaNota = accordinaNota.accord;
    			++Pointer.nNotiVAccorde;
    		} else { 
    			resetAcc();
    		}
    	} else {
    		pointsOneNotaInAccord = true;
    		accordinaNota = (Nota)Pointer.curNota;
    		nNotiVAccorde = 0;
    	}
    }

	public static Nota getCurrentAccordinuNotu() {
		return accordinaNota;
	}

}
