package Tools;

import Musica.NotnyStan;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.*;

public class Phantom extends Pointerable{	

	@Override
	public BufferedImage getImage() {
		int w = 32;
		int h = 32;
		BufferedImage rez = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = rez.getGraphics();
		g.setColor(Color.black);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // 7px width
		g.drawString(cislic+"", 0, 0+12);
		g.drawLine(0, 32, 32, 0);
		g.drawString(znamen+"", 14, 28);
		return rez;
	}

    NotnyStan stan;

	public Phantom(NotnyStan stan){
        this.stan = stan;
        znamen = 8;
        cislic = 8;
	}

	@Override
	public void changeDur(int i, boolean b) {
		// TODO Auto-generated method stub
		
	}

    private enum WhatToChange {
        cislicelj,
        znamenatelj,
        tempo,
        volume;
    }
    private WhatToChange changeMe = WhatToChange.cislicelj;

	public void tabPressed() {
		// TODO
	}

    public int valueTempo = 120;
    public double valueVolume = 0.5;
    public int tryToWrite( char c ) {
        if (c < '0' || c > '9') return -1;
        switch (changeMe) { // Monkey-coding, 'cause java doesn't have c-like pointers
            case cislicelj: // and Integer doesn't have .setValue() method and I don't want to do second switch
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
                valueVolume += (c - '0')/100;
                if (valueVolume > 9.99) valueVolume = 9.99;
                break;
            default:
                System.out.println("Неизвестный енум");
                break;
        } // switch(enum)
        stan.checkValues(this);
        return 0;
    }

    public void changeValue(int n) {
        // TODO
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
                valueVolume += n/100;
                if (valueVolume < 0) valueVolume = 0;
                if (valueVolume > 9.99) valueVolume = 9.99;
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
                valueVolume /= 10;
                break;
            default:
                System.out.println("Неизвестный енум");
                break;
        } // switch(enum)
        stan.checkValues(this);
    }

    private double log2(int n){
        return Math.log(n)/Math.log(2);
    }
	
}
