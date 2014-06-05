package Tools;

import Musica.*;

public class Pointer {
	
    public static int pos;
    public static int gpos = 0;
    public static NotnyStan stan = null;
    public static Pointerable curNota;
    public static Phantom beginNota;
    public static int AcNo = -1;
    public static int setStan(NotnyStan newStan){
        pos = -1;
        stan = newStan;
        curNota = stan.phantomka;
        return 0;
    }
    /*
    public Pointer(NotnyStan stan){
        pos = -1;       
        this.stan = stan;
        curNota = stan.phantomka;
    }
    */
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
    	while (move(1) != -1);
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
    public static int moveTo(Nota nota){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
    	moveToBegin();
    	while (nota != curNota  &&  move(1) != -1);
    	if (nota != curNota) return -1;
    	else return 0;
    }
    
    public static int move(int q){
        return moveBo(q, false);
    }
    
    public static int move(int q, boolean tru){
        return moveBo(q, tru);
    }
    
    public static int moveBo(int q, boolean bo){
        if (stan == null) {
            System.out.println("There is no stan you're talking about");
            return 66;
        }
        AcNo = -1;
    	if ((pos+q >= stan.noshuCount) || (pos+q < -1)) return -1;
    	int delta=0;
        
    	pos += q;
        stan.isChanSep = false;
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
        
        if (bo) playMusThread.playAccordDivided(curNota, 1);
        //stan.drawPanel.checkCam();
        stan.drawPanel.repaint();
        return 0;
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

}
