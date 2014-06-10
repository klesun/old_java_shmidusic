package Tools;

import GraphTmp.DrawPanel;
import Musica.Nota;
import Musica.NotnyStan;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.*;

public class Phantom extends Pointerable{	

	@Override
	public BufferedImage getImage() {
		int w = 128;
		int h = 256;
		BufferedImage rez = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = rez.getGraphics();
		g.setColor(Color.black);

        int tz=znamen, tc = cislic;
        while (tz>4 && tc%2==0) {
            tz /= 2;
            tc /= 2;
        }
        int inches = 25, taktX= 0, taktY=80;
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, inches)); // 12 - 7px width
		g.drawString(tc+"", 0 + taktX, inches*4/5 + taktY);
        int delta = 0 + (tc>9 && tz<10? inches*7/12/2: 0) + ( tc>99 && tz<100?inches*7/12/2:0 );
		g.drawString(tz+"", delta + taktX, 2*inches*4/5 + taktY);

        int tpx = 0, tpy = 0;
        g.drawImage(Nota.notaImg[3], tpx, tpy, null);
        inches = 18;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, inches)); // 12 - 7px width
        g.drawString(" = "+valueTempo, tpx + 20, tpy + inches*4/5 + 30 - 4);

        tpx = 0; tpy = 148;
        g.drawImage(DrawPanel.vseKartinki[4], tpx+2, tpy, null);
        inches = 12;
        g.setColor(Color.decode("0x00A13E"));
        g.setFont(new Font(Font.SERIF, Font.BOLD, inches)); // 12 - 7px width
        g.drawString((int)(valueVolume*100)+"%", tpx, tpy + inches*4/5 + 16);


		return rez;
	}

    NotnyStan stan;

	public Phantom(NotnyStan stan){
        this.stan = stan;
        znamen = 8;
        cislic = 8;
        underPtr = true;
	}

	@Override
	public void changeDur(int i, boolean b) {
		// TODO Auto-generated method stub
		
	}

    public enum WhatToChange {
        cislicelj,
        znamenatelj,
        tempo,
        volume;
    }
    public WhatToChange changeMe = WhatToChange.cislicelj;

	public void tabPressed() {
        switch (changeMe) {
            case cislicelj:
                changeMe = WhatToChange.tempo;
                break;
            case znamenatelj:
                changeMe = WhatToChange.cislicelj;
                break;
            case tempo:
                changeMe = WhatToChange.volume;
                break;
            case volume:
                changeMe = WhatToChange.cislicelj;
                break;
            default:
                changeMe = WhatToChange.cislicelj;
                System.out.println("Неизвестный енум");
                break;
        } // switch(enum)
	}

    public int valueTempo = 120;
    public double valueVolume = 0.5;
    public int tryToWrite( char c ) {
        if (c < '0' || c > '9') return -1;
        switch (changeMe) {
            case cislicelj:
                cislic *= 10;
                cislic += c - '0';
                if (cislic > 256) cislic = 256;
                break;
            case znamenatelj:
                znamen *= 10;
                znamen += c - '0';
                // TODO: возможно, ошибка здесь
                if (Math.abs(  Math.round( log2(znamen) ) - log2(znamen)  ) > 0.0001) // Если это степень двойки
                    znamen = (int)Math.pow( 2, Math.round( log2(znamen) ) );
                if (znamen >= 64) znamen = 64;
                break;
            case tempo:
                valueTempo *= 10;
                valueTempo += c - '0';
                if (valueTempo > 12000) valueTempo = 12000;
                break;
            case volume:
                valueVolume *= 10;
                System.out.println("c="+c+" c-'0'="+(c-'0'));
                valueVolume += ((double)(c-'0'))/100;
                if (valueVolume > 2.54) valueVolume = 2.54;
                break;
            default:
                System.out.println("Неизвестный енум");
                break;
        } // switch(enum)
        stan.checkValues(this);
        return 0;
    }

    public void changeValue(int n) {
        switch (changeMe) {
            case cislicelj:
                if (cislic > 255 && n>0) return;
                cislic += n;
                if (cislic < 1) cislic = 1;
                break;
            case znamenatelj:
                znamen = (int)Math.ceil(znamen*Math.pow(2.0, n));
                if (znamen < 1) znamen = 1;
                if (znamen > 64) znamen = 64;
                break;
            case tempo:
                valueTempo += n;
                if (valueTempo < 1) valueTempo = 1;
                if (valueTempo > 12000) valueTempo = 12000;
                break;
            case volume:
                valueVolume += ((double)n)/100;
                if (valueVolume < 0) valueVolume = 0;
                if (valueVolume > 2.54) valueVolume = 2.54;
                break;
            default:
                System.out.println("Неизвестный енум");
                break;
        } // switch(enum)
        stan.checkValues(this);
    }

    public void backspace() {
        switch (changeMe) { // Monkey-coding, 'cause java doesn't have c-like pointers
            case cislicelj: // and Integer doesn't have .setValue() method
                cislic /= 10;
                if (cislic < 1) cislic = 1;
                break;
            case znamenatelj:
                znamen /= 10;
                if (znamen < 1) znamen = 1;
                break;
            case tempo:
                valueTempo /= 10;
                if (valueTempo < 1) valueTempo = 1;
                break;
            case volume:
                int tmp = (int)(valueVolume*100);
                tmp /= 10;
                valueVolume = ((double)tmp)/100;

                break;
            default:
                System.out.println("Неизвестный енум");
                break;
        } // switch(enum)
        stan.checkValues(this);
    }

    public void setCislicFromFile( int fileCis ) {
        fileCis /= 8;
        cislic = fileCis;
    }

    private double log2(int n){
        return Math.log(n)/Math.log(2);
    }
	
}
