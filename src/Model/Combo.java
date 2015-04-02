package Model;

import javafx.scene.input.KeyCode;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	public Combo changeSign() {
		return new Combo(this.mod, anti(this.keyCode));
	}

	private static int anti(int keyCode) {
		if (keyCode == KeyEvent.VK_UP) return KeyEvent.VK_DOWN; else
		if (keyCode == KeyEvent.VK_DOWN) return KeyEvent.VK_UP; else
		if (keyCode == KeyEvent.VK_LEFT) return KeyEvent.VK_RIGHT; else
		if (keyCode == KeyEvent.VK_RIGHT) return KeyEvent.VK_LEFT; else
		if (keyCode == KeyEvent.VK_PLUS) return KeyEvent.VK_MINUS; else
		if (keyCode == KeyEvent.VK_MINUS) return KeyEvent.VK_PLUS; else {
			System.out.println("Жопа!!! Этот метод не должен вызываться с параметром " + keyCode);
			System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
		}
		return keyCode;
	}

	public int getSign() {
		if (Arrays.asList(KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_MINUS).contains(getKeyCode())) {
			return -1;
		} else if (Arrays.asList(KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_PLUS).contains(getKeyCode())) {
			return +1;
		} else {
			return 0;
		}
	}

	public int getPressedNumber() {
		return (getKeyCode() >= '0' && getKeyCode() <= '9') ? getKeyCode() - '0' : getKeyCode() - KeyEvent.VK_NUMPAD0;
	}

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

	public Boolean isUndoOrRedo() {
		Combo undo = new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Z);
		Combo redo = new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Y);
		return this.equals(undo) || this.equals(redo);
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

}
