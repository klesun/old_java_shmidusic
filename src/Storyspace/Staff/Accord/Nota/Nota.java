package Storyspace.Staff.Accord.Nota;


import Gui.ImageStorage;
import Model.Combo;
import Model.Helper;
import Model.ModelField;
import Storyspace.Staff.StaffPanel;
import Storyspace.Staff.Accord.Accord;
import java.awt.image.BufferedImage;
import java.util.*;

import Stuff.OverridingDefaultClasses.TruHashMap;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONException;
import org.json.JSONObject;

import Gui.Settings;
import Model.AbstractModel;
import Storyspace.Staff.StaffConfig.StaffConfig;
import Storyspace.Staff.Staff;
import Stuff.Tools.Fp;

import java.awt.Color;
import java.awt.Graphics;

public class Nota extends AbstractModel implements Comparable<Nota> {
	
	private List<ModelField> fieldValueStorage = new ArrayList<>();

	private ModelField tuneField = addField("tune", 34); // no exceptions
	private ModelField numeratorField = addField("numerator", 16);
	private ModelField channelField = addField("channel", 0);
	private ModelField tupletDenominatorField = addField("tupletDenominator", 1);
	private ModelField isSharpField = addField("isSharp", false);
	private ModelField isMutedField = addField("isMuted", false);

	private ModelField addField(String fieldName, Object fieldValue) {
		ModelField field = new ModelField(fieldName, fieldValue);
		fieldValueStorage.add(field);
		return field;
	}

	public long keydownTimestamp;

	public Nota(Accord parent, int tune) {
		super(parent);
		setTune(tune);
		parent.add(this);
	}

	public Boolean isLongerThan(Nota rival) {
		return getFraction().compareTo(rival.getFraction()) > 0;
	}

	@Override
	public void drawOn(Graphics surface, int x, int y) {
		surface.setColor(Color.BLACK);
		surface.drawImage(getEbonySignImage(), x + dx() / 2, y + 3 * dy() + 2, getPanel());

		BufferedImage tmpImg = getIsMuted()
				? ImageStorage.inst().getNotaImg(getNumerator(), 9)
				: ImageStorage.inst().getNotaImg(getNumerator(), getChannel());

		surface.drawImage(tmpImg, x + getNotaImgRelX(), y, null);

		if (getTupletDenominator() != 1) { for (int i = 0; i < 3; ++i) { surface.drawLine(x + getStickX(), y + i, x + getStickX() -6, y + i); } }
		if (getNumerator() % 3 == 0) { surface.fillOval(x + Settings.getStepWidth() + getWidth()*2/5, y + getHeight()*7/8, getHeight()/8, getHeight()/8); }
	}

	// getters/setters
	
	@Override
	public void getJsonRepresentation(JSONObject dict) {
		for (ModelField field: fieldValueStorage) {
			dict.put(field.getName(), field.getValue());
		}
	}

	@Override
	public Nota reconstructFromJson(JSONObject jsObject) throws JSONException {
		for (ModelField field: fieldValueStorage) {
			if (jsObject.has(field.getName())) { field.setValueFromJsObject(jsObject); }
			else { Logger.warning("Source does not have field [" + field.getName() + "] for class {" + getClass().getSimpleName() + "}"); }
		}
	
		return this;
	}

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

	public int getTimeMilliseconds() {
		int minute = 60 * 1000;
		StaffConfig config = getParentAccord().getParentStaff().getConfig();
		int semibreveTime = 4 * minute / config.valueTempo;
		return getFraction().multiply(semibreveTime).intValue();
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
		return !this.isEbony()
			? null // not sure that it will do the trick...
			: getIsSharp() ? ImageStorage.inst().getSharpImage()
			: ImageStorage.inst().getFlatImage();
	}

	// one-line-obvious-purpose methods

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

	// field getters/setters

	// model getters
	public Integer getTune() { return (Integer)tuneField.getValue(); } // TODO: make separate container classes for each primitive
	public Integer getNumerator() { return (Integer)numeratorField.getValue(); }
	public Integer getChannel() { return (Integer)channelField.getValue(); }
	public Integer getTupletDenominator() { return (Integer)tupletDenominatorField.getValue(); }
	public Boolean getIsSharp() { return (Boolean)isSharpField.getValue(); }
	public Boolean getIsMuted() { return (Boolean)isMutedField.getValue(); }
	// model setters
	public Nota setTune(int value){ this.tuneField.setValue(value); return this; }
	public Nota setNumerator(int value){ this.numeratorField.setValue(limit(value, 4, Staff.DEFAULT_ZNAM * 2)); return this; }
	public Nota setChannel(int value) { this.channelField.setValue(value); return this; }
	public Nota setTupletDenominator(int value) { this.tupletDenominatorField.setValue(value); return this; }
	public Nota setIsSharp(Boolean value) { this.isSharpField.setValue(value); return this; }
	public Nota setIsMuted(Boolean value) { this.isMutedField.setValue(value); return this; }

	public Accord getParentAccord() { return (Accord)this.getModelParent(); }
	public Fraction getFraction() { return new Fraction(getNumerator(), getTupletDenominator() * Staff.DEFAULT_ZNAM); }
	public Nota setKeydownTimestamp(long value) { this.keydownTimestamp = value; return this; }

	public StaffPanel getPanel() { return this.getParentAccord().getParentStaff().getParentSheet(); }

	// event handles

	public Nota triggerIsSharp() { setIsSharp(!getIsSharp()); return this; }
	public Nota triggerIsMuted() { setIsMuted(!getIsMuted()); return this; }
	public Nota triggerTupletDenominator() { setTupletDenominator(getTupletDenominator() == 3 ? 1 : 3); return this; }

	public Nota incLen() {
		// TODO: points should be whole separate thing
		int incrDenom = getNumerator() % 3 == 0 ? 3 : 2;
		setNumerator(getNumerator() + getNumerator() / incrDenom);
		return this;
	}

	public Nota decLen() {
		// TODO: points should be whole separate thing
		int decrDenom = getNumerator() % 3 == 0 ? 3 : 4;
		setNumerator(getNumerator() - getNumerator() / decrDenom);
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
	public AbstractModel getFocusedChild() {
		return null;
	}
	@Override
	protected NotaHandler makeHandler() { return new NotaHandler(this); }
	@Override
	public int compareTo(Nota n) { return n.getTune() - this.getTune(); }

}