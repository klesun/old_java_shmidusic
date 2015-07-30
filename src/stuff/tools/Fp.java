package stuff.tools;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Fp {
	// TODO: make static class Settings, where we will store scaling koef and the stuff
	public static void drawString(Graphics surface, String str, int x, int y, Color color) {
//		surface.setColor(color);
//		int inches = sheet.getNotaHeight()*5/8; // 25, 80
//		surface.setFont(new Font(Font.MONOSPACED, Font.PLAIN, inches)); // 12 - 7px width
	}

	public static List<Integer> vectorSum(List<Integer> vector1, List<Integer> vector2) {
		ArrayList<Integer> resultVector = new ArrayList<>();
		for (int i = 0; i < vector1.size(); ++i) {
			resultVector.add(vector1.get(i) + vector2.get(i));
		}
		return resultVector;
	}

	/** draws parabola, that would fit into the rectangle */
	public static void drawParabola(Graphics2D g, Rectangle r) {
		Color tmpColor = g.getColor();
		Stroke tmpStroke = g.getStroke();
		g.setColor(Color.MAGENTA);
		g.setStroke(new BasicStroke(2));

		double sharpness = r.getHeight() / Math.pow(r.getWidth() / 2.0, 2);
		int baseX = r.x + r.width / 2;
		int baseY = r.y + r.height;

		int lastX = - r.width / 2;
		int lastY = (int)(lastX * lastX * sharpness);

		for (int x0 = - r.width / 2 + 1; x0 < r.width / 2; ++x0) {
			int y0 = (int)(x0 * x0 * sharpness);
			g.drawLine(baseX + lastX, baseY - lastY, baseX + x0, baseY - y0);

			lastX = x0;
			lastY = y0;
		}

		g.setColor(tmpColor);
		g.setStroke(tmpStroke);
	}

	// TODO: this method was written on quick hand - don't judge strict, but better - improve!
	public static String traceDiff(Throwable trace1, Throwable trace2)
	{
		StringWriter sw = new StringWriter();
		trace1.printStackTrace(new PrintWriter(sw));
		String[] trace1LineList = sw.toString().split("\n");

		sw = new StringWriter();
		trace2.printStackTrace(new PrintWriter(sw));
		String[] trace2LineList = sw.toString().split("\n");

		int trace1LineIdx = trace1LineList.length - 1;
		int trace2LineIdx = trace2LineList.length - 1;

		while (trace1LineIdx > 0 && trace2LineIdx > 0) {
			if (!trace1LineList[trace1LineIdx--].equals(trace2LineList[trace2LineIdx--])) {
				break;
			}
		}

		String result = "";
		for (int i = 0; i <= trace1LineIdx + 1; ++i) {
			result += trace1LineList[i] + "\n";
		}

		return result;
	}
}
