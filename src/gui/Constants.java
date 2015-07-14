package gui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Constants {
	
	final public static Font PROJECT_FONT =  new Font(Font.MONOSPACED, Font.PLAIN, 10);

	public static final int FONT_WIDTH = g().getFontMetrics().stringWidth("i");
	public static final int FONT_HEIGHT = (int)g().getFontMetrics().getLineMetrics("i", g()).getHeight();

	final public static int NORMAL_NOTA_HEIGHT = 40;
	final public static int NORMAL_NOTA_WIDTH = 25;

	private static Graphics g() {
		Graphics g = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB).getGraphics();
		g.setFont(PROJECT_FONT);
		return g;
	}
}
