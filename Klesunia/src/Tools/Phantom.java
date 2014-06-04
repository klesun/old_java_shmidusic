package Tools;

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
		g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
		g.drawString(durCislic/8+"", 0, 0+12);
		g.drawLine(0, 32, 32, 0);
		g.drawString(durZnamen/8+"", 14, 28);
		return rez;
	}
	
	public Phantom(){
	}

	@Override
	public void changeDur(int i, boolean b) {
		// TODO Auto-generated method stub
		
	}

	/*
	public static void main(String[] args) {
		System.out.println("Начали");
		Phantom child = new Phantom();
		BufferedImage img = child.getImage();
		try {
			ImageIO.write(img, "png", new File("/home/irak/asdqwezxc.png"));
			System.out.println("Записали!");
		} catch(Exception e) {
			System.out.println("Не хочет сохранять картинку");
		}
		System.out.println("Закончили");
	}
	*/
    private enum WhatToChange {
        cislicelj,
        znamenatelj,
        tempo,
        volume;
    }
    private WhatToChange changeMe;

	public void tabPressed() {
		// TODO
	}

    Integer valueZnam = 64;
    Integer valueCislic = 64;
    Integer valueTempo = 120;
    Integer valueVolume = 127;
    public int tryToWrite( char c ) {
        if (c < '0' || c > '9') return -1;
        Integer value;
        Object obj = new Object();
        switch (changeMe) {
            case cislicelj:
                value = valueCislic; // TODO: проверь, всё ли с этим хорошо, он присваевает поинтер или значение?
                obj = valueCislic;
                System.out.println(value.equals(valueCislic));
                break;
            case znamenatelj:
                value = valueZnam;
                obj = valueZnam;
                break;
            case tempo:
                value = valueTempo;
                obj = valueTempo;
                break;
            case volume:
                value = valueVolume;
                obj = valueVolume;
                break;
            default:
                value = valueCislic;
                obj = valueCislic;
                System.out.println("Неизвестный енум");
        }
        Integer intgr = (Integer)obj;
        System.out.println("valueCislic = "+valueCislic);
        intgr *= 10;
        intgr += c - '0';
        value *= 10;
        value += c - '0';
        System.out.println("valueCislic = "+valueCislic);

        return 0;
    }

    public void changeValue(int n) {
        // TODO
        /*
        if ( (accord != null) && (single == false) ) accord.changeDur(n, false);

        if (durCislic == durZnamen*2) {
            durCislic = durZnamen;
            n = 0;
        }
        if (durCislic < 4) {
            durCislic = 8;
            n = 0;
        }
        while (n > 0){
            if (durCislic % 3 == 0) {
                durCislic += durCislic/3;
            } else {
                durCislic += durCislic/2;
            }
            --n;
        }
        while (n < 0){
            if (durCislic % 3 == 0) {
                durCislic -= durCislic/3;
            } else {
                durCislic -= durCislic/4;
            }
            ++n;
        }*/
    }

    public void backspace() {
        //value /= 10;
        // TODO
    }
	
}
