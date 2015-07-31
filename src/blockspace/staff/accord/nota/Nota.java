package blockspace.staff.accord.nota;


import gui.ImageStorage;
import model.AbstractPainter;
import model.Combo;
import model.field.Field;
import blockspace.staff.MidianaComponent;
import blockspace.staff.accord.Accord;

import java.awt.*;
import java.awt.image.BufferedImage;

import stuff.OverridingDefaultClasses.Pnt;
import stuff.tools.jmusic_integration.INota;
import org.apache.commons.math3.fraction.Fraction;

import blockspace.staff.StaffConfig.StaffConfig;
import stuff.tools.Fp;
import org.json.JSONObject;

public class Nota extends MidianaComponent implements INota {

	// <editor-fold desc="model field declaration">

	// TODO: normalization rules maybe ???
	public Field<Integer> tune = new Field<>("tune", Integer.class, true, this);
	protected Field<Integer> channel = new Field<>("channel", Integer.class, true, this);

	public Field<Fraction> length = new Field<>("length", new Fraction(1, 4), this, INota::legnthNorm);
	public Field<Boolean> isTriplet = new Field<>("isTriplet", false, this);
	private Field<Boolean> isSharp = new Field<>("isSharp", false, this);
	private Field<Boolean> isMuted = new Field<>("isMuted", false, this);
	private Field<Boolean> isLinkedToNext = new Field<>("isLinkedToNext", false, this);

	final private static int MAX_DOT_COUNT = 5;
	final private static int PAUSE_POSITION = 3 * 7;

	// </editor-fold>

	public long keydownTimestamp;

	public Nota(Accord parent) {
		super(parent);
		h.getFieldStorage().forEach(f -> f.setOnChange(getParentAccord()::surfaceChanged));
	}

	public Boolean isLongerThan(Nota rival) { return getRealLength().compareTo(rival.getRealLength()) > 0; }

	// <editor-fold desc="implementing abstract model">

	// TODO: AbstractPainter
	public void drawOn(Graphics surface, int x, int y, Boolean completeRepaint) {
		surface.setColor(Color.BLACK);
		surface.drawImage(getEbonySignImage(), x + dx() / 2, y + 3 * dy() + 2, null);

		int colorChannel = getIsMuted() || isPause() ? 9 : getChannel();
		BufferedImage tmpImg;
		if (isTooShort()) {
			tmpImg = getImageStorage().getTooShortImage();
		} else if (isTooLong()) {
			tmpImg = getImageStorage().getTooLongImage();
		} else {
			tmpImg = getImageStorage().getNotaImg(getCleanLength(), colorChannel);
		}

		if (getIsLinkedToNext()) {
			Fp.drawParabola((Graphics2D) surface, new Rectangle(x + dx() * 3 / 2, y + dy() * 7, dx() * 2, dy() * 2));
		}

		surface.drawImage(tmpImg, x + getNotaImgRelX(), y, null);

		if (isTriplet.get()) {
			surface.setColor(ImageStorage.getColorByChannel(channel.get()));
			Rectangle rect = new Rectangle(x + dx() /4, y + 6 * dy(), dx() / 2, dy() * 2);
			Fp.fitTextIn(rect, "3", surface);
		}

		for (int i = 0; i < getDotCount(); ++i) {
			surface.fillOval(x + dx() * 5/3 + dx() * i / getDotCount(), y + dy() * 7, dy(), dy());
		}

		if (isStriked()) {
			AbstractPainter.Straight stra = getTraitCoordinates().plus(new Point(x, y));
			surface.drawLine(stra.p1.x, stra.p1.y, stra.p2.x, stra.p2.y);
		}
	}

	// TODO: AbstractPainter
	private AbstractPainter.Straight getTraitCoordinates() {
		int r = getSettings().getNotaWidth() * 12 / 25;
		return new AbstractPainter.Straight(getAncorPoint().plus(-r, 0), getAncorPoint().plus(r, 0));
	}

	@Override
	public MidianaComponent getFocusedChild() { return null; }
	@Override
	protected NotaHandler makeHandler() { return new NotaHandler(this); }

	@Override
	public int hashCode() {
		return (tune.get().byteValue() << (8 * 3) + limit(channel.get(), 0, 15) << (8 * 2 + 4)); // 1111 1111 1111 0000 0000 0000 0000 0000
	}

	@Override
	public boolean equals(Object rival) { 	// it's a bit arguable. this equals is supposed to be used only in context of one accord or Playback (two equal nota-s cant sound simulatenously)
		return rival instanceof Nota && ((Nota)rival).tune.get() == this.tune.get() && ((Nota)rival).channel.get() == this.channel.get();
	}

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

//	public int getAcademicIndex() {
//		return isEbony() && getIsSharp()
//				? INota.ivoryMask(this.tune.get()) - 1
//				: INota.ivoryMask(this.tune.get());
//	}

	public int getTimeMilliseconds(Boolean includeLinkedTime) {
		StaffConfig config = getParentAccord().getParentStaff().getConfig();
		int linkedTime = (includeLinkedTime && linkedTo() != null) ? linkedTo().getTimeMilliseconds(true) : 0;

		return getTimeMilliseconds(getRealLength(), config.getTempo()) + linkedTime;
	}

