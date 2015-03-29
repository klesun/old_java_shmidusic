// TODO: мэрджить ноты, типа целая+(половинная+четвёртая=половинная с точкой) = целая с двумя точками
// потому что делать точки плюсиком - это убого!

package Model;

import Model.StaffConfig.StaffConfig;
import Gui.Settings;
import java.util.ArrayList;
import java.util.List;

import Gui.SheetMusic;
import Model.Accord.Accord;
import Model.Accord.Nota.Nota;
import Musica.PlayMusThread;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Staff implements IModel {
	public byte channelFlags = -1;
	int sessionId = (int)(Math.random() * Integer.MAX_VALUE);
	
	public static final int CHANNEL = 0;
	public static final int DEFAULT_ZNAM = 64; // TODO: move it into some constants maybe
	public int cislic = 64;
	Nota unclosed[] = new Nota[256];
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
	
	public SheetMusic parentSheetMusic;
	
	public StaffConfig phantomka = null;
	public Accord firstDeleted = new Accord(this);
	public Accord lastDeleted = firstDeleted;

	private ArrayList<Accord> accordList = new ArrayList<>();
	public int focusedIndex = -1;

	int closerCount = 0;
	
	public Staff(SheetMusic sheet){
		this.parentSheetMusic = sheet;
		
		this.phantomka = new StaffConfig(this);
				
		mode = aMode.insert;
	}

	public Staff add(Accord elem) {
		this.accordList.add(++focusedIndex, elem);
		return this;
	}
	
	public void addPressed(int tune, int forca, int timestamp) {
		if (forca == 0) { // key up
			if (unclosed[tune] == null) return;
			Nota closer = new Nota(null).setTune(tune).setKeydownTimestamp(timestamp);
			--closerCount;
			unclosed[tune].length = closer.keydownTimestamp - unclosed[tune].keydownTimestamp;
		} else {
			if (mode == aMode.passive || mode == aMode.playin) {
				// Показать, какую ноту ты нажимаешь
				return;
			}
			
			Nota nota;

			if (getFocusedAccord() != null && (timestamp - getFocusedAccord().getEarliest().keydownTimestamp < ACCORD_EPSILON)) {
				getFocusedAccord().add(nota = new Nota(getFocusedAccord()).setTune(tune).setKeydownTimestamp(timestamp));
			} else {
				Accord newAccord = new Accord(this);
				this.add(newAccord.add(nota = new Nota(newAccord).setTune(tune).setKeydownTimestamp(timestamp)));
			}
			
			unclosed[tune] = nota;
			++closerCount;
		}
	}
	
	int deleted = 0;
	
	// TODO: maybe repair the ctrl+z one day (rewrite it completely pls)
//	public void retrieveLast(){
//		if (deleted == 0) return;
//		Accord cur = lastDeleted;
//		lastDeleted = lastDeleted.next;
//		cur.next = cur.prev.next;
//		cur.prev.next = cur;
//		if (cur.next != null) cur.next.prev = cur;
//		cur.retrieve = lastRetrieved;
//		lastRetrieved = cur;
//		++noshuCount;
//		--deleted;
//		if ( Pointer.isAfter(cur) ) ++Pointer.pos;
//	}
//	Accord lastRetrieved = null;
//	
//	public void detrieveNotu(){ 
//		int rez = -1;
//		Accord accord = lastRetrieved;
//		if (accord == null) return;
//		lastRetrieved = accord.retrieve;    	
//		if (Pointer.pointsAt == accord) {
//			delNotu();
//			return;
//		} else rez = Pointer.moveTo(accord);
//		if (rez == 0) delNotu();
//		else {
//			out("Воскресшей ноты на стане нет");
//		}
//	}

	public void drawOn(Graphics g, int baseX, int baseY) {

		int taktCount = 1;
		int curCislic = 0;

		drawPhantom(getPhantom(), g, baseX, baseY);
		baseX += dx();

		int i = 0;
		for (List<Accord> row: getAccordRowList()) {
			int y = baseY + i * SheetMusic.SISDISPLACE * dy(); // bottommest y nota may be drawn on
			g.drawImage(getViolinKeyImage(), this.dx(), y -3 * dy(), parentSheetMusic);
			g.drawImage(getBassKeyImage(), this.dx(), 11 * dy() + y, parentSheetMusic);
			g.setColor(Color.BLUE);
			for (int j = 0; j < 11; ++j){
				if (j == 5) continue;
				g.drawLine(parentSheetMusic.getMarginX(), y + j* this.dy() *2, getWidth() - parentSheetMusic.getMarginX()*2, y + j* dy() *2);
			}

			int j = 0;
			for (Accord accord: row) {
				int x = baseX + j * (2 * dx());
				if (getFocusedAccord() == accord) { 
					g.drawImage(getPointerImage(), x + dx(), y - this.dy() *14, parentSheetMusic); 
				}

				if (accord.getNotaList().size() > 0) {

					curCislic += accord.getShortest().getNumerator();	
					if (curCislic >= getPhantom().numerator * 8) { // потому что у нас шажок 1/8 когда меняем размер такта
						curCislic %= getPhantom().numerator * 8;
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
	
	// TODO: move into StaffConfig class... some day
	private void drawPhantom(StaffConfig phantomka, Graphics g, int xIndent, int yIndent) {
		int dX = parentSheetMusic.getNotaWidth()/5, dY = parentSheetMusic.getNotaHeight()*2;
		g.drawImage(phantomka.getImage(), xIndent - dX, yIndent - dY, parentSheetMusic);
		int deltaY = 0, deltaX = 0;
		switch (phantomka.changeMe) {
			case numerator:	deltaY += 9 * this.dy(); break;
			case tempo: deltaY -= 1 * this.dy(); break;
			case instrument: deltaY += 4 * this.dy(); deltaX += this.dx() / 4; break;
			case volume: deltaY += 24 * this.dy(); break;
			default: break;
		}
		if (phantomka.getParentStaff().getFocusedAccord() == null) {
			g.drawImage(getPointerImage(), xIndent - 7*parentSheetMusic.getNotaWidth()/25 + deltaX, yIndent - this.dy() * 14 + deltaY, parentSheetMusic);	
		}
	}
	
	public int changeMode(){
	    if (mode == aMode.insert) mode = aMode.passive;
	    else mode = aMode.insert;
	
	    out(mode+"");
	    return 0;
	}
	
	public void clearStan() {
		this.getAccordList().clear();
		this.setFocusedIndex(-1);
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
	public LinkedHashMap<String, Object> getJsonRepresentation() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<>();
		dict.put("staffConfig", this.getPhantom().getJsonRepresentation());
		dict.put("accordList", this.getAccordList().stream().map(p -> p.getJsonRepresentation()).toArray());
		
		return dict;
	}
	
	@Override
	public Staff reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.clearStan();
		JSONArray accordJsonList;
		if (jsObject.has("childList")) { // TODO: deprecated. Run some script on all files, i won't manually resave all of them
			accordJsonList = jsObject.getJSONArray("childList");
			this.getPhantom().update(new StaffConfig(this).reconstructFromJson(accordJsonList.getJSONObject(0))); // TODO: it is so lame, but i spent hours to save all these files in this format
			accordJsonList.remove(0);
		} else {
			accordJsonList = jsObject.getJSONArray("accordList");
			JSONObject configJson = jsObject.getJSONObject("staffConfig");
			this.getPhantom().update(new StaffConfig(this).reconstructFromJson(configJson));
		}
		for (int idx = 0; idx < accordJsonList.length(); ++idx) {
			JSONObject childJs = accordJsonList.getJSONObject(idx);
			this.add(new Accord(this).reconstructFromJson(childJs));
		}
		
		return this;
	}

	public Boolean moveFocus(int n) {
		Boolean stop = getFocusedIndex() + n < -1 || getFocusedIndex() + n > getAccordList().size() - 1;
		setFocusedIndex(getFocusedIndex() + n);

		if (getFocusedAccord() != null && !stop) {
			PlayMusThread.playAccord(getFocusedAccord());
		}
		return !stop;
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
		for (int fromIdx = 0; fromIdx < this.getAccordList().size(); fromIdx += getNotaInRowCount()) {
			resultList.add(this.getAccordList().subList(fromIdx, Math.min(fromIdx + getNotaInRowCount(), this.getAccordList().size())));
		}

		if (resultList.isEmpty()) { resultList.add(new ArrayList<>()); }		
		return resultList;
	}

	public int getWidth() {
		return parentSheetMusic.getWidth() - parentSheetMusic.MARGIN_H * 2;
	}

	public int getNotaInRowCount() {
		return this.getWidth() / (Settings.getNotaWidth() * 2) - 3; // - 3 because violin key and phantom
	}

	public int dx() {
		return Settings.getStepWidth();
	}

	public int dy() {
		return Settings.getStepHeight();
	}

	public BufferedImage getViolinKeyImage() {
		return SheetMusic.vseKartinki[0];
	}

	public BufferedImage getBassKeyImage() {
		return SheetMusic.vseKartinki[1];
	}

	public BufferedImage getPointerImage() {
		return SheetMusic.vseKartinki[3];
	}

	// field getters/setters

	public StaffConfig getPhantom() {
		return this.phantomka;
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


