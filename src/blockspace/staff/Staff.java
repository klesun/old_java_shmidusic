package blockspace.staff;

import blockspace.staff.accord.Tact;
import blockspace.staff.accord.nota.Nota;
import model.Explain;
import model.IModel;
import model.SimpleAction;
import blockspace.staff.accord.AccordHandler;
import blockspace.staff.StaffConfig.StaffConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import blockspace.staff.accord.Accord;
import stuff.Midi.DeviceEbun;
import stuff.Midi.Playback;
import stuff.Musica.PlayMusThread;

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
	private ArrayList<Accord> accordList = new ArrayList<>();
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

	public synchronized Accord addNewAccordWithPlayback()
	{
		Accord accord = addNewAccord(getFocusedIndex() + 1);
		this.moveFocus(1);
		if (DeviceEbun.isPlaybackSoftware()) { // i.e. when playback is not done with piano - no need to play pressed accord, user hears it anyways
			new Thread(() -> { Uninterruptibles.sleepUninterruptibly(AccordHandler.ACCORD_EPSILON, TimeUnit.MILLISECONDS); PlayMusThread.playAccord(accord); }).start();
		}

		return accord;
	}

	public Accord addNewAccord()
	{
		return addNewAccord(accordList.size());
	}

	public Accord addNewAccord(int position)
	{
		return add(new Accord(this), position);
	}

	/** TODO: public is temporary */
	public synchronized Accord add(Accord accord, int index) {
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> getAccordList().add(index, accord))
			.setUndo(() -> getAccordList().remove(accord)));

		accordListChanged(index);

		return accord;
	}

	public synchronized void remove(Accord accord) {
		int index = getAccordList().indexOf(accord);
		if (index <= getFocusedIndex()) { setFocusedIndex(getFocusedIndex() - 1); }
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> getAccordList().remove(accord))
			.setUndo(() -> getAccordList().add(index, accord)));

		accordListChanged(index);
	}

	private void accordListChanged(int repaintAllFromIndex) {
		int width = getParentSheet().getParentBlock().getWidth();
		getParentSheet().staffContainer.setPreferredSize(new Dimension(10/*width - 25*/, getHeightIf(width)));	//	Needed for the scrollBar bars to appear
		getParentSheet().staffContainer.revalidate();	//	Needed to recalc the scrollBar bars

		getAccordList().subList(repaintAllFromIndex, getAccordList().size()).forEach(Accord::surfaceChanged);
	}

    @Override
    public void drawOn(Graphics g, int x, int y, Boolean completeRepaint) {
        drawOn(g, completeRepaint);
    }

	// TODO: move into some StaffPainter class
	public synchronized void drawOn(Graphics g, Boolean completeRepaintRequired)
	{
		new StaffPainter(this, g, 0, 0).draw(completeRepaintRequired);
	}

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject dict = new JSONObject()
			.put("staffConfig", this.getConfig().getJsonRepresentation());

		JSONArray accordList = new JSONArray();

		dict.put("tactList", getTactStream().stream().map(IModel::getJsonRepresentation).toArray());

		return dict;
	}

	public List<Tact> getTactStream()
	{
		// TODO: maybe move implementation into Tact or TactMeasurer
		List<Tact> result = new ArrayList<>();

		TactMeasurer measurer = new TactMeasurer(getConfig().getTactSize());

		Tact currentTact = new Tact(this);
		for (Accord accord: accordList) {
			currentTact.accordList.add(accord);
			if (measurer.inject(accord)) {
				result.add(currentTact);
				currentTact = new Tact(this);
			}
		}

		return result;
	}

	// TODO: model, mazafaka!
	@Override
	public Staff reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.clearStan();

		JSONArray accordJsonList = jsObject.getJSONArray("accordList");
		JSONObject configJson = jsObject.getJSONObject("staffConfig");
		this.getConfig().reconstructFromJson(configJson);

		for (int idx = 0; idx < accordJsonList.length(); ++idx) {
			JSONObject childJs = accordJsonList.getJSONObject(idx);
			if (childJs.has("tact")) { // small hack for great good
				toList(childJs.getJSONArray("accordList")).forEach(a -> addNewAccord().reconstructFromJson(a));
			} else {
				this.addNewAccord().reconstructFromJson(childJs);
			}
		}

		return this;
	}

	@Override
	public StaffHandler getHandler() { return (StaffHandler)super.getHandler(); }

	public Staff clearStan() {
		this.getAccordList().clear();
		this.focusedIndex = -1;

		return this;
	}

	@Override
	public Accord getFocusedChild() { return getFocusedAccord(); }
	@Override
	protected StaffHandler makeHandler() { return new StaffHandler(this); }

	// getters

	public Accord getFocusedAccord() {
		if (this.getFocusedIndex() > -1) {
			return getAccordList().get(getFocusedIndex());
		} else {
			return null;
		}
	}

	public List<Accord> getAccordList() {
		return this.accordList;
	}

	public List<List<Accord>> getAccordRowList() {
		List<List<Accord>> resultList = new ArrayList<>();
		for (int fromIdx = 0; fromIdx < this.getAccordList().size(); fromIdx += getAccordInRowCount()) {
			resultList.add(this.getAccordList().subList(fromIdx, Math.min(fromIdx + getAccordInRowCount(), this.getAccordList().size())));
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

		this.focusedIndex = limit(value, -1, getAccordList().size() - 1);
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
		for (int i = 0; i < getAccordList().size(); ++i) {

			if (curPos.equals(desiredPos)) {
				Accord accord = getAccordList().get(i);

				Fraction wasAccordLength = accord.getFraction();
				Nota newNota = accord.addNewNota(nota);

				if (!wasAccordLength.equals(accord.getFraction())) {
					// putting filler in case when accord length became smaller to preserve timing
					Fraction dl = wasAccordLength.subtract(accord.getFraction());
					this.add(new Accord(this), i + 1).addNewNota(0, 0).setLength(dl);
				}
				return newNota;
			} else if (curPos.compareTo(desiredPos) > 0) {

				Accord accord = getAccordList().get(i - 1);
				Fraction offset = new Fraction(curPos.doubleValue() - desiredPos.doubleValue());
				Fraction onset = new Fraction(accord.getFraction().doubleValue() - offset.doubleValue());

				/** @debug */
//				if (onset.equals(new Fraction(0))) {
//					Logger.fatal("How came ?! " + accord.getFraction() + " " + offset + " " + curPos + " " + desiredPos);
//				}

				accord.addNewNota(0, 0).setLength(onset);

				Accord newAccord = this.add(new Accord(this), i);
				Nota newNota = newAccord.addNewNota(nota);
				if (newNota.getLength().compareTo(offset) > 0) {
					// TODO: maybe if last accord in staff then no need
					// put nota with onset length into newNota's accord to preserve timing
					newAccord.addNewNota(0, 0).setLength(offset);
				} else if (newNota.getLength().compareTo(offset) < 0) {
					// TODO: maybe if last accord in staff then no need
					// put an empty nota after and set it's length(onset - newNota.getLength())
					this.add(new Accord(this), i + 1).addNewNota(0, 0).setLength(offset.subtract(newNota.getLength()));
				}

				return newNota;
			}

			Fraction accordFraction = getAccordList().get(i).getFraction();

			curPos = new Fraction(curPos.doubleValue() + accordFraction.doubleValue());
		}

		Fraction rest = desiredPos.subtract(curPos);
		if (!rest.equals(new Fraction(0))) {
			this.add(new Accord(this), getAccordList().size()).addNewNota(0,0).setLength(rest);
		}

		// TODO: we don't handle here pause prefix! (i.e when desired start is more than end) !!!
		// if not returned already
		return this.add(new Accord(this), getAccordList().size()).addNewNota(nota);
	}

	public static class TactMeasurer {

		final private Fraction tactSize;

		public Fraction sumFraction = new Fraction(0);

		public int tactCount = 0;

		public TactMeasurer(Fraction tactSize) {
			this.tactSize = tactSize;
		}

		/** @returns true if accord finished the tact */
		public Boolean inject(Accord accord) {
			if (INota.isDotable(accord.getFraction())) {
				sumFraction = sumFraction.add(accord.getFraction());
			} else {
				sumFraction = new Fraction(sumFraction.doubleValue() + accord.getFraction().doubleValue());
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

	private Stream<JSONObject> toList(JSONArray jsArray) throws JSONException
	{
		return IntStream.range(0, jsArray.length()).boxed().map(i -> jsArray.getJSONObject(i));
	}
}