	public static int getTimeMilliseconds(Fraction length, int tempo) {
//		int minute = 60 * 1000;
		int minute = 60 * 960; /** @debug */
		int semibreveTime = 4 * minute / tempo;
		return length.multiply(semibreveTime).intValue();
	}

	public byte getVolume() {
		if (getIsMuted() || isPause()) {
			return 0; // пауза лол какбэ
		} else {
			StaffConfig config = getParentAccord().getParentStaff().getConfig();
			return (byte)(127 * config.getVolume(getChannel()) / 100);
		}
	}

	private BufferedImage getEbonySignImage() {
		return  !this.isEbony() ? null :
				getIsSharp() ? getImageStorage().getSharpImage() :
				getImageStorage().getFlatImage();
	}

	private int getDotCount() {

		Fraction length = this.length.get().divide(ImageStorage.getTallLimit().multiply(2)); // to make sure, that the length is less than 1
		// don't worry, denominator does not affect dot count

		return 31 - Integer.numberOfLeadingZeros(length.getNumerator() + 1) - 1; // 3/8 => 001_1_ + 1 = 0100 => 3 - 2 = 1 >D
	}

	private void setDotCount(int dotCount) {
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
		return length.get().getDenominator() == 1
				? new Fraction(length.get().getNumerator() * 2, length.get().getDenominator() + 1)
				: new Fraction(length.get().getNumerator() + 1, length.get().getDenominator() * 2);
	}

	// </editor-fold>

	// <editor-fold desc="one-line-getters">

	// 0 - do, 2 - re, 4 - mi, 5 - fa, 7 - so, 9 - la, 10 - ti
	public Boolean isBotommedToFitSystem() { return this.getOctave() > 6; } // 8va
	public Boolean isStriked() { return ivoryIndex() % 2 == 1; }

	@Override
	public int ivoryIndex() {
		return isPause() ? PAUSE_POSITION : INota.super.ivoryIndex() - (isEbony() && isSharp.get() ? 1 : 0);
	}
	public Pnt getAncorPoint() { return new Pnt(getWidth()*16/25, getHeight() - dy()); }
	public int getNotaImgRelX() { return this.getWidth() / 2; } // bad name
	public int getStickX() { return this.getNotaImgRelX() + dx() / 2; }

	public int getHeight() { return getSettings().getNotaHeight(); }
	// TODO: use it in accord.getWidth()
	public int getWidth() { return dx() * 2; }

	private Boolean isPause() { return tune.get() == 0; }

	// </editor-fold>

	// <editor-fold desc="model getters/setters">

	// TODO: we can use this.{field}.get instead of manually creating separate getters
	// model getters
	public Integer getTune() { return tune.get(); }
	public Integer getChannel() { return channel.get(); }
	public Fraction getLength() { return length.get(); }
	public Boolean isTriplet() { return isTriplet.get(); }
	public Boolean getIsSharp() { return isSharp.get(); }
	public Boolean getIsMuted() { return isMuted.get(); }
	public Boolean getIsLinkedToNext() { return isLinkedToNext.get(); }
	// model setters
	public Nota setTune(int value) {
		this.tune.set(value);
		return this;
	}
	public Nota setLength(Fraction value){ this.length.set(value); return this; }
	/** @Bug - nota is immutable, this will blow with fatal !!! */
	public Nota setChannel(int value) { this.channel.set(value); return this; }
	public Nota setIsSharp(Boolean value) { this.isSharp.set(value); return this; }
	public Nota setIsMuted(Boolean value) { this.isMuted.set(value); return this; }
	public Nota setIsLinkedToNext(Boolean value) { this.isLinkedToNext.set(value); return this; }

	// </editor-fold>

	public Accord getParentAccord() { return (Accord)this.getModelParent(); }
	public Nota setKeydownTimestamp(long value) { this.keydownTimestamp = value; return this; }

	// <editor-fold desc="event handles">

	public Nota triggerIsSharp() {
		setIsSharp(!getIsSharp()); return this; }
	public Nota triggerIsMuted() {
		setIsMuted(!getIsMuted()); return this; }
	public Boolean triggerIsLinkedToNext() {
		if (getNext() != null || getIsLinkedToNext() == true) {
			setIsLinkedToNext(!getIsLinkedToNext());
			return true;
		} else {
			return false;
		}
	}
	public Nota triggerTupletDenominator() {
		isTriplet.set(!isTriplet.get());
		return this;
	}

	public Nota linkedTo() {
		return this.getIsLinkedToNext() ? this.getNext() : null;
	}

	/** @return nota of the next accord with same tune or null */
	private Nota getNext() {
		Accord nextAccord = getParentAccord().getNext();
		return nextAccord != null
				? nextAccord.findByTuneAndChannel(this.tune.get(), this.getChannel())
				: null;
	}

	public Nota incLen() {
		setLength(new Fraction(length.get().getNumerator() * 2, length.get().getDenominator()));
		return this;
	}

	public Nota decLen() {
		setLength(new Fraction(length.get().getNumerator(), length.get().getDenominator() * 2));
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

	final public static int OCTAVA = 12;

	// small octava
	final public static int DO = 48;
	final public static int RE = 50;
	final public static int MI = 52;
	final public static int FA = 53;
	final public static int SO = 55;
	final public static int LA = 57;
	final public static int TI = 59;

	// private static methods
}