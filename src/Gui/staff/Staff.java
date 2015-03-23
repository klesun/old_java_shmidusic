// TODO: мэрджить ноты, типа целая+(половинная+четвёртая=половинная с точкой) = целая с двумя точками
// потому что делать точки плюсиком - это убого!

package Gui.staff;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Gui.SheetMusic;
import Midi.MidiCommon;
import Gui.staff.pointerable.Accord;
import Gui.staff.pointerable.Nota;
import Gui.staff.pointerable.Phantom;
import Midi.DeviceEbun;
import static Midi.DeviceEbun.MidiOutputDevice;
import static Midi.DeviceEbun.sintReceiver;
import Tools.FileProcessor;
import Tools.IModel;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Staff {
	public byte channelFlags = -1;
	int sessionId = (int)Math.random()*Integer.MAX_VALUE;
	
	public static final int CHANNEL = 0;
	public static final int DEFAULT_ZNAM = 64;
	public int cislic = 64;
	Nota unclosed[] = new Nota[256];
	final public static int ACCORD_EPSILON = 50; // in milliseconds
	
	public static int tempo = 120;
	public static int instrument = 0;
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
	
	public boolean bassKey, vioKey2, bassKey2;
	
	public int noshuCount = 0;
	
	public SheetMusic parentSheetMusic;
	public int to4kaOt4eta = 66; // какой позиции соответствует нижняя до скрипичного ключа
	
	public Phantom phantomka = null;
	public Accord firstDeleted = new Accord(this);
	public Accord lastDeleted = firstDeleted;

	private ArrayList<Accord> accordList = new ArrayList<>();
	private int pointerPos = 0;

	int closerCount = 0;
	
	public Staff(SheetMusic sheet){
		this.parentSheetMusic = sheet;
		
		bassKey = true;
		vioKey2 = false;
		bassKey2 = false;
		
		this.phantomka = new Phantom(this);
		Pointer.beginNota = phantomka;
		this.checkValues(this.phantomka);
		
		Pointer.init(this);
		
		mode = aMode.insert;
	}

	public Staff add(Accord elem) {

		// deprecated
		elem.prev = getFocusedIndex() - 1 >= 0 ? getAccordList().get(getFocusedIndex() - 1) : null;
		elem.next = getFocusedIndex() + 1 < getAccordList().size() ? getAccordList().get(getFocusedIndex() + 1) : null;
		Pointer.pointsAt.next = elem;
		if (elem.next != null) elem.next.prev = elem;
		++noshuCount;
		Pointer.move(0); // я был здесь, сказал пойнтер с крутым видом
		Pointer.move(1);
		
		// apprecated
		this.accordList.add(pointerPos++, elem);
		
		return this;
	}
	
	public void addPressed(int tune, int forca, int timestamp) {
		if (forca == 0) { // key up
			if (unclosed[tune] == null) return;
			Nota closer = new Nota().setTune(tune).setKeydownTimestamp(timestamp);
			--closerCount;
			unclosed[tune].length = (int)(closer.keydownTimestamp - unclosed[tune].keydownTimestamp);
		} else {
			if (mode == aMode.passive || mode == aMode.playin) {
				// Показать, какую ноту ты нажимаешь
				return;
			}
			
			Nota nota;

			if (getFocusedAccord() != null && (timestamp - getFocusedAccord().getEarliest().keydownTimestamp < ACCORD_EPSILON)) {
				nota = new Nota(getFocusedAccord()).setTune(tune).setKeydownTimestamp(timestamp);
			} else {
				Accord newAccord = new Accord(this);
				nota = new Nota(newAccord).setTune(tune).setKeydownTimestamp(timestamp);
				this.add(newAccord);
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
	
	public boolean delNotu(){
	    if (this.getFocusedAccord() == null) return Pointer.move(1);
	    Accord elem = (Accord)Pointer.pointsAt;
	    //nota.clearAccord();
	    if (elem.prev != null) {
	        Pointer.move(-1);
	        elem.prev.next = elem.next;
	        if (elem.next != null) elem.next.prev = elem.prev;
	    } else if (elem.next != null) {
	        Pointer.move(1);
	        elem.prev.next = elem.next;
	        if (elem.next != null) elem.next.prev = elem.prev;
	
	        --Pointer.pos;
	    } else {
	        Pointer.moveOut();
	    }
	    
	    elem.next = this.lastDeleted;
	    this.lastDeleted = elem;
	    --noshuCount;
	    ++deleted;
	    parentSheetMusic.repaint();
	    return true;
	}
	
	public int changeMode(){
	    if (mode == aMode.insert) mode = aMode.passive;
	    else mode = aMode.insert;
	
	    out(mode+"");
	    return 0;
	}
	
	public void clearStan() {
		while (delNotu());
	}
	
	private void out(String str) {
		System.out.println(str);
	}
	
	// TODO: stan should store current Phantom and these values should be getting from there directly, NO DENORMALIZING
	public void checkValues(Phantom rak) {
	    int cislic = rak.numerator, znamen=rak.znamen, tempo = rak.valueTempo; double volume = rak.valueVolume; int instrument = rak.valueInstrument;
	    if (cislic!=this.cislic || znamen!=DEFAULT_ZNAM || tempo!=Staff.tempo || volume!=this.volume) {
	        // Всё равно ж перерисовывать надо будет
	        int k = DEFAULT_ZNAM / znamen;
	        this.cislic = cislic*k;
	        this.tempo = tempo;
	        this.volume = volume;
			setInstrument(instrument);
	    }
	}
	
	private void setInstrument(int instrument) {
		if (instrument != this.instrument) {
			try {
				DeviceEbun.changeInstrument(instrument);
				this.instrument = instrument;
			} catch (InvalidMidiDataException e) { System.out.println("Сука инструмент менять нихуя не получается"); }
		}
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
	
	public Dictionary<String, Object> getExternalRepresentation() {
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("childList", this.getChildList().stream().map(p -> p.getObjectState()).toArray());
		
		return dict;
	}
	
	public int reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.clearStan();
		JSONArray childJsonList = jsObject.getJSONArray("childList");
		this.getPhantom().update(new Phantom(this).setObjectStateFromJson(childJsonList.getJSONObject(0))); // TODO: it is so lame, but i spent hours to save all these files in this format
		for (int idx = 1; idx < childJsonList.length(); ++idx) { // TODO: store Phantom as dict field, not list value
			JSONObject childJs = childJsonList.getJSONObject(idx);
			this.add(new Accord(this).setObjectStateFromJson(childJs));
		}
		return 0;
	}

	// getters

	private Pointer getPointer() {
		return Pointer.getInstance();
	}

	public Accord getFocusedAccord() {
		ArrayList<Accord> accordList = this.getAccordList();
		if (this.getFocusedIndex() != -1 && accordList.size() > this.getPointer().getPos()) {
			return (Accord)accordList.get(this.getPointer().getPos());
		} else {
			return null;
		}
	}

	public ArrayList<Accord> getAccordList() {
		ArrayList<Accord> list = new ArrayList<>();
		return this.getAccordList();
	}
	
	public ArrayList<IModel> getChildList() {
		ArrayList childList = this.getAccordList();
		childList.add(0, this.getPhantom());
		return childList;
	}

	// field getters/setters

	public Phantom getPhantom() {
		return this.phantomka;
	}

	public int getFocusedIndex() {
		return this.getPointer().getPos();
	}
}


