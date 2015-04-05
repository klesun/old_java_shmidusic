package Musica;

import javax.sound.midi.*;

import Model.Staff.Accord.Nota.Nota;
import Midi.DeviceEbun;

public class OneShotThread extends Thread{
	Nota nota;
	
	public OneShotThread(Nota nota){
	    this.nota = nota;
	}
	
	@Override
	public void run() {
		try {
			ShortMessage onMessage = new ShortMessage();
			ShortMessage offMessage = new ShortMessage();
			onMessage.setMessage( ShortMessage.NOTE_ON, nota.channel, (byte)nota.tune, nota.getVolume());
			DeviceEbun.theirReceiver.send(onMessage, -1);
			
			offMessage.setMessage(ShortMessage.NOTE_OFF, nota.channel, (byte)nota.tune, 0);
			try {
			    Thread.sleep(nota.getTimeMiliseconds());
			} catch (InterruptedException e) {
			    //System.out.println("Ошибка сна"+e);
			}
			
			DeviceEbun.theirReceiver.send(offMessage, -1);
		} catch (InvalidMidiDataException e) {
			System.out.println("InvalidMidiDataException");
		}
		PlayMusThread.opentNotas[nota.tune][nota.channel] = null;
	}
}
