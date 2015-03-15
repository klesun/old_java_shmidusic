package Gui;

import java.awt.image.BufferedImage;

public class BitmapGenerator {
	private static BitmapGenerator instance = null;

	public BufferedImage[] vseKartinki = new BufferedImage[6]; // TODO: поменять этот уродский массив на ключ-значение
	public BufferedImage[] vseKartinki0 = new BufferedImage[6];

	public int notaHeight = 32;
	public final static int NORMAL_HEIGHT = 40;
	public int notaWidth = 20;
	public final static int NORMAL_WIDTH = 25;
	public int STEPY = notaHeight/8; // Графика
	public int STEPX = notaWidth; // Графика
	double MARGIN_V = 15; // Сколько отступов сделать сверху перед рисованием полосочек
	double MARGIN_H = 1;

	public static BitmapGenerator getInstance() {
		if (instance == null) {
			instance = new BitmapGenerator();
		}
		return instance;
	}
}
