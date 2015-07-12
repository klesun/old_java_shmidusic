package Gui;

import BlockSpacePkg.BlockSpace;
import BlockSpacePkg.StaffPkg.StaffConfig.Channel;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class ImageStorage {

	final private static String DEFAULT_IMAGE_FOLDER = "imgs/";

	final private BlockSpace parentBlockSpace;

	private Map<Fraction, BufferedImage>[] coloredNotas = new Map[Channel.CHANNEL_COUNT];

	private Map<URL, BufferedImage> defaultImageMap = new HashMap<>();
	private Map<URL, BufferedImage> sizedDefaultImageMap = new HashMap<>();
	private Map<URL, BufferedImage> randomImageMap = new HashMap<>();

	public ImageStorage(BlockSpace parentBlockSpace) {
		this.parentBlockSpace = parentBlockSpace;
		for (int i = 0; i < Channel.CHANNEL_COUNT; ++i) {
			coloredNotas[i] = new HashMap<>();
		}
		refreshImageSizes();
	}

	// TODO: rename to openRandomImage()
	public BufferedImage openImage(URL file) {
		return openImage(file, randomImageMap);
	}

	private static BufferedImage strToImg(String str) {
		BufferedImage img = new BufferedImage(str.length() * Constants.FONT_WIDTH, Constants.FONT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setFont(Constants.PROJECT_FONT);
		g.setColor(Color.RED);
		g.drawString(str, 0, Constants.FONT_HEIGHT / 2 + 4);
		return img;
	}

	public void refreshImageSizes() {
		sizedDefaultImageMap.clear();
		refreshNotaSizes(); // TODO: maybe make Nota-s lazy too ?
	}

	public void refreshNotaSizes() {

		int w1, h1; Graphics2D g;
		w1 = parentBlockSpace.getSettings().getNotaWidth();
		h1 = parentBlockSpace.getSettings().getNotaHeight();

		// resizing first base color nota
		for (Fraction length: getAvailableNotaLengthList()) {
			coloredNotas[0].put(length, changeSize(openDefaultImage(getNotaImageFile(length))));
		}

		// renewing other colored notas
		for (int chan = 1; chan < Channel.CHANNEL_COUNT; ++chan) {
			for (Fraction length: getAvailableNotaLengthList()) {
				coloredNotas[chan].put(length, new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB));
				g = (Graphics2D)coloredNotas[chan].get(length).getGraphics();
				g.setColor(getColorByChannel(chan));
				g.fillRect(0, 0, parentBlockSpace.getSettings().getNotaWidth(), parentBlockSpace.getSettings().getNotaHeight());
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
				g.drawImage(coloredNotas[0].get(length), 0, 0, w1, h1, null); // not sure that need
			}
		}
	}

	public BufferedImage getNotaImg(Fraction length, int channel) {
		return coloredNotas[channel].get(length);
	}

	// default images

	// it's 2/1
	public static Fraction getGreatestPossibleNotaLength() {
		return getAvailableNotaLengthList().get(0);
	}

	// it's 1/16
	public static Fraction getSmallestPossibleNotaLength() {
		return getAvailableNotaLengthList().get(getAvailableNotaLengthList().size() -1);
	}

	// from 2/1 downto 1/16
	public static List<Fraction> getAvailableNotaLengthList() {
		List<Fraction> result = new ArrayList<>();
		for (int idx = 0; idx < 6; ++idx) { result.add(new Fraction(2, pow2(idx))); }
		return result;
	}

	private URL getNotaImageFile(Fraction length) {
		int fileStupidIndex = length.equals(new Fraction(2)) ? 0 : length.getDenominator();
		String fileName = fileStupidIndex + "_sized.png";

		return getClass().getResource(DEFAULT_IMAGE_FOLDER + fileName);
	}

	public BufferedImage getQuarterImage() { return coloredNotas[0].get(new Fraction(1, 4)); }

	public BufferedImage getFlatImage() { return openSizedDefaultImage("flat_sized.png"); }
	public BufferedImage getViolinKeyImage() { return openSizedDefaultImage("vio_sized.png"); }
	public BufferedImage getBassKeyImage() { return openSizedDefaultImage("bass_sized.png"); }
	public BufferedImage getPointerImage() { return openSizedDefaultImage("MyPointer.png"); }
	public BufferedImage getSharpImage() { return openSizedDefaultImage("sharp_sized.png"); }

	// not used for now, but may be handy one day
	public BufferedImage getInstrumentImage() { return openSizedDefaultImage("instrument.png"); }
	public BufferedImage getVolumeImage() { return openSizedDefaultImage("volume.png"); }

	// static methods

	public static Color getBetween(Color start, Color end, Fraction factor) {
		factor = new Fraction(Math.max(factor.doubleValue(), 0));
		factor = new Fraction(Math.min(factor.doubleValue(), 1));

		int dr = factor.multiply(new Fraction(end.getRed() - start.getRed())).intValue();
		int dg = factor.multiply(new Fraction(end.getGreen() - start.getGreen())).intValue();
		int db = factor.multiply(new Fraction(end.getBlue() - start.getBlue())).intValue();
		int da = factor.multiply(new Fraction(end.getAlpha() - start.getAlpha())).intValue();

		return new Color(start.getRed() + dr, start.getGreen() + dg, start.getBlue() + db, start.getAlpha() + da);
	}

	public static Color getColorByChannel(int n) {
		return	n == 0 ? new Color(0,0,0) : // black
				n == 1 ? new Color(255,0,0) : // red
				n == 2 ? new Color(0,192,0) : // green
				n == 3 ? new Color(0,0,255) : // blue
				n == 4 ? new Color(191,191,0) : // yellow
				n == 5 ? new Color(0,127,255) : // cyan
				n == 6 ? new Color(192,0,192) : // magenta
				n == 7 ? new Color(255,128,0) : // orange
				n == 8 ? new Color(91,0,255) : // bluish magenta
				Color.GRAY;
	}

	// private methods


	private BufferedImage openDefaultImage(URL file) {
		return openImage(file, defaultImageMap);
	}

	private BufferedImage openSizedDefaultImage(String fileName) {
		URL file = getClass().getResource(DEFAULT_IMAGE_FOLDER + fileName);
		if (!sizedDefaultImageMap.containsKey(file)) {
			sizedDefaultImageMap.put(file, changeSize(openDefaultImage(file)));
		}

		return sizedDefaultImageMap.get(file);
	}

	private BufferedImage openImage(URL file, Map<URL, BufferedImage> imageMap) {
		if (!imageMap.containsKey(file)) {
			try {
				imageMap.put(file, ImageIO.read(file));
			} catch (IOException e) {
				String msg = "Failed to load image file! " + file.getRef() + " " + e.getMessage();
				Logger.warning(msg);
				imageMap.put(file, strToImg(msg));
			}
		}
		return imageMap.get(file);
	}

	private BufferedImage changeSize(BufferedImage originalImage) {
		int w1 = originalImage.getWidth() * parentBlockSpace.getSettings().getNotaWidth() / Constants.NORMAL_NOTA_WIDTH;
		int h1 = originalImage.getHeight() * parentBlockSpace.getSettings().getNotaHeight() / Constants.NORMAL_NOTA_HEIGHT;

		Image tmpImage = originalImage.getScaledInstance(w1, h1, Image.SCALE_SMOOTH);
		BufferedImage newImage = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
		newImage.getGraphics().drawImage(tmpImage, 0, 0, null);

		return newImage;
	}

	private static int log2(int n) {

		if (n == 1) {
			return 0;
		} else if (n <= 0 || n % 2 != 0) {
			Logger.fatal("Number " + n + " does not have integer logarithm with base 2 ");
			return -100;
		} else {
			return 1 + log2(n / 2);
		}
	}

	private static int pow2(int k){
		if (k==0) {
			return 1;
		} else {
			return 2 * pow2(k - 1);
		}
	}
}
