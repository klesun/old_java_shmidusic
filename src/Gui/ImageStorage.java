package Gui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageStorage {
	private static ImageStorage instance = null;

	private BufferedImage[] vseKartinkiSized = new BufferedImage[7]; // TODO: поменять этот уродский массив на ключ-значение // Гузно у тебя уродское
	private BufferedImage[] vseKartinkiOriginal = new BufferedImage[7];

	private BufferedImage notaImgOriginal[] = new BufferedImage[6];
	private BufferedImage[][] coloredNotas = new BufferedImage[10][6];

	private ImageStorage() {}

	public BufferedImage getFlatImage() { return vseKartinkiSized[2]; }
	public BufferedImage getSharpImage() { return vseKartinkiSized[6]; }
	public BufferedImage getViolinKeyImage() { return vseKartinkiSized[0]; }
	public BufferedImage getBassKeyImage() { return vseKartinkiSized[1]; }
	public BufferedImage getPointerImage() { return vseKartinkiSized[3]; }
	public BufferedImage getQuarterImage() { return coloredNotas[0][3]; }

	public static ImageStorage inst() {
		if (ImageStorage.instance == null) {
			ImageStorage.instance = new ImageStorage();
		}
		return ImageStorage.instance;
	}

	public void changeScale(int n) {
		Settings.inst().changeScale(n);
		refreshImageSizes();
	}

	public void loadImagesFromDisk() {
		try {	inst().vseKartinkiOriginal[0] = ImageIO.read(new File("../imgs/vio_sized.png"));
				inst().vseKartinkiOriginal[1] = ImageIO.read(new File("../imgs/bass_sized.png"));
				inst().vseKartinkiOriginal[2] = ImageIO.read(new File("../imgs/flat_sized.png"));
				inst().vseKartinkiOriginal[6] = ImageIO.read(new File("../imgs/sharp_sized.png")); // -_-
				inst().vseKartinkiOriginal[3] = ImageIO.read(new File("../imgs/MyPointer.png"));
				inst().vseKartinkiOriginal[4] = ImageIO.read(new File("../imgs/volume.png"));
				inst().vseKartinkiOriginal[5] = ImageIO.read(new File("../imgs/instrument.png"));
		} catch (IOException e) { e.printStackTrace(); System.out.println("Темнишь что-то со своей картинкой..."); }
		reloadNotaImagesFromDisk();
	}

	public void refreshImageSizes() {
		for (int i = 0; i < inst().vseKartinkiSized.length; ++i ) { // >_<
			inst().vseKartinkiSized[i] = changeSize(i);
		}
		refreshNotaSizes();
	}

	private BufferedImage changeSize(int idx) {
		int w0 = inst().vseKartinkiOriginal[idx].getWidth();
		int h0 = inst().vseKartinkiOriginal[idx].getHeight();
		int w1 = w0 * Settings.getNotaWidth() / Constants.NORMAL_NOTA_WIDTH;
		int h1 = h0 * Settings.getNotaHeight() / Constants.NORMAL_NOTA_HEIGHT;

		BufferedImage tmp = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
		Graphics g = tmp.createGraphics();
		Image scaledImage = inst().vseKartinkiOriginal[idx].getScaledInstance(w1, h1, Image.SCALE_SMOOTH);
		g.drawImage(scaledImage, 0, 0, w1, h1, null);
		g.dispose();
		return tmp;
	}

	public void reloadNotaImagesFromDisk() {
		for (int idx = 0; idx < 6; ++idx) {
			try { notaImgOriginal[idx] = ImageIO.read(new File("../imgs/" + pow(2, idx - 1) + "_sized.png")); } // "pow(2, -1) = 0" i feel so disgusting for myself
			catch (IOException e) { System.out.println(e + " Ноты не читаются!!! " + idx); }
		}
	}

	public void refreshNotaSizes() {
		int w1, h1; Graphics2D g;
		w1 = Settings.getNotaWidth(); h1 = Settings.getNotaHeight();

		// resizing first base color nota
		for (int idx = 0; idx < 6; ++idx ) {
			coloredNotas[0][idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
			g = coloredNotas[0][idx].createGraphics();

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1.0f));
			Image scaledImage = null;
			if (notaImgOriginal[idx] != null) {
				scaledImage = notaImgOriginal[idx].getScaledInstance(w1, h1, Image.SCALE_SMOOTH);
			}
			g.drawImage(scaledImage, 0, 0, w1, h1, null);
			g.dispose();
		}

		// renewing other colored notas
		for (int chan = 1; chan < 10; ++chan) {
			for (int idx = 0; idx < 6; ++idx) {
				coloredNotas[chan][idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
				g = coloredNotas[chan][idx].createGraphics();

				g.setColor(getColorByChannel(chan));
				g.fillRect(0, 0, Settings.getNotaWidth(), Settings.getNotaHeight());
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
				g.drawImage(coloredNotas[0][idx], 0, 0, w1, h1, null);
				g.dispose();
			}
		}
	}

	public BufferedImage getNotaImg(int numerator, int channel) {
		int idx = (int)(Math.ceil(7 - Math.log(numerator) / Math.log(2) ));
		return coloredNotas[channel][idx];
	}

	// static methods

	private static int pow(int n, int k){
		if (k < 0) return 0; // GENIUSSSS!!!!!!
		if (k==0) return 1;
		return n*pow(n, k-1);
	}

	public static Color getColorByChannel(int n) {
		return	n == 0 ? new Color(0,0,0) : // black
				n == 1 ? new Color(255,0,0) : // red
				n == 2 ? new Color(0,192,0) : // green
				n == 3 ? new Color(0,0,255) : // blue
				n == 4 ? new Color(255,128,0) : // orange
				n == 5 ? new Color(192,0,192) : // magenta
				n == 6 ? new Color(0,192,192) : // cyan
				Color.GRAY;
	}
}
