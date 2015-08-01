package stuff.tools;

import gui.Constants;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
