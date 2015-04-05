import Gui.Window;

import java.io.*;
public class Main {
	public static void main(String[] args){
		String OS_NAME = System.getProperty("os.name");
		try {
			System.out.println(OS_NAME);
			if (OS_NAME.equals("Windows XP") || OS_NAME.equals("Windows 7")) { // looks like windows 7 has different encoding
				PrintStream out = new PrintStream(System.out, true, "Cp866");
				System.setOut(out);
			}
			System.out.println("Отведай же ещё этих сочных французских булок");
		} catch (Exception e) {
			System.out.println("blablablabalall");
		}
		Window app = new Window();
		app.setVisible(true);
		while (true) {
			try { Thread.sleep(40); } catch (Exception e) { System.out.println("Zhopa"); }
			// maybe it's weird a bit, but i spent much time to find the way, how to make canvas be up to time
			app.keyHandler.handleFrameTimer();
		}
	}
}