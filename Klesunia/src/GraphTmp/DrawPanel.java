package GraphTmp;

import Musica.*;
import Tools.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import Tools.Phantom;

public class DrawPanel extends JPanel {
	JScrollPane scroll; 
	Status status;

    public static int STEPY = 5; // Графика
    public static int STEPX = 20; // Графика
    double MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек
    double MARGIN_H = 1;
    int MARX = (int)Math.round(MARGIN_H* STEPX);
    int MARY = (int)Math.round(MARGIN_V* STEPY);
    int notnMar = 4;
    int gPos = MARX;
    int maxgPos = 0;
    int to4kaOt4eta = 0;
    int toOtGraph = 38* STEPY;
    int maxy = 0;
    int SISDISPLACE = 40;

    int width = this.getWidth(), height = this.getHeight();
    int stepInOneSys = (int)Math.floor(width / STEPX - 2*MARGIN_H);

    private BufferedImage vikey;
    private BufferedImage bakey;
    private BufferedImage bemol;
    private BufferedImage ptrImg;
    public static BufferedImage volImg;

    NotnyStan stan;

    GeneralPath triang;

    Set<Nota> stillPlayin = new TreeSet<Nota>();
    
    public DrawPanel(final NotnyStan stan) {
        this.stan = stan;
        stan.stepInOneSys = stepInOneSys;
        URL curUr = getClass().getResource("../");
        System.out.println(curUr.getPath());
        URL keyRes = getClass().getResource("../imgs/vio_sized.png");
        URL basRes = getClass().getResource("../imgs/bass_sized.png");
        URL bemRes = getClass().getResource("../imgs/flat_sized.png");
        URL ptrRes = getClass().getResource("../imgs/MyPointer.png");
        URL volRes = getClass().getResource("../imgs/volume.png");
        

        try { vikey = ImageIO.read(keyRes);
        		bakey = ImageIO.read(basRes);
                bemol = ImageIO.read(bemRes);                 
                ptrImg = ImageIO.read(ptrRes);
                volImg = ImageIO.read(volRes);
        } catch (IOException e) { e.printStackTrace(); System.out.println("Темнишь что-то со своей картинкой..."); }

        int xPoints[] = { 6, 11, 16 };
        int yPoints[] = { 0, 25, 0 };
        triang = new GeneralPath();
        triang.moveTo( xPoints[ 0 ], yPoints[ 0 ] );
        for ( int k = 1; k < xPoints.length; k++ )
            triang.lineTo( xPoints[ k ], yPoints[ k ] );
        triang.closePath();
        stan.drawPanel = this;
        stan.checkValues(stan.phantomka);
    }

    int curCislic = 0;
    int taktCount = 1;
    
    int triol = 0;
    public void paintComponent(Graphics g) {
    	status.renew();
        this.setPreferredSize(new Dimension(width, 600+maxy));	//	Needed for the scroll bars to appear
        g.setColor(Color.WHITE);
        g.fillRect(0,0,this.getWidth(),this.getHeight());
        g.setColor(Color.BLUE);
        g.drawImage(bakey, STEPX, 11* STEPY + MARY +2, this);
        g.drawImage(vikey, STEPX, MARY - 3* STEPY, this);
        this.to4kaOt4eta = stan.to4kaOt4eta;
        gPos = MARX + notnMar* STEPX -2* STEPX;
        curCislic = 0;
        
        taktCount = 1;
        for (Pointerable anonimus = Pointer.beginNota; anonimus != null; anonimus = anonimus.next) {
            if (anonimus instanceof Phantom) {
            	out(gPos+" "+MARY);
                drawPhantom((Phantom)(anonimus), g);
                gPos += 2* STEPX *anonimus.gsize;
            }
        	if (anonimus instanceof Nota == false) continue;
        	Nota theNota = (Nota)anonimus;

            if (theNota.isTriol) {
                triol = 1;
                curCislic += theNota.getAccLen();
            } else if (triol == 0 || triol == 3 || triol == 4) {
                if (triol != 3) curCislic += theNota.getAccLen();
                if (curCislic / stan.cislic > 0) {
                    curCislic %= stan.cislic;
                    g.setColor(Color.BLACK);
                    if (curCislic > 0) {
                        g.setColor(Color.GRAY);
                    }
                    g.drawLine(gPos + STEPX * 3 / 2, MARY - STEPY * 5, gPos + STEPX * 3 / 2, MARY + STEPY * 20);
                    g.setColor(Color.decode("0x00A13E"));
                    g.drawString(taktCount + "", gPos + STEPX, MARY - STEPY * 9);

                    ++taktCount;
                }
                if (triol == 4) triol = 0;
            }
            g.setColor(Color.BLACK);

        	Nota tmp = theNota;        	
        	
        	// Рисуем нотный стан
        	if (gPos >= stepInOneSys* STEPX) {
            	// Переходим на новую октаву
            	g.drawImage(bakey, STEPX, (SISDISPLACE+11)* STEPY + MARY +2, this);
                g.drawImage(vikey, STEPX, MARY + (SISDISPLACE-3)* STEPY, this);
            	for (int j=0; j<(stan.bassKey? 11: 5); ++j){
                    if (j == 5) continue;
                    g.drawLine(MARX, MARY + j* STEPY *2, width - MARX, MARY + j* STEPY *2);
                }
                

                gPos = MARX + notnMar* STEPX;
                MARY += SISDISPLACE* STEPY;
                
            }
        	tmp = theNota;
            boolean checkVa = false;
            while (tmp!=null) {
            	if (tmp.okt > 6) checkVa = true;
            	tmp = tmp.accord;
            }
            g.setColor(Color.BLUE);
        	if (checkVa) {
        		g.drawString("8va", gPos, MARY-4* STEPY);
        		MARY += 7* STEPY;
        	}


            Nota rezNota =(Nota) theNota;
            while (theNota != null){
            	drawNotu( (Nota)theNota, g);
            	theNota = (Nota)theNota.accord;
            }
            theNota = rezNota;

            
            
            if (checkVa) {
        		MARY -= 7* STEPY;
        	}
            
            gPos += 2* STEPX *theNota.gsize;
            
        }
        maxgPos = gPos;
        for (int i=0; i<(stan.bassKey? 11: 5); ++i){
            if (i == 5) continue;
            g.drawLine(MARX, MARY + i* STEPY *2, width - MARX, MARY + i* STEPY *2);
        }        

        if (MARY>maxy) maxy=MARY;
        MARY = (int)Math.round(MARGIN_V* STEPY);
        g.drawString(Pointer.AcNo+"", 20, 10);

        this.revalidate();	//	Needed to recalc the scroll bars
    } // paintComponent

