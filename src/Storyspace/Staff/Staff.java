// TODO: мэрджить ноты, типа целая+(половинная+четвёртая=половинная с точкой) = целая с двумя точками
// потому что делать точки плюсиком - это убого!

package Storyspace.Staff;

import Gui.ImageStorage;
import Main.MajesticWindow;
import Model.AbstractModel;
import Model.Combo;
import Storyspace.Staff.StaffConfig.StaffConfig;
import Gui.Settings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Storyspace.Staff.Accord.Accord;
import Stuff.Musica.PlayMusThread;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;


import Stuff.OverridingDefaultClasses.TruHashMap;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Staff extends MidianaComponent {

	final public static int SISDISPLACE = 40;
	public static final int DEFAULT_ZNAM = 64; // TODO: move it into some constants maybe
	final public static int ACCORD_EPSILON = 50; // in milliseconds

	public enum aMode { insert, passive }
	public static aMode mode = aMode.insert;

	public StaffConfig staffConfig = null;

	private ArrayList<Accord> accordList = new ArrayList<>();
	public int focusedIndex = -1;

	private MajesticWindow parentWindow = null;
	public StaffPanel blockPanel = null;

	public Staff(StaffPanel blockPanel) {
		super(null);
		this.parentWindow = blockPanel.parentWindow;
		this.blockPanel = blockPanel;
		this.staffConfig = new StaffConfig(this);
	}

	public synchronized Staff add(Accord elem) {
		this.accordList.add(getFocusedIndex() + 1, elem);
		return this;
	}

	public Accord addNewAccord() {
		Accord accord = new Accord(this);
		this.add(accord);
		return accord;
	}

	// TODO: move into some StaffPainter class
	public synchronized void drawOn(Graphics g, int baseX, int baseY) { // baseY - highest line y

		baseX += getMarginX();
		baseY += getMarginY();

		baseX += 2 * dx(); // violin/bass keys

		TactMeasurer tactMeasurer = new TactMeasurer(this);

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		getConfig().drawOn(g, baseX, baseY);
		baseX += dx();

		int i = 0;
		for (List<Accord> row: getAccordRowList()) {
			int y = baseY + i * SISDISPLACE * dy(); // bottommest y nota may be drawn on
			g.drawImage(ImageStorage.inst().getViolinKeyImage(), this.dx(), y -3 * dy(), null);
			g.drawImage(ImageStorage.inst().getBassKeyImage(), this.dx(), 11 * dy() + y, null);
			g.setColor(Color.BLUE);
			for (int j = 0; j < 11; ++j) {
				if (j == 5) continue;
				g.drawLine(getMarginX(), y + j * dy() *2, getWidth() - getMarginX() * 2, y + j * dy() *2);
			}

			int j = 0;
			for (Accord accord: row) {
				int x = baseX + j * (2 * dx());
				if (getFocusedAccord() == accord) {
					g.drawImage(ImageStorage.inst().getPointerImage(), x + dx(), y - this.dy() * 14, getParentSheet());
				}

				if (tactMeasurer.inject(accord)) {
					g.setColor(tactMeasurer.sumFraction.equals(new Fraction(0)) ? Color.BLACK : new Color(255, 63, 0)); // reddish orange
					g.drawLine(x + dx() * 2, y - dy() * 5, x + dx() * 2, y + dy() * 20);
					g.setColor(Color.decode("0x00A13E"));
					g.drawString(tactMeasurer.tactCount + "", x + dx() * 2, y - dy() * 6);
				}

				accord.drawOn(g, x, y - 12 * dy());

				++j;
			}

			if (getFocusedIndex() == -1) {
				g.drawImage(ImageStorage.inst().getPointerImage(), baseX, y - this.dy() * 14, getParentSheet());
			}

			++i;
		}
	}

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
			this.add((Accord)new Accord(this).reconstructFromJson(childJs)).moveFocus(1);
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

	public ArrayList<Accord> getAccordList() {
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
		if (this.getFocusedAccord() != null) { this.getFocusedAccord().setFocusedIndex(-1); }
		this.focusedIndex = limit(value, -1, getAccordList().size() - 1);

		return this;
	}

	// action handles

	public void changeMode(Combo combo) {
		mode = combo.getPressedNumber() > 0 ? aMode.insert : aMode.passive;
	}

	public Boolean moveFocusUsingCombo(Combo combo) {
		Boolean result = moveFocus(combo.getSign());
		if (getFocusedAccord() != null && result) {
			PlayMusThread.playAccord(getFocusedAccord());
		}
		return result;
	}

	public Boolean moveFocusRow(Combo combo) {
		int n = combo.getSign() * getAccordInRowCount();
		moveFocus(n);
		return true;
	}

	public Boolean moveFocus(int n)
	{
		Boolean stop = getFocusedIndex() + n < -1 || getFocusedIndex() + n > getAccordList().size() - 1;
		setFocusedIndex(getFocusedIndex() + n);

		return !stop;
	}

	public void triggerPlayer(Combo combo)
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


