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
			System.out.println("Убейся головой об стену");
		} catch (Exception e) {
			System.out.println("blablablabalall");
		}
		Window app = new Window();
		app.setVisible(true);

		while (true) {
			try {
				Thread.sleep(20);
				app.keyHandler.handleFrameTimer();
			} catch (InterruptedException exc) { System.out.println("Кто разбудил мой трэд?!"); }
		}
	}
}