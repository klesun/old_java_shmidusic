package Model.Staff.Accord.Nota;


import Gui.ImageStorage;
import Model.Combo;
import Model.Containers.Panels.MusicPanel;
import Model.Staff.Accord.Accord;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONException;
import org.json.JSONObject;

import Gui.Settings;
import Model.AbstractModel;
import Model.Staff.StaffConfig.StaffConfig;
import Model.Staff.Staff;
import Tools.Fp;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class Nota extends AbstractModel implements Comparable<Nota> {

	public int tune = 34; // no exceptions
	public int channel = 0;
	public Boolean isSharp = false;

	public int numerator = 16;
	public int tupletDenominator = 1;
	Boolean isMuted = false;

	public long keydownTimestamp;

	public Nota(Accord parent, int tune) {
		super(parent);
		setTune(tune);
		parent.add(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tune;
		return result;
	}
	@Override
	public String toString() { return this.getJsonRepresentation().toString(); }
	@Override
	public boolean equals(Object obj) { return obj != null && getClass() == obj.getClass() && tune == ((Nota)obj).tune; }

	public Boolean isLongerThan(Nota rival) {
		return getFraction().compareTo(rival.getFraction()) > 0;
	}

	@Override
	public void drawOn(Graphics surface, int x, int y) {
		surface.setColor(Color.BLACK);
		surface.drawImage(getEbonySignImage(), x + dx() / 2, y + 3 * dy() + 2, getPanel());

		BufferedImage tmpImg = getIsMuted()
			? ImageStorage.inst().getNotaImg(numerator, 9)
			: ImageStorage.inst().getNotaImg(numerator, channel);

		surface.drawImage(tmpImg, x + getNotaImgRelX(), y, null);

		if (this.tupletDenominator != 1) { for (int i = 0; i < 3; ++i) { surface.drawLine(x + getStickX(), y + i, x + getStickX() -6, y + i); } }
		if (this.numerator % 3 == 0) surface.fillOval(x + Settings.getStepWidth() + getWidth()*2/5, y + getHeight()*7/8, getHeight()/8, getHeight()/8);
	}

	// getters/setters
	
	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject dict = new JSONObject();
		dict.put("tune", this.tune);
		dict.put("numerator", this.numerator);
		dict.put("channel", this.channel);
		dict.put("isSharp", this.isSharp);
		dict.put("tupletDenominator", this.tupletDenominator);
		dict.put("isMuted", this.isMuted);
	
		return dict;
	}

	@Override
	public Nota reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.tune = jsObject.getInt("tune");
		this.numerator = jsObject.getInt("numerator");
		this.channel = jsObject.getInt("channel");
		if (jsObject.has("isSharp")) { this.isSharp = jsObject.getBoolean("isSharp"); }
		if (jsObject.has("tupletDenominator")) { this.tupletDenominator = jsObject.getInt("tupletDenominator"); }
		if (jsObject.has("isMuted")) { this.isMuted = jsObject.getBoolean("isMuted"); }
	
		return this;
	}

	public int getAcademicIndex() {
		int idx = Nota.tuneToAcademicIndex(this.tune);
		if (isEbony() && isSharp) {
			idx -= 1;
		}
		return idx;
	}

	public List<Integer> getTraitCoordinates() {
		ArrayList result = new ArrayList();
		result.addAll(Fp.vectorSum(getAncorPoint(), Arrays.asList(-getWidth() * 6 / 25, 0)));
		result.addAll(Fp.vectorSum(getAncorPoint(), Arrays.asList(+getWidth() * 6 / 25, 0)));
		return result;
	}

	public int getTimeMiliseconds() {
		int minute = 60 * 1000;
		StaffConfig config = getParentAccord().getParentStaff().getConfig();
		return minute * 4 / Staff.DEFAULT_ZNAM / config.valueTempo * getNumerator() / getDenominator();
		// 4 - будем брать четвертную как основную
	}

	public byte getVolume() {
		if (this.tune == 36) {
			return 0; // пауза лол какбэ
		} else {
			StaffConfig config = getParentAccord().getParentStaff().getConfig();
			return (byte)(127 * config.getVolumeArray()[channel] / 100);
		}
	}

	private BufferedImage getEbonySignImage() {
		return !this.isEbony()
			? null // not sure that it will do the trick...
			: isSharp ? ImageStorage.inst().getSharpImage()
			: ImageStorage.inst().getFlatImage();
	}

	// one-line-obvious-purpose methods

	// 0 - do, 2 - re, 4 - mi, 5 - fa, 7 - so, 9 - la, 10 - ti
	public Boolean isEbony() { return Arrays.asList(1,3,6,8,10).contains(this.tune % 12); }
	public Boolean isBotommedToFitSystem() { return this.getOctave() > 6; } // 8va
	public Boolean isStriked() { return getAbsoluteAcademicIndex() % 2 == 1; }

	public int getAbsoluteAcademicIndex() { return getAcademicIndex() + getOctave() * 7; }
	public int getOctave() { return this.tune/12; }
	public List<Integer> getAncorPoint() { return Arrays.asList(getWidth()*16/25, Settings.getStepHeight() * 7); }
	public int getNotaImgRelX() { return this.getWidth() / 2; } // bad name
	public int getStickX() { return this.getNotaImgRelX() + dx() / 2; }

	public int getHeight() { return Settings.getNotaHeight(); }
	// TODO: use it in Accord.getWidth()
	public int getWidth() { return Settings.getNotaWidth() * 2; }

	// field getters/setters

	public Accord getParentAccord() { return (Accord)this.getModelParent(); }

	public int getNumerator() { return this.numerator; }
	public int getDenominator() { return this.getTupletDenominator(); }
	public Fraction getFraction() { return new Fraction(getNumerator(), getTupletDenominator() * Staff.DEFAULT_ZNAM); }

	public int getTupletDenominator() { return this.tupletDenominator; }
	public Nota setTupletDenominator(int value) { this.tupletDenominator = value; return this; }

	public Boolean getIsMuted() { return this.isMuted; }
	
	public Nota setIsMuted(Boolean value) { this.isMuted = value; return this; }
	public Nota setChannel(int channel) { this.channel = channel; return this; }
	public Nota setTune(int value){ this.tune = value; return this; }
	public Nota setKeydownTimestamp(long value) { this.keydownTimestamp = value; return this; }

	public MusicPanel getPanel() { return this.getParentAccord().getParentStaff().getParentSheet(); }

	// event handles

	public Nota triggerIsSharp() { this.isSharp = !this.isSharp; return this; }
	public Nota triggerIsMuted() { setIsMuted(!getIsMuted()); return this; }
	public Nota triggerTupletDenominator() { setTupletDenominator(getTupletDenominator() == 3 ? 1 : 3); return this; }

	public Nota incLen() {
		// TODO: points should be whole separate thing
		numerator += numerator % 3 == 0
				? numerator/3
				: numerator/2;
		numerator = Math.min(numerator, Staff.DEFAULT_ZNAM * 2);
		return this;
	}

	public Nota decLen() {
		// TODO: points should be whole separate thing
		numerator -= numerator % 3 == 0
				? numerator/3
				: numerator/4;
		numerator = Math.max(numerator, 4);
		return this;
	}

	public Nota changeLength(Combo combo) {
		return combo.getSign() > 0
				? incLen()
				: decLen();
	}

	// private static methods

	// TODO: what if i said we could store these instead of numbers in json?
	private static String strIdx(int n){ return Arrays.asList("do","re","mi","fa","so","la","ti").get(n % 12); }
	private static int tuneToAcademicIndex(int tune) { return Arrays.asList(0,1,1,2,2,3,4,4,5,5,6,6).get(tune % 12); }

	@Override
	public List<? extends AbstractModel> getChildList() {
		return new ArrayList<>();
	}
	@Override
	public AbstractModel getFocusedChild() {
		return null;
	}
	@Override
	protected NotaHandler makeHandler() { return new NotaHandler(this); }
	@Override
	public int compareTo(Nota n) { return n.tune - this.tune; }

}