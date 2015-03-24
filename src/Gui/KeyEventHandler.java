package Gui;

import Gui.staff.pointerable.Accord;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import Gui.SheetMusic;
import Gui.Window;
import Midi.DeviceEbun;
import Midi.MidiCommon;
import Gui.staff.Staff;
import Musica.PlayMusThread;
import Gui.staff.pointerable.Nota;
import Gui.staff.pointerable.Phantom;
import Gui.staff.Pointer;
import Tools.FileProcessor;
import java.util.LinkedList;

public class KeyEventHandler implements KeyListener {

	JFileChooser chooserSave = new JFileChooser("/var/www/desktop/Yuzefa");
	JFileChooser chooserExport = new JFileChooser("/var/www/desktop/Yuzefa");

	public SheetMusic sheet;
	final Staff staff;
	JFrame parent;

	LinkedList<int[]> midiEventQueue = new LinkedList();

	public KeyEventHandler(SheetMusic albert, JFrame parent) {
		this.sheet = albert;
		this.staff = albert.stan;
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

	public void handleFrameTimer() {
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

	public void keyPressed(KeyEvent e) {
		Accord curNota = this.sheet.getFocusedStaff().getFocusedAccord();
		int rVal;
		
		if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)) {
			switch (e.getKeyCode()) {
			case 'z':case 'Z':case 'Я':case 'я':
//				System.out.println("Вы нажали контрол-З");
//				staff.retrieveLast();
//				this.sheet.repaint();
//				break;
			case 'y':case 'Y':case 'Н':case 'н':
//				System.out.println("Вы нажали контрол-У");
//				staff.detrieveNotu();
//				this.sheet.repaint();
//				break;
			case 's':case 'S':case 'Ы':case 'ы':
				JFileChooser c = chooserSave;
				rVal = c.showSaveDialog(parent);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					File fn = c.getSelectedFile();
					if (!fn.getAbsolutePath().endsWith(".json")) {
						fn = new File(fn + ".json");
					}

					// FileProcessor.saveKlsnFile(fn);
					File jsFile = new File(fn.toString().substring(0,
							(int) (fn.toString().length() - 4))
							+ "json");
					FileProcessor.saveJsonFile(jsFile, staff);
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
			case 'P':case 'p':case 'З':case 'з':
				System.out.println("Вы нажали ctrl+p!");
				if (DeviceEbun.stop) {
					DeviceEbun.stop = false;
					(new PlayMusThread(this, this.staff)).start();
				} else {
					DeviceEbun.stopMusic();
				}
				break;
			case 'D':case 'd':case 'В':case 'в':
				System.out.println("Вы нажали ctrl+д это менять аутпут!");
				MidiCommon.listDevicesAndExit(false, true, false);
				DeviceEbun.changeOutDevice();
				break;
			case 'U':case 'u':case 'Г':case 'г':
				this.sheet.addNewStaff();
				this.sheet.repaint();
				break;
			case '+':case '=':
				System.out.println("ctrl+=");
				staff.parentSheetMusic.changeScale(1);
				break;
			case '-':case '_':
				System.out.println("ctrl+-");
				staff.parentSheetMusic.changeScale(-1);
				break;
			case 't':case 'T': case 'Е': case 'е':
				if (this.staff.getFocusedAccord() != null) { this.staff.getFocusedAccord().triggerTuplets(3); }
				this.sheet.repaint();
				break;
			case '0':
				System.out.println("Вы нажали 0!");
				staff.changeMode();
				break;
			case KeyEvent.VK_DOWN:
				System.out.println("Вы нажали Tab!");
				this.sheet.getFocusedStaff().getFocusedAccord().focusNextNota();
				sheet.repaint();
				break;

			default:
				break;
			}
			return;
		} else if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_3) {
			if (this.sheet.getFocusedStaff().getFocusedAccord().getFocusedIndex() == -1) {
				for (Nota nota: this.sheet.getFocusedStaff().getFocusedAccord().getNotaList()) {
					nota.triggerIsSharp();
				}
			} else {
				this.sheet.getFocusedStaff().getFocusedAccord().getFocusedNota().triggerIsSharp();
			}
			return;
		} else if (((e.getModifiers() & KeyEvent.CTRL_MASK) == 0)
				&& ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
			int cod = e.getKeyCode();
			if (cod >= '0' && cod <= '9') {
				staff.changeChannelFlag(cod - '0');
				sheet.repaint();
			}
		}

