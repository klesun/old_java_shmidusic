package org.shmidusic.sheet_music.staff;

import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.chord.Tact;
import org.klesun_model.IModel;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;

import java.util.*;

import org.shmidusic.MainPanel;
import org.shmidusic.stuff.graphics.Settings;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;


import org.shmidusic.stuff.tools.Fp;
import org.shmidusic.stuff.tools.INote;
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
	public Arr<Chord> chordList = new Arr<>("chordList", new ArrayList<>(), this, Chord.class);
    private List<Tact> tactList = new ArrayList<>();
	public int focusedIndex = -1;

	public Staff()
	{
		this.staffConfig = new StaffConfig();
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
		chordList.add(chord, index);
		accordListChanged();
		return chord;
	}

	public synchronized void remove(Chord chord) {
		int index = getChordList().indexOf(chord);
		getChordList().remove(chord);

		accordListChanged();
	}

	public void accordListChanged()
	{
		this.tactList = recalcTactList(); // TODO: maybe do some optimization using repaintAllFromIndex
	}

	@Override
	public JSONObject getJsonRepresentation() {
		return super.getJsonRepresentation()
			.put("staffConfig", this.getConfig().getJsonRepresentation());
	}

	private List<Tact> recalcTactList()
	{
		// TODO: maybe move implementation into Tact or TactMeasurer
		List<Tact> result = new ArrayList<>();

		TactMeasurer measurer = new TactMeasurer(getConfig().getTactSize());

		int i = 0;
		Tact currentTact = new Tact(getConfig().getTactSize());
		for (Chord chord : chordList.get()) {
			chord.tactNumber.set(i + "");
			currentTact.chordList.add(chord);
			if (measurer.inject(chord)) {
				currentTact.setPrecedingRest(measurer.sumFraction);
				result.add(currentTact);
				currentTact = new Tact(getConfig().getTactSize());
				++i;
			}
		}
		if (currentTact.chordList.size() > 0) {
			result.add(currentTact);
		}

		return result;
	}

	public Optional<Tact> findTact(Chord chord)
	{
		// for now i'll use binary search, but this probably may be resolved with something efficientier
		// like storing owner tact in each accord... nda...

		int chordIdx = chordList.indexOf(chord);
		Function<Tact, Integer> pred = t ->
			t.chordList.contains(chord) ? 0 : chordIdx - chordList.indexOf(t.chordList.get(0));

		return Fp.findBinary(tactList, pred);
	}

    public Optional<Fraction> findChordStart(Chord chord)
    {
        return findTact(chord).map(tact -> {
            Fraction precedingChords = tact.chordList
                    .subList(0, tact.chordList.indexOf(chord))
                    .stream().map(c -> c.getFraction())
                    .reduce(Fraction::add).orElse(new Fraction(0));

            Fraction startFraction = getConfig().getTactSize().multiply(tactList.indexOf(tact)).add(precedingChords);
            if (!tact.getIsCorrect()) {
                startFraction = startFraction.add(tact.getPrecedingRest());
            }

            return startFraction;
        });
    }

    public Optional<Chord> findChord(Fraction start)
    {
        int tactNum = start.divide(getConfig().getTactSize()).intValue();
        return getTact(tactNum).flatMap(tact -> {
            Fraction chordPos = start.subtract(getConfig().getTactSize().multiply(tactNum));
            return tact.findChord(chordPos);
        });
    }

    public Optional<Chord> findClosestBefore(Fraction start)
    {
        if (chordList.size() == 0) {
            return Optional.empty();
        } else {
            int tactNum = start.divide(getConfig().getTactSize()).intValue();
            if (start.compareTo(getConfig().getTactSize().multiply(tactNum)) > 0) { // start is in the latter tact

                Fraction chordPos = start.subtract(getConfig().getTactSize().multiply(tactNum));
                return getTact(tactNum).flatMap(t -> t.findClosestBefore(chordPos));
            } else {
                Fraction chordPos = start.subtract(getConfig().getTactSize().multiply(tactNum - 1));
                return getTact(tactNum - 1).flatMap(t -> t.findClosestBefore(chordPos));
            }
        }
    }

    private Optional<Tact> getTact(int index) {
        return index >= 0 && index < tactList.size() ? Optional.of(tactList.get(index)) : Optional.empty();
    }

    public Optional<Chord> getChord(int index) {
        if (index < 0) {
            return chordList.size() + index >= 0
                    ? Optional.of(chordList.get(chordList.size() + index))
                    : Optional.empty();
        } else {
            return chordList.size() > index
                    ? Optional.of(chordList.get(index))
                    : Optional.empty();
        }
    }
	
	@Override
	public Staff reconstructFromJson(JSONObject jsObject) throws JSONException
	{
		super.reconstructFromJson(jsObject);

		JSONObject configJson = jsObject.getJSONObject("staffConfig");
		this.getConfig().reconstructFromJson(configJson);

		accordListChanged();

		return this;
	}

	// getters

	public Chord getFocusedAccord() {
		if (this.getFocusedIndex() > -1 && this.getFocusedIndex() < this.chordList.size()) {
			return getChordList().get(getFocusedIndex());
		} else {
			return null;
		}
	}

	public List<Chord> getChordList() {
		return (List<Chord>)this.chordList.get();
	}

	public Stream<Chord> chordStream()
	{
		return chordList.get().stream();
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

	public StaffConfig getConfig() {
		return this.staffConfig;
	}

	// TODO: it's not IModel's deal what is focused
	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public Staff setFocusedIndex(int value)
	{
		this.focusedIndex = limit(value, -1, getChordList().size() - 1);
		return this;
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
			if (INote.isDotable(chord.getFraction())) {
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

	private static Stream<JSONObject> toStream(JSONArray jsArray) throws JSONException
	{
		return IntStream.range(0, jsArray.length()).boxed().map(i -> jsArray.getJSONObject(i));
	}
}


