package Gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import Midi.DeviceEbun;
import Model.Staff;
import Model.StaffHandler;
import Tools.FileProcessor;
import java.util.LinkedList;

public class KeyEventHandler implements KeyListener {

	JFileChooser chooserSave = new JFileChooser("/var/www/desktop/Yuzefa");
	JFileChooser chooserExport = new JFileChooser("/var/www/desktop/Yuzefa");

	public SheetMusic sheet;
	final Staff staff;
	JFrame parent;

	LinkedList<int[]> midiEventQueue = new LinkedList<>();

	public KeyEventHandler(SheetMusic albert, JFrame parent) {
		this.sheet = albert;
		this.staff = albert.getFocusedStaff();
		this.parent = parent;

		chooserSave.setFileFilter(new FileNameExtensionFilter(
				"Klesun Midi-data", "klsn"));
		chooserSave.setFileFilter(new FileNameExtensionFilter(
				"Json Midi-music data", "json"));
		chooserExport.setFileFilter(new FileNameExtensionFilter("PNG image",
				"png"));

		DeviceEbun.openInDevice(this);
		DeviceEbun.openOutDevice();
	}

	public Boolean shouldRepaint = false;

	public void handleMidiEvent(int tune, int forca, int elapsed, int timestamp) {
		this.midiEventQueue.add(new int[]{tune, forca, timestamp});
	}

	synchronized public void handleFrameTimer() {
		int[] midiRecord;
		while ((midiRecord = this.midiEventQueue.poll()) != null) {
			this.staff.addPressed(midiRecord[0], midiRecord[1], midiRecord[2]);
			this.shouldRepaint = true;
		}
		if (this.shouldRepaint) {
			try {
				this.sheet.repaint();
			} catch (java.util.ConcurrentModificationException exc) { System.out.println("Пошли в жопу пидорасы"); }
			this.shouldRepaint = false;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int rVal;

		// new SheetHandler(this.sheet).handleKey(e);
		new StaffHandler(this.sheet.getFocusedStaff()).handleKey(e);
		
		if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)) {
			switch (e.getKeyCode()) {
				case 's':case 'S':case 'Ы':case 'ы':
					JFileChooser c = chooserSave;
					rVal = c.showSaveDialog(parent);
					if (rVal == JFileChooser.APPROVE_OPTION) {
						File fn = c.getSelectedFile();
						if (!fn.getAbsolutePath().endsWith(".json")) {
							fn = new File(fn + ".json");
						}
						FileProcessor.saveJsonFile(fn, staff);
					}
					break;
				case 'j':case 'J':case 'о':case 'О':
					FileProcessor.saveJsonFile(null, staff);
					break;
				case 'o':case 'O':case 'щ':case 'Щ':
					int i = okcancel("Are your sure? Unsaved data will be lost."); // 2 - cancel, 0 - ok
					if (i == 0) {
						int sVal = chooserSave.showOpenDialog(parent);
						if (sVal == JFileChooser.APPROVE_OPTION) {
							if (chooserSave.getSelectedFile().getAbsolutePath().endsWith(".json")) {
								FileProcessor.openJsonFile(chooserSave.getSelectedFile(), this.staff);
							}
						}
					}
					this.sheet.repaint();
					break;
				case 'e':case 'E':case 'у':case 'У':
					JFileChooser c2 = chooserExport;
					rVal = c2.showSaveDialog(parent);
					if (rVal == JFileChooser.APPROVE_OPTION) {
						File fn = c2.getSelectedFile();
						if ((!fn.getAbsolutePath().endsWith(".png"))
								&& (!fn.getAbsolutePath().endsWith(".pdf"))
								&& (!fn.getAbsolutePath().endsWith(".mid"))) {
							fn = new File(fn + ".png");
							FileProcessor.savePNG(fn, this.sheet.getFocusedStaff());
						} else if (fn.getAbsolutePath().endsWith(".png")) {
							System.out.println("Ща сохраню png");
							fn = new File(fn + "");
							FileProcessor.savePNG(fn, this.sheet.getFocusedStaff());
						}
					}
					if (rVal == JFileChooser.CANCEL_OPTION) {
						break;
					}
					break;
				case 'U':case 'u':case 'Г':case 'г':
					this.sheet.addNewStaff();
					this.sheet.repaint();
					break;
				case '+':case '=':
					System.out.println("ctrl+=");
					this.sheet.changeScale(1);
					break;
				case '-':case '_':
					System.out.println("ctrl+-");
					this.sheet.changeScale(-1);
					break;
				default:
					break;
			}
			return;
		}

		switch (e.getKeyCode()) {
			case KeyEvent.VK_PAGE_DOWN:
				System.out.println("Вы нажали pageDown!");
				staff.getParentSheet().page(1);
				break;
			case KeyEvent.VK_PAGE_UP:
				System.out.println("Вы нажали pageUp!");
				staff.getParentSheet().page(-1);
				break;
			default: break;
		}
	}

	public void requestNewSurface() {
		this.shouldRepaint = true;
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public static int okcancel(String theMessage) {
		int result = JOptionPane.showConfirmDialog((Component) null,
				theMessage, "alert", JOptionPane.OK_CANCEL_OPTION);
		return result;
	}
}
