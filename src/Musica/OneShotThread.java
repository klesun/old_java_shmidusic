package Musica;

import Model.Staff;
import javax.sound.midi.*;

import Model.Accord.Nota.Nota;
import Midi.DeviceEbun;

public class OneShotThread extends Thread{
	Nota nota;
	static int EPSILON = 0;
	
	public OneShotThread(Nota nota){
	    this.nota = nota;
	}
	
	static int msIns = 1000;
	
	@Override
	public void run() {
		try {
			ShortMessage onMessage = new ShortMessage();
			ShortMessage offMessage = new ShortMessage();
			onMessage.setMessage( ShortMessage.NOTE_ON, nota.channel, (byte)nota.tune, nota.getVolume());
			DeviceEbun.sintReceiver.send(onMessage, -1);
			
			try {
			    Thread.sleep(nota.getTimeMiliseconds() - EPSILON);
			} catch (InterruptedException e) {
			    //System.out.println("Ошибка сна"+e);
			}
			
			offMessage.setMessage(ShortMessage.NOTE_OFF, nota.channel, (byte)nota.tune, 0);
			DeviceEbun.sintReceiver.send(offMessage, -1);
		} catch (InvalidMidiDataException e) {
			System.out.println("InvalidMidiDataException");
		}
		PlayMusThread.openNotas[nota.tune] = null;
	}
}
