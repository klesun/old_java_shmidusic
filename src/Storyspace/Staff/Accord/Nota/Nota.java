package Storyspace.Staff.Accord.Nota;


import Gui.ImageStorage;
import Model.Combo;
import Model.Field.Field;
import Storyspace.Staff.MidianaComponent;
import Storyspace.Staff.Accord.Accord;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;

import Gui.Settings;
import Storyspace.Staff.StaffConfig.StaffConfig;
import Stuff.Tools.Fp;
import org.json.JSONObject;

import java.util.List;

public class Nota extends MidianaComponent implements Comparable<Nota> {

	// <editor-fold desc="model field declaration">

	private Field<Integer> tune = h.addField("tune", 34);
	private Field<Fraction> length = h.addField("length", new Fraction(1, 4));
//	private Field<Integer> numerator = h.addField("numerator", 16); // deprecated
	private Field<Integer> channel = h.addField("channel", 0);
	private Field<Integer> tupletDenominator = h.addField("tupletDenominator", 1);
	private Field<Boolean> isSharp = h.addField("isSharp", false);
	private Field<Boolean> isMuted = h.addField("isMuted", false);
	private Field<Boolean> isLinkedToNext = h.addField("isLinkedToNext", false);

	final private static int MAX_DOT_COUNT = 5;

	// </editor-fold>

	public long keydownTimestamp;

	public Nota(Accord parent, int tune) {
		super(parent);
		setTune(tune);
		parent.add(this);
	}

	public Boolean isLongerThan(Nota rival) { return getLength().compareTo(rival.getLength()) > 0; }

	// <editor-fold desc="implementing abstract model">

	@Override
	public void drawOn(Graphics surface, int x, int y) {
		surface.setColor(Color.BLACK);
		surface.drawImage(getEbonySignImage(), x + dx() / 2, y + 3 * dy() + 2, null);

		BufferedImage tmpImg = getIsMuted()
				? ImageStorage.inst().getNotaImg(getCleanLength(), 9)
				: ImageStorage.inst().getNotaImg(getCleanLength(), getChannel());

		if (getIsLinkedToNext()) {
			Fp.drawParabola((Graphics2D) surface, new Rectangle(x + dx() * 3 / 2, y + dy() * 7, dx() * 2, dy() * 2));
		}

		surface.drawImage(tmpImg, x + getNotaImgRelX(), y, null);

		if (getTupletDenominator() != 1) {
			for (int i = 0; i < getTupletDenominator(); ++i) {
				surface.drawLine(x + getStickX(), y + i * 2 + 1, x + getStickX() - 6, y + i * 2 + 1);
			}
		}

		for (int i = 0; i < getDotCount(); ++i) {
			surface.fillOval(x + dx() * 5/3 + dx() * i / getDotCount(), y + dy() * 7, dy(), dy());
		}
	}

	@Override
	public MidianaComponent getFocusedChild() { return null; }
	@Override
	protected NotaHandler makeHandler() { return new NotaHandler(this); }
	@Override
	public int compareTo(Nota n) { return n.getTune() - this.getTune(); }

	@Override
	public Nota reconstructFromJson(JSONObject dict) {
		super.reconstructFromJson(dict);
		/** @deprecated */
		if (dict.has("numerator")) {
			this.setLength(new Fraction(dict.getInt("numerator"), 64));
		}
		return this;
	}

	// </editor-fold>

	// <editor-fold desc="getters">

	public int getAcademicIndex() {
		return isEbony() && getIsSharp()
				? Nota.tuneToAcademicIndex(this.getTune()) - 1
				: Nota.tuneToAcademicIndex(this.getTune());
	}

	public List<Integer> getTraitCoordinates() {
		ArrayList result = new ArrayList();
		result.addAll(Fp.vectorSum(getAncorPoint(), Arrays.asList(-getWidth() * 6 / 25, 0)));
		result.addAll(Fp.vectorSum(getAncorPoint(), Arrays.asList(+getWidth() * 6 / 25, 0)));
		return result;
	}

	public int getTimeMilliseconds(Boolean includeLinkedTime) {
		int minute = 60 * 1000;
		StaffConfig config = getParentAccord().getParentStaff().getConfig();
		int semibreveTime = 4 * minute / config.getTempo();

		int linkedTime = (includeLinkedTime && getIsLinkedToNext()) ? linkedTo().getTimeMilliseconds(true) : 0;

		return getLength().multiply(semibreveTime).intValue() + linkedTime;
	}

	public byte getVolume() {
		if (this.getTune() == 36) {
			return 0; // пауза лол какбэ
		} else {
			StaffConfig config = getParentAccord().getParentStaff().getConfig();
			return (byte)(127 * config.getVolume(getChannel()) / 100);
		}
	}

	private BufferedImage getEbonySignImage() {
		return  !this.isEbony() ? null :
				getIsSharp() ? ImageStorage.inst().getSharpImage() :
				ImageStorage.inst().getFlatImage();
	}

	// TODO: it definitely can be implemented somehow better, but i'm to stupid to realize how exactly
	private int getDotCount() {
		int dots = 0;

		// does not take into account, that Nota can be triplet (cuz for now we store triplet denominator separately)

		Fraction checkSum = getCleanLength();

		while (checkSum.compareTo(getLength()) != 0) { // for deadlock safety would be better while < 0, but for debug - this... actually even with while < 0 can be deadlock so nevermind - it works 120%
			++dots;
			checkSum = checkSum.add(getCleanLength().divide(pow(2, dots)));

			// for a case. Deadlock is deadlock after all
			if (dots > MAX_DOT_COUNT) { Logger.fatal("Could not determine dot count for Fraction: [" + getLength() + "]"); }
		}

		return dots;
	}

