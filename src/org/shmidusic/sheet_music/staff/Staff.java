package org.shmidusic.sheet_music.staff;

import org.klesun_model.AbstractModel;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.chord.Tact;
import org.shmidusic.sheet_music.staff.chord.nota.Nota;
import org.klesun_model.Explain;
import org.klesun_model.IModel;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.shmidusic.MainPanel;
import org.shmidusic.stuff.graphics.Settings;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;


import org.shmidusic.stuff.tools.Fp;
import org.shmidusic.stuff.tools.jmusic_integration.INota;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A Staff is part of SheetMusic with individual StaffConfig properties (keySignature/tempo/tactSize) */
public class Staff extends AbstractModel
{
	final public static int SISDISPLACE = 40; // it does not belong here
	public static final int DEFAULT_ZNAM = 64; // TODO: move it into some constants maybe

	public enum aMode { insert, passive }
	public static aMode mode = aMode.insert;

	public StaffConfig staffConfig = null;

	// TODO: MUAAAAH, USE FIELD CLASS MAZAFAKA AAAAAA!
	private List<Chord> chordList = new ArrayList<>();
	private List<Tact> tactList = new ArrayList<>();
	public int focusedIndex = -1;

	public Staff()
	{
		this.staffConfig = new StaffConfig(this);
	}

	public Chord addNewAccord()
	{
		return addNewAccord(chordList.size());
	}

	public Chord addNewAccord(int position) {
		return add(new Chord(), position);
	}

	/** TODO: public is temporary */
	public synchronized Chord add(Chord chord, int index) {
		getChordList().add(index, chord);
		accordListChanged(index);
		return chord;
	}

	public synchronized void remove(Chord chord) {
		int index = getChordList().indexOf(chord);
		getChordList().remove(chord);

		accordListChanged(index);
	}

	public void accordListChanged(int repaintAllFromIndex)
	{
		setFocusedIndex(limit(getFocusedIndex(), -1, chordList.size() - 1));
		this.tactList = recalcTactList(); // TODO: maybe do some optimization using repaintAllFromIndex
	}

//	public synchronized int drawOn(Graphics2D g, int x, int y) {
//		new StaffPainter(this, g, x, y).draw(true);
//		return getHeightIf(getWidth());
//	}

	@Override
	public JSONObject getJsonRepresentation() {
		return new JSONObject()
			.put("staffConfig", this.getConfig().getJsonRepresentation())
			.put("tactList", tactList.stream().map(IModel::getJsonRepresentation).toArray());
	}

	private List<Tact> recalcTactList()
	{
		// TODO: maybe move implementation into Tact or TactMeasurer
		List<Tact> result = new ArrayList<>();

		TactMeasurer measurer = new TactMeasurer(getConfig().getTactSize());

		int i = 0;
		Tact currentTact = new Tact(i++);
		for (Chord chord : chordList) {
			currentTact.accordList.add(chord);
			if (measurer.inject(chord)) {
				currentTact.setPrecedingRest(measurer.sumFraction);
				result.add(currentTact);
				currentTact = new Tact(i++);
			}
		}
		if (currentTact.accordList.size() > 0) {
			result.add(currentTact);
		}

		return result;
	}

	public Explain<Tact> findTact(Chord chord)
	{
		// for now i'll use binary search, but this probably may be resolved with something efficientier
		// like storing owner tact in each accord... nda...

		int chordIdx = chordList.indexOf(chord);
		Function<Tact, Integer> pred = t ->
			t.accordList.get().contains(chord) ? 0 : chordIdx - chordList.indexOf(t.accordList.get(0));

		return Fp.findBinary(tactList, pred);

		// SLOOOOOOOOOOOOOOOOOOOOOOW
//		return tactList.stream().filter(t -> t.accordList.get().contains(chord)).findAny();
	}

	// TODO: model, mazafaka!
	@Override
	public Staff reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.clearStan();

		JSONObject configJson = jsObject.getJSONObject("staffConfig");
		this.getConfig().reconstructFromJson(configJson);

		if (jsObject.has("chordList")) {
			toStream(jsObject.getJSONArray("chordList")).forEach(
				childJs -> this.addNewAccord().reconstructFromJson(childJs));

		} else if (jsObject.has("tactList")) {
			toStream(jsObject.getJSONArray("tactList")).forEach(
				childJs -> toStream(childJs.getJSONArray("chordList")).forEach(
					a -> addNewAccord().reconstructFromJson(a)));
		} else {
			throw new JSONException("Staff Json Has No Valid Children! It got only: " + Arrays.toString(jsObject.keySet().toArray()));
		}

		return this;
	}

	public Staff clearStan() {
		this.getChordList().clear();
		this.focusedIndex = -1;

		return this;
	}

