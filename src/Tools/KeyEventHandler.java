package Tools;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import Gui.DrawPanel;
import Gui.Window;
import Midi.MidiCommon;
import Musica.NotnyStan;
import Musica.PlayMusThread;
import Pointerable.IAccord;
import Pointerable.Nota;
import Pointerable.Phantom;
import Pointerable.Pointer;
import Pointerable.Pointerable;

public class KeyEventHandler implements KeyListener {
	
	JFileChooser chooserSave = new JFileChooser("/var/www/desktop/Yuzefa");
	JFileChooser chooserExport = new JFileChooser("/var/www/desktop/Yuzefa");
	
	public DrawPanel albert;
	final NotnyStan stan;
	JFrame parent;
	
	public KeyEventHandler(NotnyStan stan, DrawPanel Albert, JFrame parent) {
		this.stan = stan;
		this.albert = Albert;    	
		this.parent = parent;
		
		chooserSave.setFileFilter(new FileNameExtensionFilter("Klesun Midi-data","klsn"));
		chooserSave.setFileFilter(new FileNameExtensionFilter("Json Midi-music data","json"));
		chooserExport.setFileFilter(new FileNameExtensionFilter("PNG image","png"));
		
		DeviceEbun.openInDevice(this);
		DeviceEbun.openOutDevice();
	}

	public void handleMidi( int tune, int forca, int elapsed, long timestamp) {
		this.stan.addPressed(tune, forca, elapsed);
		this.albert.repaint();
	}
	
