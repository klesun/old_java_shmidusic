package Tools;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import BackEnd.MidiCommon;
import GraphTmp.DrawPanel;
import GraphTmp.GraphMusica;
import Musica.NotnyStan;
import Musica.playMusThread;
import Pointiki.Nota;
import Pointiki.Phantom;
import Pointiki.Pointer;
import Pointiki.Pointerable;

public class KeyEventi implements KeyListener {

    JFileChooser chooserSave = new JFileChooser("/var/www/desktop/Yuzefa");
    JFileChooser chooserExport = new JFileChooser("/var/www/desktop/Yuzefa");
	FileFilter filterSave = new FileNameExtensionFilter("Klesun Midi-data","klsn");
    FileFilter filterExport = new FileNameExtensionFilter("PNG image","png");
    
    DrawPanel Albert;
    final NotnyStan stan;
    JFrame parent;
	
    public KeyEventi(NotnyStan stan, DrawPanel Albert, JFrame parent) {
    	this.stan = stan;
    	this.Albert = Albert;    	
    	this.parent = parent;
    	
    	chooserSave.setFileFilter(filterSave);
        chooserExport.setFileFilter(filterExport);
    }
    
	public void keyPressed(KeyEvent e) {
		System.out.println("Нажали кнопку");
		Pointerable curNota = Pointer.curNota;
        int rVal;

        if ( ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0) ) {
            System.out.println("Control!");
            if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0) System.out.println("ALT!");;
            switch(e.getKeyCode()) {
                case 'z': case 'Z': case 'Я': case 'я':
                    System.out.println("Вы нажали контрол-З");
                    stan.retrieveLast();
                    break;
                case 'y': case 'Y': case 'Н': case 'н':
                    System.out.println("Вы нажали контрол-У");
                    stan.detrieveNotu();
                    break;
                case 's': case 'S': case 'Ы': case 'ы':
                    JFileChooser c = chooserSave;
                    rVal = c.showSaveDialog(parent);
                    if (rVal == JFileChooser.APPROVE_OPTION) {
                        File fn = c.getSelectedFile();
                        if (!fn.getAbsolutePath().endsWith(".klsn")) {
                            fn = new File(fn + ".klsn");
                        }

                        FileProcessor.saveKlsnFile(fn);
                    }
                    if (rVal == JFileChooser.CANCEL_OPTION) {
                        break;
                    }
                    break;
                case 'o': case 'O': case 'щ': case 'Щ':
                    int i = okcancel("Are your sure? Unsaved data will be lost."); // 2 - cancel, 0 - ok
                    if (i == 0) {
                        int sVal = chooserSave.showOpenDialog(parent);
                        if (sVal == JFileChooser.APPROVE_OPTION) {
                            FileProcessor.openKlsnFile(chooserSave.getSelectedFile());
                        }
                        if (sVal == JFileChooser.CANCEL_OPTION) {
                            break;
                        }
                    }
                    break;
                case 'e': case 'E': case 'у': case 'У':
                    JFileChooser c2 = chooserExport;
                    rVal = c2.showSaveDialog(parent);
                    if (rVal == JFileChooser.APPROVE_OPTION) {
                        File fn = c2.getSelectedFile();
                        if ( (!fn.getAbsolutePath().endsWith(".png")) && (!fn.getAbsolutePath().endsWith(".pdf")) ) {
                            fn = new File(fn + ".png");
                            FileProcessor.savePNG(fn);
                        } else if (fn.getAbsolutePath().endsWith(".png")) {
                            System.out.println("Ща сохраню");
                            fn = new File(fn+"");
                            FileProcessor.savePNG(fn);
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
                    if (DeviceEbun.stop)
                        DeviceEbun.playEntire(stan);
                    else
                        DeviceEbun.stopMusic();
                    break;
                case 'D': case 'd': case 'В': case 'в':
                    System.out.println("Вы нажали ctrl+д это менять аутпут!");
                    MidiCommon.listDevicesAndExit(false,true,false);
                    DeviceEbun.changeOutDevice();
                    break;
                case 'U': case 'u': case 'Г': case 'г':
                    System.out.println("Вы нажали ctrl+u!");
                    stan.addPhantom();
                    break;
                case 'T': case 't': case 'Е': case 'е':
                    System.out.println("Вы нажали ctrl+T!");
                    if (stan.checken) stan.checkTessi();
                    else stan.checken = false;
                    break;
                case '+':case '=':
                    System.out.println("ctrl+=");
                    stan.drawPanel.incScale(1);
                    break;
                case '-':case '_':
                    System.out.println("ctrl+-");
                    stan.drawPanel.incScale(-1);
                    break;
                case '1':
                    stan.slianie();
                    break;
                case '3':
                    stan.triolnutj();
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
				Albert.repaint();
        	}
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                playMusThread.shutTheFuckUp();
                Pointer.moveRealtime(1, Pointer.SOUND_ON);
                //stan.drawPanel.checkCam();
                break;
            case KeyEvent.VK_LEFT:
                playMusThread.shutTheFuckUp();
                Pointer.moveRealtime(-1, Pointer.SOUND_ON);
                //stan.drawPanel.checkCam();
                break;
            case KeyEvent.VK_UP:
                playMusThread.shutTheFuckUp();
                Pointer.moveSis(-1);
                stan.drawPanel.checkCam();
                break;
            case KeyEvent.VK_DOWN:
                playMusThread.shutTheFuckUp();
                Pointer.moveSis(1);
                stan.drawPanel.checkCam();
                break;
            case KeyEvent.VK_HOME:
            	System.out.println("Вошли в event");
                playMusThread.shutTheFuckUp();
            	Pointer.moveToBegin();
                stan.drawPanel.checkCam();
                System.out.println("Закончили event");
                break;
            case KeyEvent.VK_END:
                playMusThread.shutTheFuckUp();
            	Pointer.moveToEnd();
                stan.drawPanel.checkCam();
                break;
            case KeyEvent.VK_ENTER:
                playMusThread.shutTheFuckUp();
                Pointer.move(0, true);
                break;
            case KeyEvent.VK_SHIFT:
            	System.out.println("Вы нажали Tab!");
            	Pointer.nextAcc();
            	Albert.repaint();
                break;
                
            case KeyEvent.VK_ADD:
            	System.out.println("Вы нажали плюс!");  
                if (curNota instanceof Nota) {
                    if (Pointer.pointsOneNotaInAccord == false) curNota.changeDur(1, false);
                    else Pointer.accordinaNota.changeDur(1, true);
                    Albert.repaint();
                    
            	} else if (curNota instanceof Phantom) {
                    ((Phantom)curNota).changeValue(1);
            	}
                break;
            case KeyEvent.VK_SUBTRACT:
            	System.out.println("Вы нажали минус!");
                if (curNota instanceof Nota) {
                    if (Pointer.pointsOneNotaInAccord == false) curNota.changeDur(-1, false);
                    else Pointer.accordinaNota.changeDur(-1, true);
            	    Albert.repaint();
                } else if (curNota instanceof Phantom) {
                    ((Phantom)curNota).changeValue(-1);
                }
                break;
            case KeyEvent.VK_DELETE:
            	System.out.println("Вы нажали Delete!");
                stan.delNotu();
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
                    break;
                }
                String slog = curNota.slog;
                if (slog.length() < 2) {

                    if (slog.length() == 0 && curNota.prev instanceof Nota) {
                        slog = curNota.prev.slog;
                        if (slog.length() < 2) {
                            curNota.prev.slog = "";
                        } else {
                            ((Nota)curNota.prev).setSlog(  slog.substring(0, slog.length()-1)  );
                        }
                    }
                    curNota.slog = "";
                } else {
                    ((Nota)curNota).setSlog(  slog.substring(0, slog.length()-1)  );
                }
                Albert.repaint();
                break;
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
			case KeyEvent.VK_NUMPAD0: case KeyEvent.VK_NUMPAD1: case KeyEvent.VK_NUMPAD2: case KeyEvent.VK_NUMPAD3: case KeyEvent.VK_NUMPAD4: case KeyEvent.VK_NUMPAD5: 
			case KeyEvent.VK_NUMPAD6: case KeyEvent.VK_NUMPAD7: case KeyEvent.VK_NUMPAD8: case KeyEvent.VK_NUMPAD9:
				if (curNota instanceof Phantom) {
                    ((Phantom)curNota).tryToWrite( e.getKeyChar() );
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
							Albert.repaint();
						}
					} else {
						cifra = Math.min(cifra, ((Nota)curNota).getNoteCountInAccord());
						while(cifra-- > 0) { 
							Pointer.nextAcc(); 
						}
						Albert.repaint();
					} 
				} // не работает - сделай
				break;
            default:
                if (stan.mode == NotnyStan.aMode.playin) break;

                System.out.println("Keycode "+e.getKeyCode());
            	if (e.getKeyCode() >= 32 || e.getKeyCode() == 0) {
            		// Это символ - напечатать
            		((Nota)curNota).setSlog(  curNota.slog.concat( "" + e.getKeyChar() )  );
            	}

                if (e.getKeyCode() == '-') {
                    if (Pointer.curNota.isTriol)
                        Pointer.move(3, true);
                    else {
                        Pointer.move(1, true);
                    }
                }
            	System.out.println(curNota.slog);
                Albert.repaint();
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