	private void setDotCount(int dotCount) {
		// does not take into account, that Nota can be triplet (cuz for now we store triplet denominator separately)

		Fraction checkSum = getCleanLength();

		for (int i = 1; i <= dotCount; ++i) {
			checkSum = checkSum.add(getCleanLength().divide(pow(2, i)));
		}

		this.setLength(checkSum);
	}

	private int pow(int n, int e) {
		return e == 0 ? 1 : n * pow(n, e - 1);
	}

	private Fraction getCleanLength() { // i.e. length without dots: 1/4, 1/2
		return getLength().getDenominator() == 1
				? new Fraction(getLength().getNumerator() * 2, getLength().getDenominator() + 1)
				: new Fraction(getLength().getNumerator() + 1, getLength().getDenominator() * 2);
	}

	// </editor-fold>

	// <editor-fold desc="one-line-getters">

	// 0 - do, 2 - re, 4 - mi, 5 - fa, 7 - so, 9 - la, 10 - ti
	public Boolean isEbony() { return Arrays.asList(1, 3, 6, 8, 10).contains(this.getTune() % 12); }
	public Boolean isBotommedToFitSystem() { return this.getOctave() > 6; } // 8va
	public Boolean isStriked() { return getAbsoluteAcademicIndex() % 2 == 1; }

	public int getAbsoluteAcademicIndex() { return getAcademicIndex() + getOctave() * 7; }
	public int getOctave() { return this.getTune()/12; }
	public List<Integer> getAncorPoint() { return Arrays.asList(getWidth()*16/25, Settings.getStepHeight() * 7); }
	public int getNotaImgRelX() { return this.getWidth() / 2; } // bad name
	public int getStickX() { return this.getNotaImgRelX() + dx() / 2; }

	public int getHeight() { return Settings.getNotaHeight(); }
	// TODO: use it in Accord.getWidth()
	public int getWidth() { return Settings.getNotaWidth() * 2; }

	// </editor-fold>

	// <editor-fold desc="model getters/setters">

	// model getters
	public Integer getTune() { return tune.getValue(); }
	public Fraction getLength() { return length.getValue(); }
	public Integer getChannel() { return channel.getValue(); }
	public Integer getTupletDenominator() { return tupletDenominator.getValue(); }
	public Boolean getIsSharp() { return isSharp.getValue(); }
	public Boolean getIsMuted() { return isMuted.getValue(); }
	public Boolean getIsLinkedToNext() { return isLinkedToNext.getValue(); }
	// model setters
	public Nota setTune(int value){ this.tune.setValue(value); return this; }
	public Nota setLength(Fraction value){ this.length.setValue(limit(value, new Fraction(1, 16), new Fraction(2))); return this; }
	public Nota setChannel(int value) { this.channel.setValue(value); return this; }
	public Nota setTupletDenominator(int value) { this.tupletDenominator.setValue(value); return this; }
	public Nota setIsSharp(Boolean value) { this.isSharp.setValue(value); return this; }
	public Nota setIsMuted(Boolean value) { this.isMuted.setValue(value); return this; }
	public Nota setIsLinkedToNext(Boolean value) { this.isLinkedToNext.setValue(value); return this; }

	// </editor-fold>

	public Accord getParentAccord() { return (Accord)this.getModelParent(); }
	public Nota setKeydownTimestamp(long value) { this.keydownTimestamp = value; return this; }

	// <editor-fold desc="event handles">

	public Nota triggerIsSharp() { setIsSharp(!getIsSharp()); return this; }
	public Nota triggerIsMuted() { setIsMuted(!getIsMuted()); return this; }
	public Boolean triggerIsLinkedToNext() {
		if (getNext() != null || getIsLinkedToNext() == true) {
			setIsLinkedToNext(!getIsLinkedToNext());
			return true;
		} else {
			return false;
		}
	}
	public Nota triggerTupletDenominator() { setTupletDenominator(getTupletDenominator() == 3 ? 1 : 3); return this; }

	public Nota linkedTo() {
		return this.getIsLinkedToNext() ? this.getNext() : null;
	}

	/** @return Nota of the next accord with same tune or null */
	private Nota getNext() {
		Accord nextAccord = getParentAccord().getNext();
		return nextAccord != null
				? nextAccord.findByTune(this.getTune())
				: null;
	}

	public Nota incLen() {
		// TODO: points should be whole separate thing
		setLength(new Fraction(getLength().getNumerator() * 2, getLength().getDenominator()));
		return this;
	}

	public Nota decLen() {
		// TODO: points should be whole separate thing
		setLength(new Fraction(getLength().getNumerator(), getLength().getDenominator() * 2));
		return this;
	}

	public Nota changeLength(Combo combo) {
		return combo.getSign() > 0
				? incLen()
				: decLen();
	}

	public Boolean putDot() {
		if (getDotCount() < MAX_DOT_COUNT) {
			setDotCount(getDotCount() + 1);
			return true;
		} else {
			return false;
		}
	}

	public Boolean removeDot() {
		if (getDotCount() > 0) {
			setDotCount(getDotCount() - 1);
			return true;
		} else {
			return false;
		}
	}

	public Boolean dot(Combo combo) {
		return combo.getSign() > 0
			? putDot()
			: removeDot();
	}

	// </editor-fold>

	// private static methods

	// TODO: what if i said we could store these instead of numbers in json?
	private static String strIdx(int n){ return Arrays.asList("do","re","mi","fa","so","la","ti").get(n % 12); }
	private static int tuneToAcademicIndex(int tune) { return Arrays.asList(0,1,1,2,2,3,4,4,5,5,6,6).get(tune % 12); }
}