    int tp = 0;

    void stretch(int w, int h){
    	if (width > w) {
    		// Сейчас он по-блядски удвойняет вертикальный скролбар - поправь блядь
    	}
        width = w;
        height = h;
        stepInOneSys = (int)Math.floor(width / STEPX - 2*MARGIN_H);
        stan.stepInOneSys = stepInOneSys;
    }

    int x0, y0, x1,y1;
    private int drawNotu(Nota theNota, Graphics g) {
    	    	
    	int thisY = MARY + toOtGraph - STEPY * (theNota.pos + theNota.okt * 7);
    	
        if (theNota.isBemol) {
            g.drawImage(bemol, gPos-(int)Math.round(0.5* STEPX), thisY + 3* STEPY +2, this);
        } // Хочу, чтобы он рисовал от ноты, поэтому не инкапсулировал бемоль

        g.drawImage(theNota.getImage(), gPos, thisY, this);
        if (triol == 1) {
            x0 = gPos+20-4;
            y0 = thisY;
        } else if (triol == 3) {
            x1 = gPos+20-4;
            y1 = thisY;
            g.setColor(Color.BLACK);
            g.drawLine(x0,y0, x1,y1);
            g.drawLine(x0,y0-3, x1,y1-3);
            g.drawLine(x0,y0-1, x1,y1-1);
            g.drawLine(x0,y0-2, x1,y1-2);
            g.drawString("3",x0+(x1-x0)/2 - 6, y0+(y1-y0)/2 - 10);
        }
        if (triol != 0) ++triol;
        if (theNota.underPtr) g.drawImage( ptrImg, gPos, MARY- STEPY *14, this );
    	
        int n = theNota.cislic;
    	boolean to4ka = false;
    	if (n % 3 == 0) {
			to4ka = true;
			n -= n/3;
		}			    	
    	g.setColor(Color.BLACK);
    	if (to4ka) g.fillOval(gPos + 20, thisY + 35, 4, 4);
        
    	boolean chet = (theNota.pos % 2 == 1) ^ (theNota.okt % 2 == 1);
    	if (theNota.okt > 6) chet = !chet;
        if (chet) g.drawLine(gPos - 4, thisY + STEPY * 7, gPos + 22, thisY + STEPY * 7);
    	// полоска для до...
        if (theNota.slog != null && theNota.slog != ""){
        	g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            g.drawString(theNota.slog, gPos, MARY-6* STEPY);
        }

    	return 0;
    } 
    private void drawPhantom(Phantom phantomka, Graphics g) {
        int dX=5, dY=80;
        g.drawImage( phantomka.getImage(), gPos-dX, MARY-dY, this );
        if (phantomka.underPtr) {
            int deltaY = 0;
            switch (phantomka.changeMe) {
                case cislicelj:
                    deltaY += 7*STEPY;
                    break;
                case znamenatelj:
                    deltaY += 11*STEPY;
                    break;
                case tempo:
                    deltaY -= 1*STEPY;
                    break;
                case volume:
                    deltaY += 24*STEPY;
                    break;
                default:
                    System.out.println("Неизвестный енум в ДоуПанеле");
                    break;
            }
            g.drawImage( ptrImg, gPos - 7, MARY- STEPY *14 + deltaY, this );
        }
    } 
    
    public void checkCam(){
    	JScrollBar vertical = scroll.getVerticalScrollBar();
    	vertical.setValue( SISDISPLACE* STEPY * (Pointer.pos / (stepInOneSys/2-2)  -1) );
    	repaint();
    }
    
    public void page(int pageCount) {
    	JScrollBar vertical = scroll.getVerticalScrollBar();
    	int pos = vertical.getValue()+pageCount*SISDISPLACE* STEPY;
    	if (pos<0) pos = 0;
    	if (pos>vertical.getMaximum()) pos = vertical.getMaximum();
    	vertical.setValue(pos);
    	repaint();
    }
    
    int getSys(int n){
    	return n/(stepInOneSys/2-2);
    }
    
    private void out(String str) {
    	System.out.println(str);
    }
}

