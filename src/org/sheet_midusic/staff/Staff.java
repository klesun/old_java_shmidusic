package org.sheet_midusic.staff;

import org.klesun_model.AbstractModel;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.staff.chord.Tact;
import org.sheet_midusic.staff.chord.nota.Nota;
import org.klesun_model.Explain;
import org.klesun_model.IModel;
import org.sheet_midusic.staff.chord.ChordHandler;
import org.sheet_midusic.staff.staff_config.StaffConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sheet_midusic.staff.staff_panel.MainPanel;
import org.sheet_midusic.staff.staff_panel.StaffComponent;
import org.sheet_midusic.stuff.Midi.DeviceEbun;
import org.sheet_midusic.stuff.Midi.Playback;
import org.sheet_midusic.stuff.graphics.Settings;
import org.sheet_midusic.stuff.musica.PlayMusThread;

import java.util.stream.IntStream;
import java.util.stream.Stream;


import org.sheet_midusic.stuff.tools.Logger;
import org.sheet_midusic.stuff.tools.jmusic_integration.INota;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** A Staff is part of SheetMusic with individual StaffConfig properties (keySignature/tempo/tactSize) */
public class Staff extends AbstractModel
{
	final public static int SISDISPLACE = 40;
	public static final int DEFAULT_ZNAM = 64; // TODO: move it into some constants maybe

	public enum aMode { insert, passive }
	public static aMode mode = aMode.insert;

	public StaffConfig staffConfig = null;

	// TODO: MUAAAAH, USE FIELD CLASS MAZAFAKA AAAAAA!
	private List<Chord> chordList = new ArrayList<>();
	private List<Tact> tactList = new ArrayList<>();
	public int focusedIndex = -1;

	@Deprecated final private MainPanel blockPanel;
	final private Playback playback;

	public Staff(MainPanel blockPanel)
	{
//		super(null);
		this.blockPanel = blockPanel;
		this.staffConfig = new StaffConfig(this);
		this.playback = new Playback(this);
	}

	public synchronized Chord addNewAccordWithPlayback()
	{
		Chord chord = addNewAccord(getFocusedIndex() + 1);
		this.moveFocus(1);
		if (DeviceEbun.isPlaybackSoftware()) { // i.e. when playback is not done with piano - no need to play pressed chord, user hears it anyways
			new Thread(() -> {
				try {
					Thread.sleep(ChordHandler.ACCORD_EPSILON);
					PlayMusThread.playAccord(chord);
				} catch (InterruptedException exc) {
					Logger.error("okay...");
				}
			}).start();
		}

		return chord;
	}

	public Chord addNewAccord()
	{
		return addNewAccord(chordList.size());
	}

	public Chord addNewAccord(int position)
	{
		// TODO: it's a temporary hack till we completely separate Model from Component
		StaffComponent hackPanel = blockPanel.staffContainer.getFocusedChild();
		return add(new Chord(hackPanel), position);
	}

	/** TODO: public is temporary */
	public synchronized Chord add(Chord chord, int index) {
		getChordList().add(index, chord);
		accordListChanged(index);
		return chord;
	}

	public synchronized void remove(Chord chord) {
		int index = getChordList().indexOf(chord);
		if (index <= getFocusedIndex()) { setFocusedIndex(getFocusedIndex() - 1); }
		getChordList().remove(chord);

		accordListChanged(index);
	}

	private void accordListChanged(int repaintAllFromIndex) {
		int width = getParentSheet().getWidth();
		getParentSheet().staffContainer.setPreferredSize(new Dimension(10/*width - 25*/, getHeightIf(width)));	//	Needed for the scrollBar bars to appear
		getParentSheet().staffContainer.revalidate();	//	Needed to recalc the scrollBar bars

//		getChordList().subList(repaintAllFromIndex, getChordList().size()).forEach(Chord::surfaceChanged);
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
				result.add(currentTact);
				currentTact = new Tact(i++);
			}
		}
		if (currentTact.accordList.size() > 0) {
			result.add(currentTact);
		}

		return result;
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

	public List<List<Chord>> getAccordRowList() {
		List<List<Chord>> resultList = new ArrayList<>();
		for (int fromIdx = 0; fromIdx < this.getChordList().size(); fromIdx += getAccordInRowCount()) {
			resultList.add(this.getChordList().subList(fromIdx, Math.min(fromIdx + getAccordInRowCount(), this.getChordList().size())));
		}

		if (resultList.isEmpty()) { resultList.add(new ArrayList<>()); }
		return resultList;
	}

	public int getHeightIf(int width) {
		return getAccordRowList().size() * SISDISPLACE * dy() + getMarginY();
	}


	public int getWidth() { return getParentSheet().getWidth(); }
	@Deprecated // no comments
	public int getHeight() { return getParentSheet().getHeight(); }

	public int getMarginX() {
		return Math.round(MainPanel.MARGIN_H * dx());
	}
	public int getMarginY() {
		return Math.round(MainPanel.MARGIN_V * dy());
	}

	public int getAccordInRowCount() {
		int result = this.getWidth() / (dx() * 2) - 3; // - 3 because violin key and phantom
		return Math.max(result, 1);
	}

	final private int dx() { return Settings.inst().getStepWidth(); }
	final private int dy() { return Settings.inst().getStepHeight(); }

	// field getters/setters

	public StaffConfig getConfig() {
		return this.staffConfig;
	}
	public MainPanel getParentSheet() { // ???
		return this.blockPanel;
	}
	public Playback getPlayback() { return this.playback; }
	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public Staff setFocusedIndex(int value) {
		if (this.getFocusedAccord() != null) { this.getFocusedAccord().setFocusedIndex(-1).surfaceChanged(); } // surfaceChanged - to erase pointer

		this.focusedIndex = limit(value, -1, getChordList().size() - 1);
		if (this.getFocusedAccord() != null) { this.getFocusedAccord().surfaceChanged(); } // to draw pointer

		return this;
	}

	// action handles

	public Explain moveFocusWithPlayback(int sign, Boolean interruptSounding) {
		Explain result = moveFocus(sign);
		if (getFocusedAccord() != null && result.isSuccess()) {

			if (interruptSounding) {
				PlayMusThread.shutTheFuckUp();
				playback.interrupt();
			}
			PlayMusThread.playAccord(getFocusedAccord());
		}
		return result;
	}

	public Explain moveFocusWithPlayback(int sign) {
		return moveFocusWithPlayback(sign, true);
	}

	public Explain moveFocusTact(int sign) {
		return new Explain(false, "Not Implemented Yet!");
	}

	public Explain moveFocusRow(int sign) {
		int n = sign * getAccordInRowCount();
		return moveFocusWithPlayback(n);
	}

	public Explain moveFocus(int n)
	{
		int wasIndex = getFocusedIndex();
		setFocusedIndex(getFocusedIndex() + n);

		return getFocusedIndex() != wasIndex ? new Explain(true) : new Explain(false, "dead end").setImplicit(true);
	}

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
					// TODO: maybe if last chord in org.sheet_midusic.staff then no need
					// put nota with onset length into newNota's chord to preserve timing
					newChord.addNewNota(0, 0).setLength(offset);
				} else if (newNota.getLength().compareTo(offset) < 0) {
					// TODO: maybe if last chord in org.sheet_midusic.staff then no need
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


