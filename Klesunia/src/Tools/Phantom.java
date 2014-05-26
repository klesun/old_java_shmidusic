package Tools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.*;

import Musica.Nota;

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
	
}
