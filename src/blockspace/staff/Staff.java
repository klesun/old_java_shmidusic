package blockspace.staff;

import blockspace.staff.accord.Chord;
import blockspace.staff.accord.Tact;
import blockspace.staff.accord.nota.Nota;
import model.Explain;
import model.IModel;
import model.SimpleAction;
import blockspace.staff.accord.AccordHandler;
import blockspace.staff.StaffConfig.StaffConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import stuff.Midi.DeviceEbun;
import stuff.Midi.Playback;
import stuff.musica.PlayMusThread;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;


import stuff.tools.jmusic_integration.INota;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Staff extends MidianaComponent {

	final public static int SISDISPLACE = 40;
	public static final int DEFAULT_ZNAM = 64; // TODO: move it into some constants maybe

	public enum aMode { insert, passive }
	public static aMode mode = aMode.insert;

	public StaffConfig staffConfig = null;

	// TODO: MUAAAAH, USE FIELD CLASS MAZAFAKA AAAAAA!
	private ArrayList<Chord> chordList = new ArrayList<>();
	public int focusedIndex = -1;

	final private StaffPanel blockPanel;
	final private Playback playback;

	private Boolean surfaceChanged = true;

	public Staff(StaffPanel blockPanel) {
		super(null);
		this.blockPanel = blockPanel;
		this.staffConfig = new StaffConfig(this);
		this.playback = new Playback(this);
	}

	public synchronized Chord addNewAccordWithPlayback()
	{
		Chord chord = addNewAccord(getFocusedIndex() + 1);
		this.moveFocus(1);
		if (DeviceEbun.isPlaybackSoftware()) { // i.e. when playback is not done with piano - no need to play pressed chord, user hears it anyways
			new Thread(() -> { Uninterruptibles.sleepUninterruptibly(AccordHandler.ACCORD_EPSILON, TimeUnit.MILLISECONDS); PlayMusThread.playAccord(chord); }).start();
		}

		return chord;
	}

	public Chord addNewAccord()
	{
		return addNewAccord(chordList.size());
	}

	public Chord addNewAccord(int position)
	{
		return add(new Chord(this), position);
	}

	/** TODO: public is temporary */
	public synchronized Chord add(Chord chord, int index) {
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> getChordList().add(index, chord))
			.setUndo(() -> getChordList().remove(chord)));

		accordListChanged(index);

		return chord;
	}

	public synchronized void remove(Chord chord) {
		int index = getChordList().indexOf(chord);
		if (index <= getFocusedIndex()) { setFocusedIndex(getFocusedIndex() - 1); }
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> getChordList().remove(chord))
			.setUndo(() -> getChordList().add(index, chord)));

		accordListChanged(index);
	}

	private void accordListChanged(int repaintAllFromIndex) {
		int width = getParentSheet().getParentBlock().getWidth();
		getParentSheet().staffContainer.setPreferredSize(new Dimension(10/*width - 25*/, getHeightIf(width)));	//	Needed for the scrollBar bars to appear
		getParentSheet().staffContainer.revalidate();	//	Needed to recalc the scrollBar bars

		getChordList().subList(repaintAllFromIndex, getChordList().size()).forEach(Chord::surfaceChanged);
	}

    @Override
    public void drawOn(Graphics2D g, int x, int y, Boolean completeRepaint) {
        drawOn(g, completeRepaint);
    }

	public synchronized void drawOn(Graphics2D g, Boolean completeRepaintRequired) {
		new StaffPainter(this, g, 0, 0).draw(completeRepaintRequired);
	}

	@Override
	public JSONObject getJsonRepresentation() {
		return new JSONObject()
			.put("staffConfig", this.getConfig().getJsonRepresentation())
			.put("tactList", getTactStream().stream().map(IModel::getJsonRepresentation).toArray());
	}

	public List<Tact> getTactStream()
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

	@Override
	public StaffHandler getHandler() { return (StaffHandler)super.getHandler(); }

	public Staff clearStan() {
		this.getChordList().clear();
		this.focusedIndex = -1;

		return this;
	}

	@Override
	public Chord getFocusedChild() { return getFocusedAccord(); }
	@Override
	protected StaffHandler makeHandler() { return new StaffHandler(this); }

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
	public int getHeight() { return getParentSheet().getHeight(); }

	public int getMarginX() {
		return Math.round(StaffPanel.MARGIN_H * dx());
	}
	public int getMarginY() {
		return Math.round(StaffPanel.MARGIN_V * dy());
	}

	public int getAccordInRowCount() {
		int result = this.getWidth() / (dx() * 2) - 3; // - 3 because violin key and phantom
		return Math.max(result, 1);
	}

	// field getters/setters

	public StaffConfig getConfig() {
		return this.staffConfig;
	}
	public StaffPanel getParentSheet() { // ???
		return this.blockPanel;
	}
	public Playback getPlayback() { return this.playback; }
	@Override
	public StaffPanel getModelParent() { return getParentSheet(); }
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

    public void triggerPlayback() {
        this.playback.trigger();
    }

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
					this.add(new Chord(this), i + 1).addNewNota(0, 0).setLength(dl);
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

				Chord newChord = this.add(new Chord(this), i);
				Nota newNota = newChord.addNewNota(nota);
				if (newNota.getLength().compareTo(offset) > 0) {
					// TODO: maybe if last chord in staff then no need
					// put nota with onset length into newNota's chord to preserve timing
					newChord.addNewNota(0, 0).setLength(offset);
				} else if (newNota.getLength().compareTo(offset) < 0) {
					// TODO: maybe if last chord in staff then no need
					// put an empty nota after and set it's length(onset - newNota.getLength())
					this.add(new Chord(this), i + 1).addNewNota(0, 0).setLength(offset.subtract(newNota.getLength()));
				}

				return newNota;
			}

			Fraction accordFraction = getChordList().get(i).getFraction();

			curPos = new Fraction(curPos.doubleValue() + accordFraction.doubleValue());
		}

		Fraction rest = desiredPos.subtract(curPos);
		if (!rest.equals(new Fraction(0))) {
			this.add(new Chord(this), getChordList().size()).addNewNota(0,0).setLength(rest);
		}

		// TODO: we don't handle here pause prefix! (i.e when desired start is more than end) !!!
		// if not returned already
		return this.add(new Chord(this), getChordList().size()).addNewNota(nota);
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


