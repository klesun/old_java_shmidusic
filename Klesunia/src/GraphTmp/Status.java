package GraphTmp;

import java.awt.Label;

import Musica.Nota;
import Musica.NotnyStan;

public class Status extends Label{
	NotnyStan stan;
	public Status(NotnyStan stan){
		super();
		this.stan = stan;
	}

	public void renew(){
		String s = "Режим: " + stan.mode + "  Размер системы: " + stan.stepInOneSys + "  Число нот: " + stan.noshuCount + ", ВРЕМЯ: " + Nota.time;
		setText(s);	
	}
}
