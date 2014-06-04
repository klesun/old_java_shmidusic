package Musica;

import javax.sound.midi.*;

public class OneShotThread extends Thread{
    Receiver sintReceiver = NotnyStan.sintReceiver;;
    Nota nota;
    static int EPSILON = 50;
    int volume;
	
	public OneShotThread(Nota nota){
        this.nota = nota;
        volume = (int)Math.round(nota.forca * NotnyStan.volume);
        if (volume > 127) volume = 127;
        if (volume < 0) {
            System.out.println("Однопульные трэды бастуют против отрицательных громкостей!");
            volume = 0;
        }
	}
	
	static int msIns = 1000;
	
    @Override
    public void run() {
    	try {
    	    ShortMessage onMessage = new ShortMessage();
    	    ShortMessage offMessage = new ShortMessage();
	        int time = (short)( msIns*nota.cislic/nota.znamen*4/NotnyStan.tempo*60 );
			onMessage.setMessage( ShortMessage.NOTE_ON, 2, (byte)nota.tune, (byte)nota.forca);             	
			sintReceiver.send(onMessage, -1);
	        
	        try { Thread.sleep(time - EPSILON); } catch (InterruptedException e) { System.out.println("Ошибка сна"+e); }
	
	    	offMessage.setMessage(ShortMessage.NOTE_OFF, 2, (byte)nota.tune, 0);
	    	sintReceiver.send(offMessage, -1);
    	} catch (InvalidMidiDataException e) {
    		System.out.println("InvalidMidiDataException");
    	}
    }
}
