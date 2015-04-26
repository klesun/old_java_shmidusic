// TODO: мэрджить ноты, типа целая+(половинная+четвёртая=половинная с точкой) = целая с двумя точками
// потому что делать точки плюсиком - это убого!

package Model.Staff;

import Gui.SheetPanel;
import Model.AbstractModel;
import Model.Combo;
import Model.Staff.StaffConfig.StaffConfig;
import Gui.Settings;
import java.util.ArrayList;
import java.util.List;

import Model.Staff.Accord.Accord;
import Musica.PlayMusThread;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Staff extends AbstractModel {
	public byte channelFlags = -1;

	public static final int CHANNEL = 0;
	public static final int DEFAULT_ZNAM = 64; // TODO: move it into some constants maybe
	final public static int ACCORD_EPSILON = 50; // in milliseconds
	
	public static double volume = 0.5;
	
	public enum aMode {
	    append,
	    rewrite,
	    insert,
	    playin,
	    passive,
	    
	    tictacin; // Написать
	    // идея вообще такая: тиктакает метроном, а ты нажимаешь аккорды
	} 
	public aMode mode;

	public StaffConfig phantomka = null;

	private ArrayList<Accord> accordList = new ArrayList<>();
	public int focusedIndex = -1;

	public Staff(SheetPanel sheet){
		super(sheet);
		this.phantomka = new StaffConfig(this);
		mode = aMode.insert;
	}

	public synchronized Staff add(Accord elem) {
		this.accordList.add(getFocusedIndex() + 1, elem);
		return this;
	}

	public synchronized void drawOn(Graphics g, int baseX, int baseY) {

		int taktCount = 1;
		int curCislic = 0;

		getConfig().drawOn(g, baseX, baseY);
		baseX += dx();

		int i = 0;
		for (List<Accord> row: getAccordRowList()) {
			int y = baseY + i * SheetPanel.SISDISPLACE * dy(); // bottommest y nota may be drawn on
			g.drawImage(getViolinKeyImage(), this.dx(), y -3 * dy(), getParentSheet());
			g.drawImage(getBassKeyImage(), this.dx(), 11 * dy() + y, getParentSheet());
			g.setColor(Color.BLUE);
			for (int j = 0; j < 11; ++j){
				if (j == 5) continue;
				g.drawLine(getParentSheet().getMarginX(), y + j* this.dy() *2, getWidth() - getParentSheet().getMarginX()*2, y + j* dy() *2);
			}

			int j = 0;
			for (Accord accord: row) {
				int x = baseX + j * (2 * dx());
				if (getFocusedAccord() == accord) { 
					g.drawImage(getPointerImage(), x + dx(), y - this.dy() *14, getParentSheet());
				}

				if (accord.getNotaList().size() > 0) {

					curCislic += accord.getShortestNumerator(); // TODO: triols are counted as each was complete nota, bad
					if (curCislic >= getConfig().numerator * 8) { // потому что у нас шажок 1/8 когда меняем размер такта
						curCislic %= getConfig().numerator * 8;
						g.setColor(curCislic > 0 ? Color.BLUE : Color.BLACK);
						g.drawLine(x + dx() * 2, y - dy() * 5, x + dx() * 2, y + dy() * 20);
						g.setColor(Color.decode("0x00A13E"));
						g.drawString(taktCount + "", x + dx() * 2, y - dy() * 6);

						++taktCount;
					}

					accord.drawOn(g, x, y - 12 * dy());
				}
				++j;
			}
			++i;
		}
	}
	
	public int changeMode(){
	    if (mode == aMode.insert) mode = aMode.passive;
	    else mode = aMode.insert;
	
	    out(mode + "");
	    return 0;
	}
	
	public void clearStan() {
		this.getAccordList().clear();
		this.focusedIndex = -1;
	}
	
	private void out(String str) {
		System.out.println(str);
	}
	
	public int changeChannelFlag(int channel) {
		if (channel > 7 || channel < 0) return -1;
		channelFlags ^= 1 << channel;
		return 0;
	}
	
	public Boolean getChannelFlag(int channel) {
		if (channel > 7 || channel < 0) return false;
		return (channelFlags & (1 << channel)) > 0;
	}

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject dict = new JSONObject();
		dict.put("staffConfig", this.getConfig().getJsonRepresentation());
		dict.put("accordList", new JSONArray(this.getAccordList().stream().map(p -> p.getJsonRepresentation()).toArray()));
		
		return dict;
	}
	
	@Override
	public Staff reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.clearStan();
		JSONArray accordJsonList;
		if (jsObject.has("childList")) { // TODO: deprecated. Run some script on all files, i won't manually resave all of them... again
			accordJsonList = jsObject.getJSONArray("childList");
			this.getConfig().reconstructFromJson(accordJsonList.getJSONObject(0)); // TODO: it is so lame, but i spent hours to save all these files in this format
			accordJsonList.remove(0);
		} else {
			accordJsonList = jsObject.getJSONArray("accordList");
			JSONObject configJson = jsObject.getJSONObject("staffConfig");
			this.getConfig().reconstructFromJson(configJson);
		}
		for (int idx = 0; idx < accordJsonList.length(); ++idx) {
			JSONObject childJs = accordJsonList.getJSONObject(idx);
			this.add(new Accord(this).reconstructFromJson(childJs)).moveFocus(1);
		}
		
		return this;
	}

	@Override
	public synchronized List<? extends AbstractModel> getChildList() {
		List childList = (List<Accord>)getAccordList().clone();
		childList.add(0, getConfig());
		return childList;
	}

	@Override
	public AbstractModel getFocusedChild() {
		return getFocusedAccord() != null ? getFocusedAccord() : getConfig();
	}

	@Override
	protected StaffHandler makeHandler() {
		return new StaffHandler(this);
	}

	public Boolean moveFocus(int n) {
		Boolean stop = getFocusedIndex() + n < -1 || getFocusedIndex() + n > getAccordList().size() - 1;
		setFocusedIndex(getFocusedIndex() + n);

		return !stop;
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

	public int getWidth() {
		return getParentSheet().getWidth() - getParentSheet().MARGIN_H * 2;
	}

	public int getAccordInRowCount() {
		return this.getWidth() / (Settings.getNotaWidth() * 2) - 3; // - 3 because violin key and phantom
	}

	public int dx() {
		return Settings.getStepWidth();
	}

	public int dy() {
		return Settings.getStepHeight();
	}

	public BufferedImage getViolinKeyImage() {
		return SheetPanel.vseKartinkiSized[0];
	}

	public BufferedImage getBassKeyImage() {
		return SheetPanel.vseKartinkiSized[1];
	}

	public BufferedImage getPointerImage() {
		return SheetPanel.vseKartinkiSized[3];
	}

	// field getters/setters

	public StaffConfig getConfig() {
		return this.phantomka;
	}

	public SheetPanel getParentSheet() {
		return (SheetPanel)getParent();
	}

	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public Staff setFocusedIndex(int value) {
		if (this.getFocusedAccord() != null) {
			this.getFocusedAccord().setFocusedIndex(-1);
		}
		value = value < -1 ? -1 : value;
		value = value >= getAccordList().size() ? getAccordList().size() - 1 : value;
		this.focusedIndex = value;
		return this;
	}
}


