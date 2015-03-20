package Musica;

import javax.sound.midi.*;

import Pointerable.Nota;
import Midi.DeviceEbun;

public class OneShotThread extends Thread{
	Receiver sintReceiver = DeviceEbun.sintReceiver;;
	Nota nota;
	static int EPSILON = 0;
	int volume;
	int tupletDenominator = 1;
	
	public OneShotThread(Nota nota, int tupletDenominator){
	    this.tupletDenominator = tupletDenominator;
	    this.nota = nota;
	    volume = (int)Math.round(nota.forca * Staff.volume);
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
			int time = (short)( msIns*nota.numerator/Staff.DEFAULT_ZNAM*4/Staff.tempo*60 / tupletDenominator );
			if (nota.tune == 36) volume = 0;
			onMessage.setMessage( ShortMessage.NOTE_ON, Staff.CHANNEL, (byte)nota.tune, (byte)volume);
			sintReceiver.send(onMessage, -1);
			
			try {
			    Thread.sleep(time - EPSILON);
			} catch (InterruptedException e) {
			    //System.out.println("Ошибка сна"+e);
			}
			
			offMessage.setMessage(ShortMessage.NOTE_OFF, Staff.CHANNEL, (byte)nota.tune, 0);
			sintReceiver.send(offMessage, -1);
		} catch (InvalidMidiDataException e) {
			System.out.println("InvalidMidiDataException");
		}
		PlayMusThread.openNotes[nota.tune] = null;
	}
}
