package stuff.tools.jmusic_integration;

import blockspace.staff.accord.nota.Nota;
import blockspace.staff.Staff;
import gui.ImageStorage;
import stuff.tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AddPhrase {

	final private Staff staff;
	final private List<JmNote> noteList;
	final private Fraction startOffset;
	final private int channel;
	final private Boolean round;

	public AddPhrase(Staff staff, JSONObject phraseJs, int channel, Boolean round) {
		this.staff = staff;
		this.channel = channel;
		this.round = round;

		this.noteList = toList(phraseJs.getJSONArray("noteList"))
				.stream().map(j -> JmNote.mk(j, channel)).collect(Collectors.toList());
		this.startOffset = phraseJs.has("startTime")
				? JmNote.toFrac(phraseJs.getDouble("startTime"))
				: new Fraction(0);
	}

	public void perform() {
		Fraction curPos = this.startOffset;
		for (JmNote note: noteList) {

			if (note.getTune() == Integer.MIN_VALUE) {
				// �� ��������, ��� ����� ��� INT_MIN, � � ������ ��� ����!
				note = JmNote.mk(0, note.getLength(), channel);
			}

			/** @debug */
			if (note.getLength().equals(new Fraction(0))) {
				System.out.println("zhopa " + note.rawLength);
				Runtime.getRuntime().exit(-100);
			}

			if (round) {
				note = JmNote.mk(note.getTune(), roundNotaLength(note.rawLength), channel);
			}

			put(curPos, note);

			curPos = new Fraction(curPos.floatValue() + note.getRealLength().floatValue());
			if (round) {
				curPos = roundPos(curPos);
			}
		}
	}

	private void put(Fraction pos, JmNote note) {
		Nota nota;
		if (note.isDotable() || note.isTooLong() || note.isTooShort()) {
			nota = staff.putAt(pos, note);
		} else {
			Fraction base = getClosestRegularBelow(note.getLength());
			Fraction rest = note.getLength().subtract(base);
			nota = staff.putAt(pos, JmNote.mk(note.getTune(), base, channel)).setIsLinkedToNext(true);
			nota = staff.putAt(pos.add(base), JmNote.mk(note.getTune(), rest, channel));
		}
	}

	private static Fraction roundPos(Fraction pos) {
		Fraction result = new Fraction(0);
		// ��������� �� ����� ��������� ��� � ������� �������
		while (pos.compareTo(ImageStorage.getShortLimit().divide(3)) > 0) {
			Fraction change = pos.compareTo(ImageStorage.getTallLimit()) < 0
					? roundNotaLength(pos.doubleValue())
					: ImageStorage.getTallLimit();
			result = result.add(change);
			pos = pos.subtract(change);
		}
		return result;
	}

	// clean with dots
	private static Fraction getClosestRegularBelow(Fraction irregular) {
		Fraction regular = getClosestCleanBelow(irregular);

		// for now i'll allow only one dot here, cuz more dots are rarely used; less conditions - more chances to guess correctly
		if (regular.add(regular.divide(2)).compareTo(irregular) < 0) {
			regular = regular.add(regular.divide(2));
		}

		return regular;
	}

	private static Fraction getClosestCleanBelow(Fraction irregular) {
		for (Fraction maybeResult: ImageStorage.getAvailableNotaLengthList()) {
			if (irregular.compareTo(maybeResult) >= 0) {
				return maybeResult;
			}
		}

		Logger.fatal("You should not need to call this method with such small argument"); // maybe i amnt right
		return new Fraction(-100);
	}

	private static List<JSONObject> toList(JSONArray arr) {
		List<JSONObject> result = new ArrayList<>();
		for (int i = 0; i < arr.length(); ++i) {
			result.add(arr.getJSONObject(i));
		}
		return result;
	}

	/** @return a fraction between left and right limit (1/48 and 2/1) */
	private static Fraction roundNotaLength(Double rhythm) {

		rhythm = rhythm / 4; // i dunno, looks like it's standard practice in midi to divide nota length in milliseconds to TicksPerBeat

		List<Fraction> lengths = new ArrayList<>();
		// filling the list
		for (Fraction frac: ImageStorage.getAvailableNotaLengthList()) {
			lengths.add(frac); // simple
			lengths.add(frac.divide(3)); // triplet
			lengths.add(frac.add(frac.divide(2))); // with dot
			if (frac.divide(4).compareTo(ImageStorage.getShortLimit()) > 0) {
				lengths.add(frac.add(frac.divide(4))); // linked to 4 times smaller
			}
		}
		Collections.sort(lengths);

		/** @debug */
		System.out.println("\nRounders gonna round! " + rhythm);

		for (int i = 0; i < lengths.size(); ++i) {
			System.out.println("Iteration " + i);
			if (i < lengths.size() - 1) {
				Fraction small = lengths.get(i);
				Fraction big = lengths.get(i + 1);

				if (rhythm <= big.doubleValue()) {
					Double dSmall = rhythm - small.doubleValue();
					Double dBig = rhythm - big.doubleValue();

					System.out.println("huj");
					return dSmall < dBig ? small : big; // should note: works also if rhythm is greater than greatest (dSmall will be negative)
				}
			} else {
				System.out.println("zhopa");
				return lengths.get(lengths.size() - 1);
			}
		}

		Logger.fatal("Cant happen!");
		return new Fraction(-100);
	}
}
