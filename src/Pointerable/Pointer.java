package Pointerable;

import Musica.*;

public class Pointer {
	public static boolean SOUND_ON = true;
	public static boolean SOUND_OFF = false;
	
    public static int pos;
    public static int gpos = 0;
    public static NotnyStan stan = null;
    public static Pointerable pointsAt;
    public static Phantom beginNota;    
    public static int init(NotnyStan newStan){
        pos = -1;
        stan = newStan;
        pointsAt = stan.phantomka;
        return 0;
    }
    
    public static int moveOut(){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	pos = -1;
    	pointsAt = beginNota;
    	return 0;
    }
    
    public static int moveToBegin(){
        stan.checkValues(stan.phantomka);
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	pointsAt.underPtr = false;
    	pos = 0;    	
    	gpos = 0;
    	if (beginNota.next == null) return -1;
    	pointsAt = beginNota.next;
    	pointsAt.underPtr = true;
        return 0;
    }
    public static int moveToEnd(){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	pointsAt.underPtr = false;
    	while (move(1) != false);
    	pointsAt.underPtr = true;
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
    	while (nota != pointsAt  &&  move(1));
    	if (nota != pointsAt) return -1;
    	else return 0;
    }
    
    public static boolean move(int q) {
        return move(q, false);
    }
    
    public static boolean moveSis(int n) { // TODO: logic mistake... somewhere here
    	int stepCount = n * stan.drawPanel.stepInOneSys;
    	while (stepCount > 0) {
    		stepCount -= pointsAt.getWidth() * 2;
    		moveRealtime(1, SOUND_OFF);
    	}
    	while (stepCount < 0) {
    		moveRealtime(-1, SOUND_OFF);
    		stepCount += pointsAt.getWidth() * 2;
    	}
    	return true;
    }
    
    public static boolean moveRealtime(int q, boolean shouldISound){
    	while (q < 0) {
			if (pointsAt == null) return false;
			move(-1, shouldISound);
			q += 1;
    	}
    	while (q > 0) {
			if (pointsAt == null) return false;
			move(1, shouldISound);
			q -= 1;
    	}
    	return true;
    }
    
    public static boolean move(int q, boolean withSound){
    	nNotiVAccorde = -1;
        pointsOneNotaInAccord = false;
    	
    	if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return false;
        }        
    	if ((pos+q >= stan.noshuCount) || (pos+q < -1)) return false;
        int delta=0;        
    	pos += q;
    	
        pointsAt.underPtr = false;
        while (q > 0) {
        	delta += pointsAt.getWidth();
        	pointsAt = pointsAt.next;
        	--q;
        }
        while (q < 0) {        	
        	pointsAt = pointsAt.prev;
        	delta -= pointsAt.getWidth();
        	++q;
        }                
        gpos += delta;
        pointsAt.underPtr = true;
		accordinaNota = null;
        if (pointsAt instanceof Phantom) {
			stan.checkValues((Phantom)pointsAt);
		} else if (withSound && pointsAt instanceof Nota) {
			PlayMusThread.playAccord((Nota)pointsAt);
		}
        //stan.drawPanel.checkCam();
        stan.drawPanel.repaint();
        return true;
    }
    
    public static boolean isAfter(Pointerable iskomajaNota){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return false;
        }
    	for ( Pointerable tmp = pointsAt; tmp != null; tmp = tmp.next ) {
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
    	if (pointsAt instanceof Nota == false) {
    		if (pointsAt instanceof Phantom) ((Phantom)pointsAt).chooseNextParam();
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
    		accordinaNota = (Nota)Pointer.pointsAt;
    		nNotiVAccorde = 0;
    	}
    }

	public static Nota getCurrentAccordinuNotu() {
		return accordinaNota;
	}

}
