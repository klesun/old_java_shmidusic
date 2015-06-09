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
		Runtime.getRuntime().exit(666);
		return 777;
	}

	public static int warning(String msg) {
		System.out.println("Warning: " + msg);
		return 555;
	}

	public static int FYI(String msg) {
		System.out.println("For your information: " + msg);
		return 0;
	}


	public static int logMemory(String msg) {
		int mb = 1024 * 1024;
		long totalMemory = Runtime.getRuntime().totalMemory();
		long allocatedMemory = totalMemory - Runtime.getRuntime().freeMemory();
		System.out.println(totalMemory / mb + " " + Runtime.getRuntime().freeMemory() / mb + " " + allocatedMemory / mb + " " + msg);
		return 111;
	}
}
