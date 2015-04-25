package Gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import Midi.DeviceEbun;
import Model.Combo;
import Model.Staff.Staff;
import Model.Staff.StaffHandler;
import Tools.FileProcessor;
import java.util.LinkedList;

public class KeyEventHandler implements KeyListener {

	JFileChooser chooserSave = new JFileChooser("/var/www/desktop/Yuzefa");
	JFileChooser chooserExport = new JFileChooser("/var/www/desktop/Yuzefa");

	public SheetPanel sheet;
	final Staff staff;
	JFrame parent;

	public Boolean shouldRepaint = true;

	public KeyEventHandler(SheetPanel albert, JFrame parent) {
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

	public void handleMidiEvent(Integer tune, int forca, int timestamp) {
		if (forca > 0) {
			KeyEvent dispatchEvent = new KeyEvent(sheet, 0, 0, 11, Combo.tuneToAscii(tune), '♥'); // (11 -ctrl+shift+alt)+someKey
			this.keyPressed(dispatchEvent);
		} else {
			// keyup event
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

		// new SheetHandler(this.sheet).handleKey(e);
		if (sheet.getFocusedStaff() != null) {
			this.sheet.getFocusedStaff().gettHandler().handleKey(new Combo(e));
		}
		
		if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)) {
			switch (e.getKeyCode()) {
				case 's':case 'S':case 'Ы':case 'ы':
					JFileChooser c = chooserSave;
					int rVal = c.showSaveDialog(parent);
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
					int i = okcancel("Are your sure? Unsaved data will be lost."); // 2 - cancel, 0 - ok очевидно же
					if (i == 0) {
						int sVal = chooserSave.showOpenDialog(parent);
						if (sVal == JFileChooser.APPROVE_OPTION) {
							if (chooserSave.getSelectedFile().getAbsolutePath().endsWith(".json")) {
								FileProcessor.openJsonFile(chooserSave.getSelectedFile(), this.staff);
							}
						}
					}
					this.sheet.checkCam();
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
							FileProcessor.savePNG(fn, sheet);
						} else if (fn.getAbsolutePath().endsWith(".png")) {
							System.out.println("Ща сохраню png");
							fn = new File(fn + "");
							FileProcessor.savePNG(fn, sheet);
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
				sheet.page(1);
				break;
			case KeyEvent.VK_PAGE_UP:
				System.out.println("Вы нажали pageUp!");
				sheet.page(-1);
				break;
			default: break;
		}
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