	public void keyPressed(KeyEvent e) {
		System.out.println("Нажали кнопку");
		Pointerable curNota = Pointer.pointsAt;
		int rVal;
		
		if ( ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0) ) {
			System.out.println("Control!");
			if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0) System.out.println("ALT!");;
			switch(e.getKeyCode()) {
				case 'z': case 'Z': case 'Я': case 'я':
				    System.out.println("Вы нажали контрол-З");
				    stan.retrieveLast();
				    this.albert.repaint();
				    break;
				case 'y': case 'Y': case 'Н': case 'н':
				    System.out.println("Вы нажали контрол-У");
				    stan.detrieveNotu();
				    this.albert.repaint();
				    break;
				case 's': case 'S': case 'Ы': case 'ы':
					JFileChooser c = chooserSave;
					rVal = c.showSaveDialog(parent);
					if (rVal == JFileChooser.APPROVE_OPTION) {
						File fn = c.getSelectedFile();
						if (!fn.getAbsolutePath().endsWith(".json")) {
						    fn = new File(fn + ".json");
						}
						
//						FileProcessor.saveKlsnFile(fn);
						File jsFile = new File(fn.toString().substring(0, (int)(fn.toString().length() - 4)) + "json");
						FileProcessor.saveJsonFile(jsFile, stan);
					}
					break;
				case 'j': case 'J': case 'о': case 'О':
					FileProcessor.saveJsonFile(null, stan);
					break;
				case 'o': case 'O': case 'щ': case 'Щ':
				    int i = okcancel("Are your sure? Unsaved data will be lost."); // 2 - cancel, 0 - ok
				    if (i == 0) {
				        int sVal = chooserSave.showOpenDialog(parent);
				        if (sVal == JFileChooser.APPROVE_OPTION) {
				        	if (chooserSave.getSelectedFile().getAbsolutePath().endsWith(".klsn")) {
				        		FileProcessor.openKlsnFile(chooserSave.getSelectedFile());
				        	} else if (chooserSave.getSelectedFile().getAbsolutePath().endsWith(".json")) {
				        		FileProcessor.openJsonFile(chooserSave.getSelectedFile(), this.stan);
				        	}
				        }
				    }
				    this.albert.repaint();
				    break;
				case 'e': case 'E': case 'у': case 'У':
				    JFileChooser c2 = chooserExport;
				    rVal = c2.showSaveDialog(parent);
				    if (rVal == JFileChooser.APPROVE_OPTION) {
				        File fn = c2.getSelectedFile();
				        if ( (!fn.getAbsolutePath().endsWith(".png")) && (!fn.getAbsolutePath().endsWith(".pdf")) && (!fn.getAbsolutePath().endsWith(".mid")) ) {
				            fn = new File(fn + ".png");
				            FileProcessor.savePNG(fn);
				        } else if (fn.getAbsolutePath().endsWith(".png")) {
				            System.out.println("Ща сохраню png");
				            fn = new File(fn+"");
				            FileProcessor.savePNG(fn);
				        } else if (fn.getAbsolutePath().endsWith(".mid")) {
				            System.out.println("Ща сохраню midi");
				            fn = new File(fn+"");
				            FileProcessor.saveMID(fn);
				        } else {
				            fn = new File(fn+"");
				            FileProcessor.savePDF(fn);
				        }
				    }
				    if (rVal == JFileChooser.CANCEL_OPTION) {
				        break;
				    }
				    break;
				case 'P': case 'p': case 'З': case 'з':
				    System.out.println("Вы нажали ctrl+p!");
				    if (DeviceEbun.stop) {
				    	DeviceEbun.stop = false;
						(new PlayMusThread(this, this.stan)).start();
				    } else {
				        DeviceEbun.stopMusic();
				    }
				    break;
				case 'D': case 'd': case 'В': case 'в':
				    System.out.println("Вы нажали ctrl+д это менять аутпут!");
				    MidiCommon.listDevicesAndExit(false,true,false);
				    DeviceEbun.changeOutDevice();
				    break;
				case 'U': case 'u': case 'Г': case 'г':
				    System.out.println("Вы нажали ctrl+u!");
				    stan.add(new Phantom());
				    this.albert.repaint();
				    break;
				case '+':case '=':
				    System.out.println("ctrl+=");
				    stan.drawPanel.incScale(1);
				    break;
				case '-':case '_':
				    System.out.println("ctrl+-");
				    stan.drawPanel.incScale(-1);
				    break;
				case '3':
				    stan.triolnutj();
				    this.albert.repaint();
				    break;
				case '0':
				    System.out.println("Вы нажали 0!");
				    stan.changeMode();
				    break;
				
				default: break;
			}
			return;
		} else if ( ((e.getModifiers() & KeyEvent.CTRL_MASK) == 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) != 0) ) {
			int cod = e.getKeyCode();
			if (cod >= '0' && cod <= '9') {
				stan.changeChannelFlag(cod - '0');
				albert.repaint();
			}
		}
		
		switch (e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
			    PlayMusThread.shutTheFuckUp();
			    Pointer.moveRealtime(1, Pointer.SOUND_ON);
			    this.albert.repaint();
			    //stan.drawPanel.checkCam();
			    break;
			case KeyEvent.VK_LEFT:
			    PlayMusThread.shutTheFuckUp();
			    Pointer.moveRealtime(-1, Pointer.SOUND_ON);
			    this.albert.repaint();
			    //stan.drawPanel.checkCam();
			    break;
			case KeyEvent.VK_UP:
			    PlayMusThread.shutTheFuckUp();
			    Pointer.moveSis(-1);
			    this.albert.repaint();
			    stan.drawPanel.checkCam();
			    break;
			case KeyEvent.VK_DOWN:
			    PlayMusThread.shutTheFuckUp();
			    Pointer.moveSis(1);
			    this.albert.repaint();
			    stan.drawPanel.checkCam();
			    break;
			case KeyEvent.VK_HOME:
				System.out.println("Вошли в event");
			    PlayMusThread.shutTheFuckUp();
				Pointer.moveToBegin();
				this.albert.repaint();
			    stan.drawPanel.checkCam();
			    System.out.println("Закончили event");
			    break;
			case KeyEvent.VK_END:
			    PlayMusThread.shutTheFuckUp();
				Pointer.moveToEnd();
				this.albert.repaint();
			    stan.drawPanel.checkCam();
			    break;
			case KeyEvent.VK_ENTER:
			    PlayMusThread.shutTheFuckUp();
				if (Pointer.accordinaNota != null) {
					PlayMusThread.playNotu(Pointer.accordinaNota, 1);
				} else if (Pointer.pointsAt instanceof Nota) PlayMusThread.playAccord((Nota)Pointer.pointsAt);
			    break;
			case KeyEvent.VK_SHIFT:
				System.out.println("Вы нажали Tab!");
				Pointer.nextAcc();
				albert.repaint();
			    break;
			    
			case KeyEvent.VK_ADD:
				System.out.println("Вы нажали плюс!");  
			    if (curNota instanceof Nota) {
			        if (Pointer.pointsOneNotaInAccord == false) curNota.changeDur(1, false);
			        else Pointer.accordinaNota.changeDur(1, true);
			        albert.repaint();
			        
				} else if (curNota instanceof Phantom) {
			        ((Phantom)curNota).changeValue(1);
			        stan.checkValues((Phantom)curNota);
			        this.albert.repaint();
				}
			    break;
			case KeyEvent.VK_SUBTRACT:
				System.out.println("Вы нажали минус!");
			    if (curNota instanceof Nota) {
			        if (Pointer.pointsOneNotaInAccord == false) curNota.changeDur(-1, false);
			        else Pointer.accordinaNota.changeDur(-1, true);
				    albert.repaint();
			    } else if (curNota instanceof Phantom) {
			        ((Phantom)curNota).changeValue(-1);
			        this.albert.repaint();
			    }
			    break;
			case KeyEvent.VK_DELETE:
				System.out.println("Вы нажали Delete!");
			    stan.delNotu();
			    this.albert.repaint();
			    break;
			case KeyEvent.VK_PAGE_DOWN:
				System.out.println("Вы нажали pageDown!");
			    stan.drawPanel.page(1);
			    break;
			case KeyEvent.VK_PAGE_UP:
				System.out.println("Вы нажали pageUp!");
			    stan.drawPanel.page(-1);
			    break;
			case KeyEvent.VK_BACK_SPACE:
				if (curNota instanceof Nota == false) {
			        ((Phantom)curNota).backspace();
			        stan.checkValues((Phantom)curNota);
			        break;
			    } else if (curNota instanceof IAccord) {
			    	IAccord accord = (IAccord)curNota;
				    String slog = accord.getSlog();
				    if (slog.length() < 2) {
				
				        if (slog.length() == 0 && curNota.prev instanceof IAccord) {
				        	IAccord prevAccord = (IAccord)curNota.prev;
				            slog = prevAccord.getSlog();
				            if (slog.length() < 2) {
				            	prevAccord.setSlog("");
				            } else {
				                ((Nota)curNota.prev).setSlog(  slog.substring(0, slog.length()-1)  );
				            }
				        }
				        accord.setSlog("");
				    } else {
				        ((Nota)curNota).setSlog(  slog.substring(0, slog.length()-1)  );
				    }
			    }
				albert.repaint();
			    break;
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
			case KeyEvent.VK_NUMPAD0: case KeyEvent.VK_NUMPAD1: case KeyEvent.VK_NUMPAD2: case KeyEvent.VK_NUMPAD3: case KeyEvent.VK_NUMPAD4: case KeyEvent.VK_NUMPAD5: 
			case KeyEvent.VK_NUMPAD6: case KeyEvent.VK_NUMPAD7: case KeyEvent.VK_NUMPAD8: case KeyEvent.VK_NUMPAD9:
				if (curNota instanceof Phantom) {
			        ((Phantom)curNota).tryToWrite( e.getKeyChar() );
			        stan.checkValues((Phantom)curNota);
			        break;
			    } else if (curNota instanceof Nota) {
					int cifra = (e.getKeyCode() >= '0' && e.getKeyCode() <='9') 
							? e.getKeyCode() - '0'
							: e.getKeyCode() - KeyEvent.VK_NUMPAD0;
					Nota nota = Pointer.getCurrentAccordinuNotu();
					if (nota != null) { 
						if (nota.channel != cifra) {
							nota.setChannel(cifra); 
						} else {
							Pointer.resetAcc();
							albert.repaint();
						}
					} else {
						cifra = Math.min(cifra, ((IAccord)curNota).getNotaList().size());
						while(cifra-- > 0) { 
							Pointer.nextAcc(); 
						}
						albert.repaint();
					} 
				} // не работает - сделай
				break;
			default:
			    if (stan.mode == NotnyStan.aMode.playin) break;
			
			    System.out.println("Keycode "+e.getKeyCode());
				if (e.getKeyCode() >= 32 || e.getKeyCode() == 0) {
					// Это символ - напечатать
					if (curNota instanceof IAccord) {
						IAccord accord = (IAccord)curNota;
						accord.setSlog( accord.getSlog().concat("" + e.getKeyChar()) );
					}
				}
			
			    if (e.getKeyCode() == '-') {
					Pointer.move(1, true);
			    }
			    albert.repaint();
			    break;
		}
	}
	
	public void keyReleased(KeyEvent e) {
	}
	
	public void keyTyped(KeyEvent e) {  }
	
	public static int okcancel(String theMessage) {
	    int result = JOptionPane.showConfirmDialog((Component) null, theMessage,
	        "alert", JOptionPane.OK_CANCEL_OPTION);
	    return result;
	}
}
