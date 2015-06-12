package Stuff.Musica;

import javax.sound.midi.*;

import Storyspace.Staff.Accord.Nota.Nota;
import Stuff.Midi.DeviceEbun;

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
			onMessage.setMessage( ShortMessage.NOTE_ON, nota.getChannel(), nota.getTune().byteValue(), nota.getVolume());
			DeviceEbun.theirReceiver.send(onMessage, -1);

			offMessage.setMessage(ShortMessage.NOTE_OFF, nota.getChannel(), nota.getTune().byteValue(), 0);
			try {
			    Thread.sleep(nota.getTimeMilliseconds(true));
			} catch (InterruptedException e) {
			    //System.out.println("Ошибка сна"+e);
			}

			DeviceEbun.theirReceiver.send(offMessage, -1);
		} catch (InvalidMidiDataException e) {
			System.out.println("InvalidMidiDataException");
		}
		PlayMusThread.opentNotas[nota.getTune()][nota.getChannel()] = null;
	}

	public Nota getNota() {
		return this.nota;
	}

	public void setNota(Nota nota) {
		this.nota = nota;
	}
}
