package GraphTmp;

import Musica.*;
import Tools.Phantom;
import Tools.Pointer;
import Tools.Pointerable;

import javax.swing.*;
import javax.swing.filechooser.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GraphMusica extends JFrame implements ActionListener {

    final NotnyStan stan;
    DrawPanel Albert;
    Status status;
    
    Pointerable curNota;
    JFileChooser c = new JFileChooser();
	FileFilter filter = new FileNameExtensionFilter("Klesun Midi-data","klsn");
	
	JTextField tempoField = new JTextField(4);
	boolean ctrl = false;
    
    public GraphMusica(final NotnyStan stan){
        super("Да будет такая музыка!"); //Заголовок окна

        this.stan = stan;
        status = new Status(stan);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setLayout(new BorderLayout());
        JScrollPane elder;
        this.add(elder = new JScrollPane(Albert = new DrawPanel(stan)), BorderLayout.CENTER);
        elder.getVerticalScrollBar().setUnitIncrement(16);      

        stan.drawPanel = Albert;
        Albert.scroll = elder;
       
        
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);

        this.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
            @Override
            public void ancestorMoved(HierarchyEvent e) {}
            @Override
            public void ancestorResized(HierarchyEvent e) {
                String s = e.paramString();
                int p = 0;
                for (int i=0; i<3; i++){
                    p = s.indexOf(',', p+1);
                }
                int p2 = s.indexOf('x', p+1);
                int p3 = s.indexOf(',', p2+1);
                int w = Integer.parseInt(s.substring(p+1, p2));
                int h = Integer.parseInt(s.substring(p2+1, p3));
                Albert.stretch(w,h);
            }
        });               
	    
	    Albert.status = status;
	    c.setFileFilter(filter);
	    
	    tempoField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            	curNota = Pointer.curNota;
            	                
                switch (e.getKeyCode()) {
	                case KeyEvent.VK_CONTROL:
	                	ctrl = true;
	                    break;
                
                    case KeyEvent.VK_RIGHT:
                        playMusThread.shutTheFuckUp();
                        if (Pointer.curNota.isTriol)
                            Pointer.move(3, true);
                        else {
                            Pointer.move(1, true);
                        }

                        //stan.drawPanel.checkCam();
                        break;
                    case KeyEvent.VK_LEFT:
                        playMusThread.shutTheFuckUp();
                        Pointerable n = Pointer.curNota;
                        if ( (n=n.prev)==null?false:(n=n.prev)==null?false:(n=n.prev)==null?false:n.isTriol )
                            Pointer.move(-3, true);
                        else {
                            Pointer.move(-1, true);
                        }

                        //stan.drawPanel.checkCam();
                        break;
                    case KeyEvent.VK_UP:
                        playMusThread.shutTheFuckUp();
                        Pointer.move(-Albert.stepInOneSys/2+2, true);
                        stan.drawPanel.checkCam();
                        break;
                    case KeyEvent.VK_DOWN:
                        playMusThread.shutTheFuckUp();
                        Pointer.move(Albert.stepInOneSys/2-2, true);
                        stan.drawPanel.checkCam();
                        break;
                    case KeyEvent.VK_HOME:
                        playMusThread.shutTheFuckUp();
                    	Pointer.moveToBegin();
                        stan.drawPanel.checkCam();
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
                    case KeyEvent.VK_TAB:
                    	System.out.println("Вы нажали Tab!");
                    	stan.nextAcc();
                        break;
                        
                    case KeyEvent.VK_ADD:
                    	System.out.println("Вы нажали плюс!");  
	                    if (curNota instanceof Nota) {
	                        if (stan.isChanSep == false) curNota.changeDur(1, false);
	                        else stan.Acc.changeDur(1, true);
	                        Albert.repaint();
	                        
                    	} else if (curNota instanceof Phantom) {
                            ((Phantom)curNota).changeValue(1);
                    	}
                        break;
                    case KeyEvent.VK_SUBTRACT:
                    	System.out.println("Вы нажали минус!");
                        if (curNota instanceof Nota) {
                            if (stan.isChanSep == false) curNota.changeDur(-1, false);
                            else stan.Acc.changeDur(-1, true);
                    	    Albert.repaint();
                        } else if (curNota instanceof Phantom) {
                            ((Phantom)curNota).changeValue(-1);
                        }
                        break;
                    case KeyEvent.VK_DELETE:
                    	System.out.println("Вы нажали Delete!");
                        if (curNota instanceof Nota) stan.delNotu();
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
                        // И ёжику ясно, что надо стереть последний символ... а если ты хочешь стереть не последний
                        // или копипастить текст - хуй тебе в руки... Для начала и так неплохо, но, естественно,
                        // не забыть добавить альтернативный способ ввода (типа, нажимаешь кнопку, и появляется
                        // окошко для редактирования текста)
                    	if (curNota instanceof Nota == false) {
                            ((Phantom)curNota).backspace();
                            break;
                        }
                        String slog = curNota.slog;
                        if (slog.length() < 2) {
                            curNota.slog = "";
                        } else {
                            ((Nota)curNota).setSlog(  slog.substring(0, slog.length()-1)  );
                        }
                        Albert.repaint();
                        break;
                    default:
                    	if (ctrl) {
                    		switch(e.getKeyCode()) { // Возможно, здесь есть ошибка
                                case 'z': case 'Z': case 'Я': case 'я':
                                    System.out.println("Вы нажали контрол-З");
                                    stan.retrieveNotu();
                                    break;
                                case 'y': case 'Y': case 'Н': case 'н':
                                    System.out.println("Вы нажали контрол-У");
                                    stan.detrieveNotu();
                                    break;
                                case 's': case 'S': case 'Ы': case 'ы':
                                    System.out.println("Вы нажали ctrl+s!");
                                    int rVal = c.showSaveDialog(GraphMusica.this);
                                    if (rVal == JFileChooser.APPROVE_OPTION) {
                                        File fn = c.getSelectedFile();
                                        if (!fn.getAbsolutePath().endsWith(".klsn")) {
                                            fn = new File(fn + ".klsn");
                                        }

                                        stan.saveFile(fn);
                                    }
                                    if (rVal == JFileChooser.CANCEL_OPTION) {
                                        break;
                                    }
                                    break;
                                case 'o': case 'O': case 'щ': case 'Щ':
                                    System.out.println("Вы нажали ctrl+o!");
                                    int i = okcancel("Are your sure? Unsaved data will be lost."); // 2 - cancel, 0 - ok
                                    if (i == 0) {
                                        int sVal = c.showOpenDialog(GraphMusica.this);
                                        if (sVal == JFileChooser.APPROVE_OPTION) {
                                            stan.klsnOpen(c.getSelectedFile());
                                        }
                                        if (sVal == JFileChooser.CANCEL_OPTION) {
                                            break;
                                        }
                                    }
                                    break;
                                case 'P': case 'p': case 'З': case 'з':
                                    System.out.println("Вы нажали ctrl+p!");
                                    if (stan.stop)
                                        stan.playEntire();
                                    else
                                        stan.stopMusic();
                                    break;
                                case 'h': case 'H': case 'р': case 'Р':
                                    System.out.println("ctrl+h");
                                    stan.drawPanel.incHeight(1);
                                    break;
                                case 'w':case 'W': case 'Ц': case 'ц':
                                    System.out.println("ctrl+h");
                                    stan.drawPanel.incWidth(1);
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
                    	}
                        if (ctrl) {
                            ctrl = false;
                            break;
                        }
                        if (stan.mode == NotnyStan.aMode.playin) break;

                    	if (curNota instanceof Nota == false) {
                            ((Phantom)curNota).tryToWrite( e.getKeyChar() );
                            break;
                        }
                        System.out.println("Keycode "+e.getKeyCode());
                    	if (e.getKeyCode() >= 32 || e.getKeyCode() == 0) {
                    		// Это символ - напечатать
                    		((Nota)curNota).setSlog(  curNota.slog.concat( "" + e.getKeyChar() )  );
                    	}

                    	System.out.println(curNota.slog);
                        Albert.repaint();
                        break;
                }
            }
            public void keyReleased(KeyEvent e) {
            	switch (e.getKeyCode()) {
        		case KeyEvent.VK_CONTROL: 
        			ctrl = false; 
        			break;
            	}
            }
            public void keyTyped(KeyEvent e) {  }
        });
	    JPanel  topMenu = new JPanel();        
        this.add( topMenu, BorderLayout.NORTH );;
	    topMenu.add(status);
	    tempoField.setFocusTraversalKeysEnabled(false);
	    topMenu.add(tempoField);
	    tempoField.addActionListener(this);
	    
    }
    

    
    public static int okcancel(String theMessage) {
        int result = JOptionPane.showConfirmDialog((Component) null, theMessage,
            "alert", JOptionPane.OK_CANCEL_OPTION);
        return result;
      }



	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
		
	}

}