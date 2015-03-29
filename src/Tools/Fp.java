package Tools;

import java.awt.Color;
import java.awt.Graphics;
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
}
