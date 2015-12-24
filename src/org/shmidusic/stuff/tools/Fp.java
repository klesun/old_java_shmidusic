package org.shmidusic.stuff.tools;

import org.klesun_model.Explain;

import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

// TODO: rename. Fp stands for "Functional Programming", not "Funkcii Poleznie"

public class Fp
{
	public static Boolean isPowerOf2(int n) {
		return (n & (n - 1)) == 0;
	}

	public static KeyListener onKey(Consumer<KeyEvent> lambda) {
		return new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				lambda.accept(e);
			}
		};
	}

	public static MouseListener onClick(Consumer<MouseEvent> lambda) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lambda.accept(e);
			}
		};
	}

	public static String splitCamelCase(String s) {
		return "<html><center>" + s.replaceAll(
				String.format("%s|%s|%s",
						"(?<=[A-Z])(?=[A-Z][a-z])",
						"(?<=[^A-Z])(?=[A-Z])",
						"(?<=[A-Za-z])(?=[^A-Za-z])"
				),
				" "
		) + "</center></html>";
	}

	public static <T> Optional<T> findBinary(List<T> list, Function<T, Integer> pred)
	{
		Optional<T> result = Optional.empty();

		int l = 0;
		int r = list.size() - 1;

		while (l <= r) {
			int middle = (l + r) / 2;
			int cmpResult = pred.apply(list.get(middle));
			if (cmpResult > 0) {
				l = middle + 1;
			} else if (cmpResult < 0) {
				r = middle - 1;
			} else {
				result = Optional.of(list.get(middle));
				break;
			}
		}

		return result;

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

	private static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException exc) {
			Logger.fatal(exc, "Dont call this method when thread may be interrupted!");
		}
	}

	// an equivalent of Javascript's setTimeout
	public static void setTimeout(Runnable callback, int millis) {
		new Thread(() -> { sleep(millis); callback.run(); }).start();
	}
}
