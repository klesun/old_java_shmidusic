package Stuff.Tools;

import Main.Main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private static Long time = null;

	final private static String PRE_FATAL_BACKUP_FOLDER = "./savedJustBeforeFatal/";

	public static int getFatal(String msg) {
		fatal(msg);
		return -100;
	}

	public static void fatal(String msg) {
		fatal("Fatal: " + msg, new Throwable());
	}

	public static void fatal(Exception e, String msg) {
		fatal("Got exception" + e.getClass().getName() + " with message: [" + e.getMessage() + "] - " + msg, e);
	}

	private static void fatal(String msg, Throwable traceProvider) {
		System.out.println(msg);
		traceProvider.printStackTrace();

		// TODO: if we don't have permissions - let user manually selected where to save
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		new File(PRE_FATAL_BACKUP_FOLDER).mkdirs();
		FileProcessor.saveStoryspace(new File(PRE_FATAL_BACKUP_FOLDER + "fatal_backup_" + dateFormat.format(new Date()) + ".bs.json"), Main.window.blockSpace);

		Runtime.getRuntime().exit(666);
	}

	public static int warning(String msg) {

		// TODO: group similar warnings (i.e. when json does not have a field versus setting value that does not match a normalization rule)

		System.out.println("Warning: " + msg);
		return 555;
	}

	public static void error(String msg) {
		warning("Error: " + msg); // =D
	}

	public static int FYI(String msg) {
		System.out.println("For your information: " + msg);
		return 0;
	}


	public static void logMemory(String msg) {
		int mb = 1024 * 1024;
		long totalMemory = Runtime.getRuntime().totalMemory();
		long allocatedMemory = totalMemory - Runtime.getRuntime().freeMemory();
		System.out.println(totalMemory / mb + " " + Runtime.getRuntime().freeMemory() / mb + " " + allocatedMemory / mb + " " + msg);
	}

	public static void resetTimer(String msg) {
		if (time == null) {
			time = System.nanoTime();
		}

		long newTime = System.nanoTime();
		logForUser("==== " + (newTime - time)/1e9 + " - " + msg);
		time = newTime;
	}

	public static void logForUser(String msg) {
		Main.window.terminal.append("\n" + msg);
		System.out.println("*** " + msg);
	}
}
