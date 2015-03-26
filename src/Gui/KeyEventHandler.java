package Gui;

import Model.Accord.Accord;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import Midi.DeviceEbun;
import Model.Staff;
import Musica.PlayMusThread;
import Model.Accord.Nota.Nota;
import Model.StaffHandler;
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

	@Override
	public void keyPressed(KeyEvent e) {
		Accord curAccord = this.sheet.getFocusedStaff().getFocusedAccord();
		int rVal;

		// new SheetHandler(this.sheet).handleKey(e);
		new StaffHandler(this.sheet.getFocusedStaff()).handleKey(e);
		
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
					this.requestNewSurface();
					break;
				case '0':
					System.out.println("Вы нажали 0!");
					staff.changeMode();
					break;
				case KeyEvent.VK_DOWN:
					System.out.println("Вы нажали Tab!");
					if (this.sheet.getFocusedStaff().getFocusedAccord() != null) {
						this.sheet.getFocusedStaff().getFocusedAccord().moveFocus(+1);
					} else {
						this.sheet.getFocusedStaff().getPhantom().chooseNextParam();
					}

					sheet.repaint();
					break;
				case KeyEvent.VK_UP:
					System.out.println("Вы нажали Tab!");
					if (this.sheet.getFocusedStaff().getFocusedAccord() != null) {
						this.sheet.getFocusedStaff().getFocusedAccord().moveFocus(-1);
					} else {
						// this.sheet.getFocusedStaff().getPhantom().choosePrevParam();
					}

					sheet.repaint();
					break;

				default:
					break;
			}
			return;
		} else if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_3) {
			if (this.sheet.getFocusedStaff().getFocusedAccord().getFocusedNota() == null) {
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
			sheet.getFocusedStaff().moveFocus(1);
			this.sheet.repaint();
			// stan.drawPanel.checkCam();
			break;
		case KeyEvent.VK_LEFT:
			PlayMusThread.shutTheFuckUp();
			sheet.getFocusedStaff().moveFocus(-1);
			this.sheet.repaint();
			// stan.drawPanel.checkCam();
			break;
		case KeyEvent.VK_UP:
			PlayMusThread.shutTheFuckUp();
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
					this.sheet.getFocusedStaff().getFocusedAccord().changeLength(1);
				} else {
					this.sheet.getFocusedStaff().getFocusedAccord().getFocusedNota().changeDur(1);
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
					this.sheet.getFocusedStaff().getFocusedAccord().changeLength(-1);
				} else {
					this.sheet.getFocusedStaff().getFocusedAccord().getFocusedNota().changeDur(-1);
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
				Accord accord = (Accord) curAccord;
				String slog = accord.getSlog();
				if (slog.length() < 2) {

					if (slog.length() == 0) {
						sheet.getFocusedStaff().moveFocus(-1);
					}
					accord.setSlog("");
				} else {
					((Accord) curAccord).setSlog(slog.substring(0, slog.length() - 1));
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
			} else {
				int cifra = (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') ? e
						.getKeyCode() - '0' : e.getKeyCode()
						- KeyEvent.VK_NUMPAD0;
				Nota nota = curAccord.getFocusedNota();
				if (nota != null) {
					if (nota.channel != cifra) {
						nota.setChannel(cifra);
					} else {
						curAccord.setFocusedIndex(-1);
						sheet.repaint();
					}
				} else {
					cifra = Math.min(cifra, ((Accord)curAccord).getNotaList()
							.size());
					this.sheet.getFocusedStaff().getFocusedAccord().setFocusedIndex(cifra);
					sheet.repaint();
				}
			} // не работает - сделай
			break;
		default:
			if (staff.mode == Staff.aMode.playin)
				break;

			if (e.getKeyCode() >= 32 || e.getKeyCode() == 0) {
				// Это символ - напечатать
				if (curAccord instanceof Accord) {
					Accord accord = (Accord) curAccord;
					accord.setSlog(accord.getSlog().concat("" + e.getKeyChar()));
				}
			}

			if (e.getKeyCode() == '-') {
				sheet.getFocusedStaff().moveFocus(1);
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