//	public Chord getFocusedChild() { return getFocusedAccord(); }
//	protected StaffHandler makeHandler() { return new StaffHandler(this); }

	// getters

	public Chord getFocusedAccord() {
		if (this.getFocusedIndex() > -1) {
			return getChordList().get(getFocusedIndex());
		} else {
			return null;
		}
	}

	public List<Chord> getChordList() {
		return this.chordList;
	}

	public Stream<Chord> chordStream()
	{
		return chordList.stream();
	}

	public List<List<Chord>> getAccordRowList(int rowSize)
	{
		List<List<Chord>> resultList = new ArrayList<>();

		for (int fromIdx = 0; fromIdx < this.getChordList().size(); fromIdx += rowSize) {
			int toIndex = Math.min(fromIdx + rowSize, this.getChordList().size());
			resultList.add(this.getChordList().subList(fromIdx, toIndex));
		}

		if (resultList.isEmpty()) { resultList.add(new ArrayList<>()); }

		return resultList;
	}

	public int getMarginX() {
		return dx();
	}
	public int getMarginY() {
		return Math.round(MainPanel.MARGIN_V * dy());
	}

	final private int dx() { return Settings.inst().getStepWidth(); }
	final private int dy() { return Settings.inst().getStepHeight(); }

	// field getters/setters

	public StaffConfig getConfig() {
		return this.staffConfig;
	}

	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public Staff setFocusedIndex(int value)
	{
		this.focusedIndex = limit(value, -1, getChordList().size() - 1);
		return this;
	}

	// action handles

	/** @return - nota that we just put */
	public Nota putAt(Fraction desiredPos, INota nota)
	{
		// TODO: it's broken somehow. Bakemonogatari can be opent with Noteworthy, but cant with midiana
		// and yu-no, maybe move this method into some specialized class? it seems to be something serious
		// what this method does, yes it's definitely should be move into some MidiSheetMusicCalculator or so...

		Fraction curPos = new Fraction(0);
		for (int i = 0; i < getChordList().size(); ++i) {

			if (curPos.equals(desiredPos)) {
				Chord chord = getChordList().get(i);

				Fraction wasAccordLength = chord.getFraction();
				Nota newNota = chord.addNewNota(nota);

				if (!wasAccordLength.equals(chord.getFraction())) {
					// putting filler in case when chord length became smaller to preserve timing
					Fraction dl = wasAccordLength.subtract(chord.getFraction());
					this.addNewAccord(i + 1).addNewNota(0, 0).setLength(dl);
				}
				return newNota;
			} else if (curPos.compareTo(desiredPos) > 0) {

				Chord chord = getChordList().get(i - 1);
				Fraction offset = new Fraction(curPos.doubleValue() - desiredPos.doubleValue());
				Fraction onset = new Fraction(chord.getFraction().doubleValue() - offset.doubleValue());

				/** @debug */
//				if (onset.equals(new Fraction(0))) {
//					Logger.fatal("How came ?! " + chord.getFraction() + " " + offset + " " + curPos + " " + desiredPos);
//				}

				chord.addNewNota(0, 0).setLength(onset);

				Chord newChord = this.addNewAccord(i);
				Nota newNota = newChord.addNewNota(nota);
				if (newNota.getLength().compareTo(offset) > 0) {
					// TODO: maybe if last chord in org.shmidusic.staff then no need
					// put nota with onset length into newNota's chord to preserve timing
					newChord.addNewNota(0, 0).setLength(offset);
				} else if (newNota.getLength().compareTo(offset) < 0) {
					// TODO: maybe if last chord in org.shmidusic.staff then no need
					// put an empty nota after and set it's length(onset - newNota.getLength())
					this.addNewAccord(i + 1).addNewNota(0, 0).setLength(offset.subtract(newNota.getLength()));
				}

				return newNota;
			}

			Fraction accordFraction = getChordList().get(i).getFraction();

			curPos = new Fraction(curPos.doubleValue() + accordFraction.doubleValue());
		}

		Fraction rest = desiredPos.subtract(curPos);
		if (!rest.equals(new Fraction(0))) {
			this.addNewAccord().addNewNota(0,0).setLength(rest);
		}

		// TODO: we don't handle here pause prefix! (i.e when desired start is more than end) !!!
		// if not returned already
		return this.addNewAccord().addNewNota(nota);
	}

	public static class TactMeasurer {

		final private Fraction tactSize;

		public Fraction sumFraction = new Fraction(0);

		public int tactCount = 0;

		public TactMeasurer(Fraction tactSize) {
			this.tactSize = tactSize;
		}

		/** @returns true if chord finished the tact */
		public Boolean inject(Chord chord) {
			if (INota.isDotable(chord.getFraction())) {
				sumFraction = sumFraction.add(chord.getFraction());
			} else {
				sumFraction = new Fraction(sumFraction.doubleValue() + chord.getFraction().doubleValue());
			}

			Boolean finishedTact = false;
			while (sumFraction.compareTo(tactSize) >= 0) {
				sumFraction = sumFraction.subtract(tactSize);
				++tactCount;
				finishedTact = true;
			}

			return finishedTact;
		}
	}

	private Stream<JSONObject> toStream(JSONArray jsArray) throws JSONException
	{
		return IntStream.range(0, jsArray.length()).boxed().map(i -> jsArray.getJSONObject(i));
	}
}


