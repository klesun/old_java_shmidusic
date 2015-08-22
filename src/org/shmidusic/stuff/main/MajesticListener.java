package org.shmidusic.stuff.main;

import org.shmidusic.stuff.Midi.DeviceEbun;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MajesticListener implements WindowListener {
	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		DeviceEbun.closeMidiDevices();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}
}
