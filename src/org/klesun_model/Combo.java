package org.klesun_model;

import org.shmidusic.PianoLayoutPanel;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Combo
{
	final public static KeyEvent K = new KeyEvent(new JPanel(),0,0,0,0,'h'); // just for constants

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

	public int getPressedNumber()
	{
		return getPressedNumber(getKeyCode()).dieIfFailure();
	}

	public static Explain<Integer> getPressedNumber(Integer keyCode)
	{
		List<Function<Integer, Explain<Integer>>> preds = Arrays.asList(
			Explain.mkPred(k -> k >= '0' && k <= '9', k -> k - '0'),
			Explain.mkPred(k -> k >= K.VK_NUMPAD0 && k <= K.VK_NUMPAD9, k -> k - K.VK_NUMPAD0),
			Explain.mkPred(k -> k >= K.VK_F10 && k <= K.VK_F12, k -> k - K.VK_F1 + 1)
		);

		return preds.stream().anyMatch(pr -> pr.apply(keyCode).isSuccess())
				? preds.stream().map(pr -> pr.apply(keyCode)).filter(e -> e.isSuccess()).findAny().get()
				: new Explain<>(false, "No Number With Such Keycode! " + keyCode);
	}

	public int asciiToTune() {
		return getAsciTuneMap().get(getKeyCode());
	}

	public static int getAsciiTuneMods() {
		return KeyEvent.ALT_MASK;
	}

	// static - public

	public static IntStream getNumberKeys()
	{
		return IntStream.range(1, 255).filter(k -> getPressedNumber(k).isSuccess());
	}

	public static List<Combo> getNumberComboList(int mod) {
		return getNumberKeys().boxed().map(num -> new Combo(mod, num)).collect(Collectors.toList());
	}

//	public static List<Integer> getCharacterKeycodeList() {
//		List<Integer> keyList = new ArrayList<>();
//		for (Integer i = KeyEvent.VK_COMMA; i <= KeyEvent.VK_DIVIDE; ++i) { keyList.add(i); }
//		keyList.addAll(Arrays.asList(KeyEvent.VK_SPACE, KeyEvent.VK_BACK_QUOTE));
//		keyList.removeAll(getNumberKeys());
//		return keyList;
//	}

	// be carefull, it contains only tunes from 34 to 128
	public static Integer tuneToAscii(Integer tune) {
		return getAsciTuneMap().inverse().get(tune);
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
		return (this.mod > 0 ? this.getModName() + "+" : "") + this.getKeyName() + "(" + keyCode + ")";
	}

	public KeyStroke toKeystroke() {
		return KeyStroke.getKeyStroke(getKeyCode(), mod);
	}

	private String getModName() {
		Map<Integer, String> modMap = new TruMap<>().p(KeyEvent.SHIFT_MASK, "Shift").p(KeyEvent.CTRL_MASK, "Ctrl").p(KeyEvent.ALT_MASK, "Alt");
		return modMap.get(this.mod);
	}

	private String getKeyName() {
		return Character.toString((char)getKeyCode());
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
		for (Integer i = PianoLayoutPanel.FIRST_TUNE; i < 128; ++i) {
			if (!map.containsValue(i)) {
				map.put(256 + i, i); // setting not existing (on generic keyboard) key
				// TODO: not parsed back correctly
			}
		}

		map.put(KeyEvent.VK_PAUSE, 0);

		return map;
	}

	public static BiMap<Combo, Integer> getComboTuneMap() {
		int mod = getAsciiTuneMods();
		BiMap<Combo, Integer> map = HashBiMap.create();
		for (Map.Entry<Integer, Integer> entry: getAsciTuneMap().entrySet()) {
			map.put(new Combo(mod, entry.getKey()), entry.getValue());
		}
		return map;
	}

}
