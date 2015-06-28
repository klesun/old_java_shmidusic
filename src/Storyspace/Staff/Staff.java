// TODO: мэрджить ноты, типа целая+(половинная+четвёртая=половинная с точкой) = целая с двумя точками
// потому что делать точки плюсиком - это убого!

package Storyspace.Staff;

import Gui.ImageStorage;
import Main.MajesticWindow;
import Model.AbstractModel;
import Model.ActionResult;
import Model.Combo;
import Model.SimpleAction;
import Storyspace.Staff.Accord.AccordHandler;
import Storyspace.Staff.StaffConfig.StaffConfig;
import Gui.Settings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Storyspace.Staff.Accord.Accord;
import Stuff.Midi.DeviceEbun;
import Stuff.Musica.PlayMusThread;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import Stuff.OverridingDefaultClasses.TruHashMap;
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

	private ArrayList<Accord> accordList = new ArrayList<>();
	public int focusedIndex = -1;

	public StaffPanel blockPanel = null;

	private Boolean surfaceChanged = true;

	public Staff(StaffPanel blockPanel) {
		super(null);
		this.blockPanel = blockPanel;
		this.staffConfig = new StaffConfig(this);
	}

	public Accord addNewAccord() {
		Accord accord = new Accord(this);
		if (DeviceEbun.isPlaybackSoftware()) { // i.e. when playback is not done with piano - no need to play pressed accord, user hears it anyways
			new Thread(() -> { Uninterruptibles.sleepUninterruptibly(AccordHandler.ACCORD_EPSILON, TimeUnit.MILLISECONDS); PlayMusThread.playAccord(accord); }).start();
		}
		this.add(accord, getFocusedIndex() + 1);
		this.moveFocus(1);
		return accord;
	}

	private synchronized Accord add(Accord accord, int index) {
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> getAccordList().add(index, accord))
			.setUndo(() -> getAccordList().remove(accord)));
		return accord;
	}

	public synchronized void remove(Accord accord) {
		int index = getAccordList().indexOf(accord);
		if (index <= getFocusedIndex()) { setFocusedIndex(getFocusedIndex() - 1); }
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> getAccordList().remove(accord))
			.setUndo(() -> getAccordList().add(index, accord)));
	}

	// TODO: move into some StaffPainter class
	public synchronized void drawOn(Graphics g, @Deprecated Boolean completeRepaintRequired) {

		int baseX = getMarginX() + 2 * dx();  // 2вч - violin/bass keys
		int baseY = getMarginY(); // highest line y

		TactMeasurer tactMeasurer = new TactMeasurer(this);

		getConfig().drawOn(g, baseX, baseY);
		baseX += dx();

		int i = 0;
		for (List<Accord> row : getAccordRowList()) {
			int y = baseY + i * SISDISPLACE * dy(); // bottommest y nota may be drawn on

			if (completeRepaintRequired) {
				g.drawImage(ImageStorage.inst().getViolinKeyImage(), this.dx(), y - 3 * dy(), null);
				g.drawImage(ImageStorage.inst().getBassKeyImage(), this.dx(), 11 * dy() + y, null);
			}

			int j = 0;
			for (Accord accord : row) {
				int x = baseX + j * (2 * dx());

				accord.drawOn(g, x, y - 12 * dy(), completeRepaintRequired);
				if (tactMeasurer.inject(accord)) {
					g.setColor(tactMeasurer.sumFraction.equals(new Fraction(0)) ? Color.BLACK : new Color(255, 63, 0)); // reddish orange
					g.drawLine(x + dx() * 2, y - dy() * 5, x + dx() * 2, y + dy() * 20);
					g.setColor(Color.decode("0x00A13E"));
					g.drawString(tactMeasurer.tactCount + "", x + dx() * 2, y - dy() * 6);
				}

				++j;
			}

			g.setColor(Color.BLUE);
			for (j = 0; j < 11; ++j) {
				if (j == 5) continue;
				g.drawLine(getMarginX(), y + j * dy() * 2, getWidth() - getMarginX() * 2, y + j * dy() * 2);
			}

			++i;
		}
	}

	// i hope i wont need this
	// TODO: move into some StaffPainter class
//	public void repaintPointer(int oldPos) {
//		int baseX = getMarginX() + 2 * dx() + dx();
//		int baseY = getMarginY();
//
//		int w = ImageStorage.inst().getPointerImage().getWidth();
//		int h = ImageStorage.inst().getPointerImage().getHeight();
//
//		int oldX = baseX + (oldPos % getAccordInRowCount()) * (2 * dx()) + dx();
//		int oldY = baseY + (oldPos / getAccordInRowCount()) * SISDISPLACE * dy() - this.dy() * 14;
//
//		int newX = baseX + (getFocusedIndex() % getAccordInRowCount()) * (2 * dx()) + dx();
//		int newY = baseY + (getFocusedIndex() / getAccordInRowCount()) * SISDISPLACE * dy() - this.dy() * 14;
//
//		getParentSheet().repaint(0, oldX, oldY, w, h);
//		getParentSheet().repaint(0, newX, newY, w, h);
//	}

	@Override
	public void getJsonRepresentation(JSONObject dict) {
		dict.put("staffConfig", this.getConfig().getJsonRepresentation());
		dict.put("accordList", new JSONArray(this.getAccordList().stream().map(p -> p.getJsonRepresentation()).toArray()));
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
			this.addNewAccord().reconstructFromJson(childJs);
			this.moveFocus(1); // TODO: maybe not ?
		}

		return this;
	}

	@Override
	public StaffHandler getHandler() { return (StaffHandler)super.getHandler(); }

	private void clearStan() {
		this.getAccordList().clear();
		this.focusedIndex = -1;
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
		return getAccordRowList().size() * SISDISPLACE * dy();
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
		int result = this.getWidth() / (Settings.getNotaWidth() * 2) - 3; // - 3 because violin key and phantom
		return Math.max(result, 1);
	}

	// field getters/setters

	public StaffConfig getConfig() {
		return this.staffConfig;
	}
	public StaffPanel getParentSheet() { // ???
		return this.blockPanel;
	}
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

	public void changeMode(Combo combo) {
		mode = combo.getPressedNumber() > 0 ? aMode.insert : aMode.passive;
	}

	public ActionResult moveFocusWithPlayback(int sign, Boolean interruptSounding) {
		ActionResult result = moveFocus(sign);
		if (getFocusedAccord() != null && result.isSuccess()) {
			if (interruptSounding) { PlayMusThread.shutTheFuckUp(); }
			PlayMusThread.playAccord(getFocusedAccord());
		}
		return result;
	}

	public ActionResult moveFocusWithPlayback(int sign) {
		return moveFocusWithPlayback(sign, true);
	}

	public ActionResult moveFocusRow(int sign) {
		int n = sign * getAccordInRowCount();
		return moveFocusWithPlayback(n);
	}

	public ActionResult moveFocus(int n)
	{
		Boolean stop = getFocusedIndex() + n < -1 || getFocusedIndex() + n > getAccordList().size() - 1;
		setFocusedIndex(getFocusedIndex() + n);

		return !stop ? new ActionResult(true) : new ActionResult("dead end");
	}

	public void triggerPlayer()
	{
		if (PlayMusThread.stop) {
			PlayMusThread.shutTheFuckUp();
			PlayMusThread.stop = false;
			(new PlayMusThread(this)).start();
		} else {
			PlayMusThread.stopMusic();
		}
	}
}


