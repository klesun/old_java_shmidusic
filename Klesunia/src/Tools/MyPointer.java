package Tools;

import Musica.*;

public class MyPointer {
	
    public int pos;    
    public int gpos = 0;
    NotnyStan stan;
    public Pointerable curNota;
    public Phantom beginNota;
    public int AcNo = -1;
    public MyPointer(NotnyStan stan){
        pos = -1;       
        this.stan = stan;
        curNota = stan.phantomka;
    }
    public int moveOut(){
    	pos = -1;
    	curNota = beginNota;
    	return 0;
    }
    
    public int moveToBegin(){    	
    	curNota.underPtr = false;
    	pos = 0;    	
    	gpos = 0;
    	if (beginNota.next == null) return -1;
    	curNota = beginNota.next;
    	curNota.underPtr = true;
        return 0;
    }
    public void moveToEnd(){    	
    	curNota.underPtr = false;
    	while (move(1) != -1);
    	curNota.underPtr = true;
    }
    
    public int moveTo(int q){
    	if ((q >= stan.noshuCount) || (q < -1)) {System.out.println("Stack overflow"); return -1;}    	
    	 
    	moveToBegin();
    	move(q);

        return 0;
    }
    public int moveTo(Nota nota){	    	
    	moveToBegin();
    	while (nota != curNota  &&  move(1) != -1);
    	if (nota != curNota) return -1;
    	else return 0;
    }
    
    public int move(int q){        
        return moveBo(q, false);
    }
    
    public int move(int q, boolean tru){        
        return moveBo(q, tru);
    }
    
    public int moveBo(int q, boolean bo){
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
        
        if (bo) playMusThread.playAccord(curNota);
        //stan.drawPanel.checkCam();
        stan.drawPanel.repaint();
        return 0;
    }
    
    public boolean isAfter(Pointerable iskomajaNota){    	
    	for ( Pointerable tmp = curNota; tmp != null; tmp = tmp.next ) {
    		if (tmp == iskomajaNota) return false; 
    	}
    	return true;
    }

}
