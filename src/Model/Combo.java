package Model;

import Stuff.Tools.Logger;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javafx.scene.input.KeyCode;

import java.awt.event.KeyEvent;
import java.util.*;

public class Combo {

	public int mod = 0; // public for debug
	private int keyCode = 0;
	private char keychar = 'z';

	public Combo(KeyEvent keyEvent) {
		this.mod = keyEvent.getModifiers();
		this.keyCode = keyEvent.getKeyCode();
		this.keychar = keyEvent.getKeyChar();
	}

	public Combo(int mod, int keyCode) {
		this.mod = mod;
		this.keyCode = keyCode;
	}

	public static Combo makeFake() {
		return new Combo(0,0);
	}

	public Combo changeSign() {
		return new Combo(this.mod, anti(this.keyCode));
	}

	public int getSign() {
		if (getAntiKeyMap().containsKey(getKeyCode())) {
			return -1;
		} else if (getAntiKeyMap().inverse().containsKey(getKeyCode())) {
			return +1;
		} else {
			return 0;
		}
	}

	public int getPressedNumber() {
		return (getKeyCode() >= '0' && getKeyCode() <= '9') ? getKeyCode() - '0' : getKeyCode() - KeyEvent.VK_NUMPAD0;
	}

	public int asciiToTune() {
		return getAsciTuneMap().get(getKeyCode());
	}

	// static - public

	public static List<Integer> getNumberKeyList() {
		List<Integer> keyList = new ArrayList<>();
		for (Integer i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { keyList.add(i); }
		for (Integer i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; ++i) { keyList.add(i); }
		return keyList;
	}

	public static List<Integer> getCharacterKeycodeList() {
		List<Integer> keyList = new ArrayList<>();
		for (Integer i = KeyEvent.VK_COMMA; i <= KeyEvent.VK_DIVIDE; ++i) { keyList.add(i); }
		keyList.addAll(Arrays.asList(KeyEvent.VK_SPACE, KeyEvent.VK_BACK_QUOTE));
		keyList.removeAll(getNumberKeyList());
		return keyList;
	}

	public static Integer tuneToAscii(Integer tune) {
		return getAsciTuneMap().inverse().get(tune);
	}

	// static - private

	private static int anti(int keyCode) {
		if (getAntiKeyMap().containsKey(keyCode)) {
			return getAntiKeyMap().get(keyCode);
		} else if (getAntiKeyMap().inverse().containsKey(keyCode)) {
			return getAntiKeyMap().inverse().get(keyCode);
		} else {
			Logger.fatal("Жопа!!! Этот метод не должен вызываться с параметром " + keyCode);
			System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
			return keyCode;
		}
	}

	// field getters

	public char getKeyChar() {
		return this.keychar;
	}
	public int getKeyCode() {
		return this.keyCode;
	}

	// overrides

	@Override
	public String toString() {
		return "" + this.mod + " " + this.keyCode;
	}

	@Override
	public int hashCode() {
		// mod - 4 bits, keycode - 8 in generic cases
		return (this.keyCode << 4) + this.mod;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == this.getClass() &&
			this.hashCode() == obj.hashCode();
	}

	// 100500-line properties

	private static BiMap<Integer, Integer> getAntiKeyMap() {
		BiMap<Integer, Integer> antiKeyMap = HashBiMap.create();
		antiKeyMap.put(KeyEvent.VK_UP, KeyEvent.VK_DOWN);
		antiKeyMap.put(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
		antiKeyMap.put(KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS);
		antiKeyMap.put(KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET);
		antiKeyMap.put(KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD);
		antiKeyMap.put(KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN);
		return antiKeyMap;
	}

	public static BiMap<Integer, Integer> getAsciTuneMap() {
		int[][] keyboardArrangement = {
			// bottommest row
			{ KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V, KeyEvent.VK_B, KeyEvent.VK_N, KeyEvent.VK_M,
				KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH, KeyEvent.VK_BACK_SLASH, KeyEvent.VK_BACK_SPACE, },
			// middle row
			{ KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_J,
				KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_SEMICOLON, KeyEvent.VK_QUOTEDBL, KeyEvent.VK_EQUALS, },
			// top row
			{ KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R, KeyEvent.VK_T, KeyEvent.VK_Y, KeyEvent.VK_U,
				KeyEvent.VK_I, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, },
			// number row
			{ KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5,
				KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0, KeyEvent.VK_MINUS, },
		};
		BiMap<Integer, Integer> map = HashBiMap.create();
		int doOfSmallOctava = 48; // -до- малой октавы
		for (int i = 0; i < keyboardArrangement.length; ++i) {
			for (int j = 0; j < 12; ++j) {
				map.put(keyboardArrangement[i][j], doOfSmallOctava + 12 * i + j);
			}
		}
		for (Integer i = 32; i < 128; ++i) {
			if (!map.containsValue(i)) {
				map.put(256 + i, i); // setting not existing (on generic keyboard) key
				// TODO: not parsed back correctly
			}
		}
		return map;
	}

}
