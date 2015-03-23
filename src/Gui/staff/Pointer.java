package Gui.staff;

import Gui.staff.Staff;
import Gui.staff.pointerable.Accord;
import Gui.staff.pointerable.Nota;
import Gui.staff.pointerable.Phantom;
import Musica.*;

public class Pointer {
	public static boolean SOUND_ON = true;
	public static boolean SOUND_OFF = false;
	
    public static int pos;
    public static int gpos = 0;
    public static Staff stan = null;
    public static Accord pointsAt = null;
    public static Phantom beginNota;    
    public static int init(Staff newStan){
        pos = -1;
        stan = newStan;
        return 0;
    }

	private static Pointer instance = null;

	public static Pointer getInstance() {
		if (Pointer.instance == null) {
			Pointer.instance = new Pointer();
		}
		return Pointer.instance;
	}
    
    public static int moveOut(){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	pos = -1;
    	pointsAt = null;
    	return 0;
    }
    
    public static int moveToBegin(){
        stan.checkValues(stan.phantomka);
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	pos = 0;    	
    	gpos = 0;
    	if (stan.getAccordList().size() == 0) return -1;
    	pointsAt = stan.getAccordList().get(0);
        return 0;
    }
    public static int moveToEnd(){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	while (move(1) != false);
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
    public static int moveTo(Accord accord){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	moveToBegin();
    	while (accord != pointsAt  &&  move(1));
    	if (accord != pointsAt) return -1;
    	else return 0;
    }
    
    public static boolean move(int q) {
        return move(q, false);
    }
    
    public static boolean moveSis(int n) { // TODO: logic mistake... somewhere here
    	int stepCount = n * stan.parentSheetMusic.getStepInOneSysCount();
    	while (stepCount > 0) {
    		stepCount -= pointsAt.getTakenStepCount() * 2;
    		moveRealtime(1, SOUND_OFF);
    	}
    	while (stepCount < 0) {
    		moveRealtime(-1, SOUND_OFF);
    		stepCount += pointsAt.getTakenStepCount() * 2;
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
    	
        while (q > 0) {
        	delta += pointsAt.getTakenStepCount();
        	pointsAt = pointsAt.next;
        	--q;
        }
        while (q < 0) {        	
        	pointsAt = pointsAt.prev;
        	delta -= pointsAt.getTakenStepCount();
        	++q;
        }                
        gpos += delta;
		accordinaNota = null;
        if (withSound && pointsAt != null) {
			PlayMusThread.playAccord((Accord)pointsAt);
		}

        return true;
    }
    
    public static boolean isAfter(Accord accord){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return false;
        }
    	for ( Accord tmp = pointsAt; tmp != null; tmp = tmp.next ) {
    		if (tmp == accord) return false; 
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

	public static Nota getCurrentAccordinuNotu() {
		return accordinaNota;
	}

	// getters/setters

	public int getPos() {
		return this.pos;
	}

}
