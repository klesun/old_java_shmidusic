package Stuff.Tools.jmusicIntegration;

import BlockSpacePkg.StaffPkg.Accord.Accord;
import BlockSpacePkg.StaffPkg.Accord.Nota.Nota;
import BlockSpacePkg.StaffPkg.Staff;
import Gui.ImageStorage;
import Stuff.Tools.Logger;
import main.Main;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddPhrase {

	final private Staff staff;
	final private List<JmNote> noteList;
	final private Fraction startOffset;
	final private int channel;

	public AddPhrase(Staff staff, JSONObject phraseJs, int channel) {
		this.staff = staff;
		this.channel = channel;
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
				// вы говорите, что пауза это INT_MIN, а я говорю что ноль!
				note = JmNote.mk(0, note.getLength(), channel);
			}

			put(curPos, note);

			curPos = curPos.add(note.getRealLength());
		}
	}

	private void put(Fraction pos, JmNote note) {
		Nota nota;
		if (note.isDotable() || note.getLength().compareTo(ImageStorage.getSmallestPossibleNotaLength()) < 0) {
			nota = staff.putAt(pos, note);
		} else {
			Fraction base = getClosestRegularBelow(note.getLength());
			Fraction rest = note.getLength().subtract(base);
			nota = staff.putAt(pos, JmNote.mk(note.getTune(), base, channel)).setIsLinkedToNext(true);
			nota = staff.putAt(pos.add(base), JmNote.mk(note.getTune(), rest, channel));
		}

		/** @debug */
//		nota.getFirstAwtParent().repaint();
//		JOptionPane.showMessageDialog(Main.window, nota.toString());
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

		Logger.fatal("You should not need to call this method with such small argument");
		return new Fraction(-100);
	}

	private static List<JSONObject> toList(JSONArray arr) {
		List<JSONObject> result = new ArrayList<>();
		for (int i = 0; i < arr.length(); ++i) {
			result.add(arr.getJSONObject(i));
		}
		return result;
	}
}
