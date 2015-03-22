package Gui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import Gui.staff.pointerable.Nota;

public class BitmapGenerator {
//	private static BitmapGenerator instance = null;
//
//	public BufferedImage[] vseKartinki = new BufferedImage[6]; // TODO: поменять этот уродский массив на ключ-значение
//	public BufferedImage[] vseKartinki0 = new BufferedImage[6];
//
//	public int notaHeight = 32;
//	public final static int NORMAL_HEIGHT = 40;
//	public int notaWidth = 20;
//	public final static int NORMAL_WIDTH = 25;
//	public int STEPY = notaHeight/8; // Графика
//	public int STEPX = notaWidth; // Графика
//	double MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек
//	double MARGIN_H = 1;
//	int MARX = (int)Math.round(MARGIN_H* STEPX);
//	int MARY = (int)Math.round(MARGIN_V* STEPY);
//
//	public static BitmapGenerator getInstance() {
//		if (instance == null) {
//			instance = new BitmapGenerator();
//		}
//		return instance;
//	}
//
//	private BitmapGenerator() {
//	    URL curUr = getClass().getResource("../");
//	    System.out.println(curUr.getPath());
//	    String keyRes = "imgs/vio_sized.png";
//	    String basRes = "imgs/bass_sized.png";
//	    String bemRes = "imgs/flat_sized.png";
//	    String ptrRes = "imgs/MyPointer.png";
//	    String volRes = "imgs/volume.png";
//		String instrRes = "imgs/instrument.png";
//	    try {	vseKartinki[0] = ImageIO.read(new File(keyRes));
//				vseKartinki[1] = ImageIO.read(new File(basRes));
//				vseKartinki[2] = ImageIO.read(new File(bemRes));
//				vseKartinki[3] = ImageIO.read(new File(ptrRes));
//				vseKartinki[4] = ImageIO.read(new File(volRes));
//				vseKartinki[5] = ImageIO.read(new File(instrRes));
//	    } catch (IOException e) { e.printStackTrace(); System.out.println("Темнишь что-то со своей картинкой..."); }
//	    for (int i = 0; i < vseKartinki.length; ++i ) {
//	        vseKartinki0[i] = vseKartinki[i];
//	        vseKartinki[i] = changeSize(i);
//	    }
//	}
//
//	private void refresh() {
//	    STEPY = notaHeight/8;
//	    STEPX = notaWidth;
//	    for (int i = 0; i < vseKartinki.length; ++i ) { // >_<
//	        vseKartinki[i] = changeSize(i);
//	    }
//	    MARX = (int)Math.round(MARGIN_H* STEPX);
//	    MARY = (int)Math.round(MARGIN_V* STEPY);
//	    toOtGraph = 38* STEPY;
//	    stepInOneSys = (int)Math.floor(width / STEPX - 2*MARGIN_H);
//	
//	    Nota.refreshSizes();
//	    maxy = 0;
//	    repaint();
//	}
//	
//	private BufferedImage changeSize(int idx) { // TODO
//	    int w0 = vseKartinki0[idx].getWidth();
//	    int h0 = vseKartinki0[idx].getHeight();
//	    int w1 = w0*notaWidth/NORMAL_WIDTH;
//	    int h1 = h0*notaHeight/NORMAL_HEIGHT;
//	
//	    BufferedImage tmp = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
//	    Graphics g = tmp.createGraphics();
//	    Image scaledImage = vseKartinki0[idx].getScaledInstance(w1, h1, Image.SCALE_SMOOTH);
//	    g.drawImage(scaledImage, 0, 0, w1, h1, null);
//	    g.dispose();
//	    return tmp;
//	}
}
