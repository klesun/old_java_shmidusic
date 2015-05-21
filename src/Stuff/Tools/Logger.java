package Stuff.Tools;

public class Logger {

	public static int fatal(String msg) {
		System.out.println("Fatal: " + msg);
		new Throwable().printStackTrace();
		Runtime.getRuntime().exit(666);
		return 666;
	}

	public static int fatal(Exception e, String msg) {
		System.out.println("Got exception" + e.getClass().getName() + " with message: [" + e.getMessage() + "] ");
		System.out.println(msg);
		e.printStackTrace();
		return 777;
	}

	public static int warning(String msg) {
		System.out.println("Warning: " + msg);
		return 555;
	}
}