		switch (e.getKeyCode()) {
		case KeyEvent.VK_RIGHT:
			PlayMusThread.shutTheFuckUp();
			Pointer.moveRealtime(1, Pointer.SOUND_ON);
			this.sheet.repaint();
			// stan.drawPanel.checkCam();
			break;
		case KeyEvent.VK_LEFT:
			PlayMusThread.shutTheFuckUp();
			Pointer.moveRealtime(-1, Pointer.SOUND_ON);
			this.sheet.repaint();
			// stan.drawPanel.checkCam();
			break;
		case KeyEvent.VK_UP:
			PlayMusThread.shutTheFuckUp();
			System.out.println("hujguzno " + sheet.getWidth());
			sheet.getFocusedStaff().setFocusedIndex(sheet.getFocusedStaff().getFocusedIndex() - sheet.getFocusedStaff().getNotaInRowCount());
			this.requestNewSurface();
			staff.parentSheetMusic.checkCam();
			break;
		case KeyEvent.VK_DOWN:
			PlayMusThread.shutTheFuckUp();
			sheet.getFocusedStaff().setFocusedIndex(sheet.getFocusedStaff().getFocusedIndex() + sheet.getFocusedStaff().getNotaInRowCount());
			this.requestNewSurface();
			staff.parentSheetMusic.checkCam();
			break;
		case KeyEvent.VK_HOME:
			PlayMusThread.shutTheFuckUp();
			sheet.getFocusedStaff().setFocusedIndex(-1);
			staff.parentSheetMusic.checkCam();
			break;
		case KeyEvent.VK_END:
			PlayMusThread.shutTheFuckUp();
			sheet.getFocusedStaff().setFocusedIndex(sheet.getFocusedStaff().getAccordList().size() - 1);
			staff.parentSheetMusic.checkCam();
			break;
		case KeyEvent.VK_ENTER:
			PlayMusThread.shutTheFuckUp();
			PlayMusThread.playAccord(this.sheet.getFocusedStaff().getFocusedAccord());
			break;

		case KeyEvent.VK_ADD:
			System.out.println("Вы нажали плюс!");
			if (this.sheet.getFocusedStaff().getFocusedAccord() != null) {
				if (this.sheet.getFocusedStaff().getFocusedAccord().getFocusedIndex() == -1) {
					this.sheet.getFocusedStaff().getFocusedAccord().changeDur(1, false);
				} else {
					this.sheet.getFocusedStaff().getFocusedAccord().getFocusedNota().changeDur(1, true);
				}
			} else {
				this.sheet.getFocusedStaff().getPhantom().changeValue(1);
			}
			sheet.repaint();
			break;
		case KeyEvent.VK_SUBTRACT:
			System.out.println("Вы нажали минус!");
			if (this.sheet.getFocusedStaff().getFocusedAccord() != null) {
				if (this.sheet.getFocusedStaff().getFocusedAccord().getFocusedIndex() == -1) {
					this.sheet.getFocusedStaff().getFocusedAccord().changeDur(-1, false);
				} else {
					this.sheet.getFocusedStaff().getFocusedAccord().getFocusedNota().changeDur(-1, true);
				}
			} else {
				this.sheet.getFocusedStaff().getPhantom().changeValue(-1);
			}
			this.sheet.repaint();
			break;
		case KeyEvent.VK_DELETE:
			System.out.println("Вы нажали Delete!");
			staff.delNotu();
			this.requestNewSurface();
			break;
		case KeyEvent.VK_PAGE_DOWN:
			System.out.println("Вы нажали pageDown!");
			staff.parentSheetMusic.page(1);
			break;
		case KeyEvent.VK_PAGE_UP:
			System.out.println("Вы нажали pageUp!");
			staff.parentSheetMusic.page(-1);
			break;
		case KeyEvent.VK_BACK_SPACE:
			if (this.sheet.getFocusedStaff().getFocusedAccord() == null) {
				this.sheet.getFocusedStaff().getPhantom().backspace();
				break;
			} else {
				Accord accord = (Accord) curNota;
				String slog = accord.getSlog();
				if (slog.length() < 2) {

					if (slog.length() == 0) {
						Pointer.move(-1);
					}
					accord.setSlog("");
				} else {
					((Accord) curNota).setSlog(slog.substring(0, slog.length() - 1));
				}
			}
			sheet.repaint();
			break;
		case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
		case KeyEvent.VK_NUMPAD0:case KeyEvent.VK_NUMPAD1:case KeyEvent.VK_NUMPAD2:case KeyEvent.VK_NUMPAD3:case KeyEvent.VK_NUMPAD4:
		case KeyEvent.VK_NUMPAD5:case KeyEvent.VK_NUMPAD6:case KeyEvent.VK_NUMPAD7:case KeyEvent.VK_NUMPAD8:case KeyEvent.VK_NUMPAD9:
			if (this.sheet.getFocusedStaff().getFocusedAccord() == null) {
				this.sheet.getFocusedStaff().getPhantom().tryToWrite(e.getKeyChar());
				break;
			} else if (curNota instanceof Accord) {
				int cifra = (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') ? e
						.getKeyCode() - '0' : e.getKeyCode()
						- KeyEvent.VK_NUMPAD0;
				Nota nota = Pointer.getCurrentAccordinuNotu();
				if (nota != null) {
					if (nota.channel != cifra) {
						nota.setChannel(cifra);
					} else {
						Pointer.resetAcc();
						sheet.repaint();
					}
				} else {
					cifra = Math.min(cifra, ((Accord)curNota).getNotaList()
							.size());
					while (cifra-- > 0) {
						this.sheet.getFocusedStaff().getFocusedAccord().focusNextNota();
					}
					sheet.repaint();
				}
			} // не работает - сделай
			break;
		default:
			if (staff.mode == Staff.aMode.playin)
				break;

			if (e.getKeyCode() >= 32 || e.getKeyCode() == 0) {
				// Это символ - напечатать
				if (curNota instanceof Accord) {
					Accord accord = (Accord) curNota;
					accord.setSlog(accord.getSlog().concat("" + e.getKeyChar()));
				}
			}

			if (e.getKeyCode() == '-') {
				Pointer.move(1, true);
			}
			sheet.repaint();
			break;
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
