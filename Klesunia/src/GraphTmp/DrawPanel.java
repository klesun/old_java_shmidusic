package GraphTmp;

import Musica.*;
import Tools.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DrawPanel extends JPanel {
	JScrollPane scroll; 
	Status status;
	
    int STEP_V = 5; // Графика
    int STEP_H = 20; // Графика
    double MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек
    double MARGIN_H = 1;
    int MARX = (int)Math.round(MARGIN_H*STEP_H);
    int MARY = (int)Math.round(MARGIN_V*STEP_V);
    int notnMar = 4;
    int gPos = MARX;
    int maxgPos = 0;
    int to4kaOt4eta = 0;
    int toOtGraph = 38*STEP_V;
    int maxy = 0;
    int SISDISPLACE = 40;

    int width = this.getWidth(), height = this.getHeight();
    int stepInOneSys = (int)Math.floor(width / STEP_H - 2*MARGIN_H);

    Nota baseNota = new Nota(to4kaOt4eta, 1);

    private BufferedImage vikey;
    private BufferedImage bakey;
    private BufferedImage bemol;
    
    private BufferedImage notaImg[] = new BufferedImage[8];
    
    NotnyStan stan;

    GeneralPath triang;

    Set<Nota> stillPlayin = new TreeSet<Nota>();
    
    public DrawPanel(final NotnyStan stan) {
        this.stan = stan;
        stan.stepInOneSys = stepInOneSys;
        
        URL keyRes = getClass().getResource("/imgs/vio_sized.png");
        URL basRes = getClass().getResource("/imgs/bass_sized.png");
        URL bemRes = getClass().getResource("/imgs/flat_sized.png");
        

        try {   vikey = ImageIO.read(keyRes);
        		bakey = ImageIO.read(basRes);
                bemol = ImageIO.read(bemRes);                 
        } catch (IOException e) { e.printStackTrace(); System.out.println("Темнишь что-то со своей картинкой..."); }

        int xPoints[] = { 6, 11, 16 };
        int yPoints[] = { 0, 25, 0 };
        triang = new GeneralPath();
        triang.moveTo( xPoints[ 0 ], yPoints[ 0 ] );
        for ( int k = 1; k < xPoints.length; k++ )
            triang.lineTo( xPoints[ k ], yPoints[ k ] );
        triang.closePath();
    }

    int curCislic = 0;
    int taktCount = 1;
    
    boolean kostil = true;
    int lastSis = 0;
    public void paintComponent(Graphics g) {
    	status.renew();
    	
        this.setPreferredSize(new Dimension(width, 600+maxy));	//	Needed for the scroll bars to appear

        Graphics2D g2d = ( Graphics2D ) g;

        g.setColor(Color.WHITE);
        g.fillRect(0,0,this.getWidth(),this.getHeight());
        g.setColor(Color.BLUE);
        
        g.drawImage(bakey, STEP_H, 11*STEP_V + MARY +2, this);
        g.drawImage(vikey, STEP_H, MARY - 3*STEP_V, this);
                
        this.to4kaOt4eta = stan.to4kaOt4eta;
        //baseNota.tune = to4kaOt4eta;

        gPos = MARX + notnMar*STEP_H;
        curCislic = 0;
        
        // 
        
        taktCount = 1;
        for (Pointerable anonimus = stan.ptr.beginNota.next; anonimus != null; anonimus = anonimus.next) {
            if (anonimus instanceof Phantom) {
            	drawPhantom((Phantom)(anonimus), g);
            }
        	if (anonimus instanceof Nota == false) continue;
        	
        	Nota theNota = (Nota)anonimus;
        	curCislic += theNota.getAccLen();
        	if (curCislic / stan.cislic > 0) {
        		// drawTakt
        		boolean bo = false;
        		        		
        		curCislic %= stan.cislic;
        		if (curCislic > 0) {
        			g.setColor(Color.RED);
        		} 
        		g.drawLine(gPos + STEP_H*3/2, MARY - STEP_V*5, gPos + STEP_H*3/2, MARY + STEP_V*20);        	
        		g.setColor(Color.BLACK);;
        		g.drawString(taktCount+"", gPos + STEP_H, MARY - STEP_V*7);
        		
        		++taktCount;        		
        	}
        	
        	Nota tmp = theNota;        	
        	
        	// Рисуем нотный стан
        	if (gPos >= stepInOneSys*STEP_H) {
            	// Переходим на новую октаву
            	g.drawImage(bakey, STEP_H, (SISDISPLACE+11)*STEP_V + MARY +2, this);
                g.drawImage(vikey, STEP_H, MARY + (SISDISPLACE-3)*STEP_V, this);
            	for (int j=0; j<(stan.bassKey? 11: 5); ++j){
                    if (j == 5) continue;
                    g.drawLine(MARX, MARY + j*STEP_V*2, width - MARX, MARY + j*STEP_V*2);
                }
                

                gPos = MARX + notnMar*STEP_H;
                MARY += SISDISPLACE*STEP_V;
                
            }
            
            boolean checkVa = false;
            tmp = theNota;
            while (tmp!=null) {
            	if (tmp.okt > 6) checkVa = true;
            	tmp = tmp.accord;
            }

            if (theNota.slog != null && theNota.slog != ""){
                g.drawString(theNota.slog, gPos, MARY-6*STEP_V);
            }
            g.setColor(Color.BLUE);
        	if (checkVa) {
        		g.drawString("8va", gPos, MARY-4*STEP_V);
        		MARY += 7*STEP_V;
        	}


            Nota rezNota =(Nota) theNota;
            while (theNota != null){
            	drawNotu( (Nota)theNota, g);
            	theNota = (Nota)theNota.accord;
            }
            theNota = rezNota;

            
            
            if (checkVa) {
        		MARY -= 7*STEP_V;
        	}
            
            gPos += 2*STEP_H;
            
        }
        maxgPos = gPos;
        for (int i=0; i<(stan.bassKey? 11: 5); ++i){
            if (i == 5) continue;
            g.drawLine(MARX, MARY + i*STEP_V*2, width - MARX, MARY + i*STEP_V*2);
        }        

        g2d.translate( ( (stan.ptr.pos*2 +1) % (stepInOneSys - notnMar) + notnMar)*STEP_H, STEP_V*SISDISPLACE*getSys(stan.ptr.pos)/*MARY + STEP_V*12*/ );
        g2d.setColor(new Color(0, 191, 0));
        g2d.fill(triang);
        if (MARY>maxy) maxy=MARY;
        MARY = (int)Math.round(MARGIN_V*STEP_V);
        g.drawString(stan.ptr.AcNo+"", 20, 10);

        this.revalidate();	//	Needed to recalc the scroll bars
    }

    int tp = 0;

    void stretch(int w, int h){
    	if (width > w) {
    		// Сейчас он по-блядски удвойняет вертикальный скролбар - поправь блядь
    	}
        width = w;
        height = h;
        stepInOneSys = (int)Math.floor(width / STEP_H - 2*MARGIN_H);
        stan.stepInOneSys = stepInOneSys;
    }
       
    boolean is8v = false;
    private int drawNotu(Nota theNota, Graphics g) {    	
    	    	
    	int thisY = MARY + toOtGraph - STEP_V * (theNota.pos + theNota.okt * 7);
    	
        if (theNota.isBemol) {
            g.drawImage(bemol, gPos-(int)Math.round(0.5*STEP_H), thisY + 3*STEP_V +2, this);
        } // Хочу, чтобы он рисовал от ноты, поэтому не инкапсулировал бемоль
        
       // g.drawImage(notaImg[idx], gPos, thisY, this);
        g.drawImage( theNota.getImage(), gPos, thisY, this );        
    	
        int n = theNota.durCislic;
    	boolean to4ka = false;
    	if (n % 3 == 0) {
			to4ka = true;
			n -= n/3;
		}			    	
    	g.setColor(Color.BLACK);
    	if (to4ka) g.fillOval(gPos + 20, thisY + 35, 4, 4);
        
    	boolean chet = (theNota.pos % 2 == 1) ^ (theNota.okt % 2 == 1);
    	if (theNota.okt > 6) chet = !chet;
        if (chet) g.drawLine(gPos - 4, thisY + STEP_V*7, gPos + 22, thisY + STEP_V*7);
    	// полоска для до...

    	return 0;
    } 
    private void drawPhantom(Phantom theNota, Graphics g) {
    	// TODO: написать    	
    } 
    
    public void checkCam(){
    	JScrollBar vertical = scroll.getVerticalScrollBar();
    	vertical.setValue( SISDISPLACE*STEP_V * (stan.ptr.pos / (stepInOneSys/2-2)  -1) );
    	repaint();
    }
    
    int getSys(int n){
    	return n/(stepInOneSys/2-2);
    }
}

