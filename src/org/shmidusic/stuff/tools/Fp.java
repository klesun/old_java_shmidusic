package org.shmidusic.stuff.tools;

import org.klesun_model.Explain;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Function;

public class Fp
{
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

	public static <T> Explain<T> findBinary(List<T> list, Function<T, Integer> pred)
	{
		Explain<T> result = new Explain<>(false, "No one element matched predicate");

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
				result = new Explain<>(list.get(middle));
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
}
