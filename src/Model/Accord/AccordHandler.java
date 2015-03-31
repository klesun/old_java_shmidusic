
package Model.Accord;

import Model.Accord.Nota.Nota;
import Model.Accord.Nota.NotaHandler;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.*;

public class AccordHandler {
	
	private Accord context = null;
	private LinkedHashMap<List<Integer>, Consumer<KeyEvent>> handleEvent = new LinkedHashMap<>();
	
	public AccordHandler(Accord context) {
		this.context = context;
		this.init();
	}

	private void init() {

		// you maybe noticed than mojority of events are just events from child runt through all children
		// maybe do some abstraction? cuz for example i wanna change combination for length change
		// so i have to change it in both accord and nota =(

		this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_3), (event) -> {
			for (Nota n: getContext().getNotaList()) { n.setTupletDenominator(n.getTupletDenominator() == 3 ? 1 : 3); }
		});
		this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_H), (event) -> {
			for (Nota n: getContext().getNotaList()) { n.setIsMuted(!n.getIsMuted()); }
		});
		this.handleEvent.put(Arrays.asList(KeyEvent.SHIFT_MASK, KeyEvent.VK_3), (event) -> {
			for (Nota nota: getContext().getNotaList()) { nota.triggerIsSharp(); }
		});
		this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_ADD), (event) -> {
			for (Nota nota: getContext().getNotaList()) { nota.changeDur(1); }
		});
		this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_SUBTRACT), (event) -> {
			for (Nota nota: getContext().getNotaList()) { nota.changeDur(-1); }
		});

		this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_DOWN), (event) -> {
			getContext().moveFocus(+1);
		});
		this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_UP), (event) -> {
			getContext().moveFocus(-1);
		});

		Consumer<KeyEvent> handlePressNumber = (e) -> {
			int cifra = (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') ? e.getKeyCode() - '0' : e.getKeyCode() - KeyEvent.VK_NUMPAD0;
			cifra = Math.min(cifra, getContext().getNotaList().size());
			getContext().setFocusedIndex(cifra);
		};
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { this.handleEvent.put(Arrays.asList(0, i), handlePressNumber); }
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; ++i) { this.handleEvent.put(Arrays.asList(0, i), handlePressNumber); }

		this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_BACK_SPACE), (event) -> {
			String slog = getContext().getSlog();
			if (slog.length() < 2) {
				if (slog.length() == 0) {
					getContext().getParentStaff().moveFocus(-1);
				}
				getContext().setSlog("");
			} else {
				getContext().setSlog(slog.substring(0, slog.length() - 1));
			}
		});
		Consumer<KeyEvent> handlePressChar = (e) -> { getContext().setSlog(getContext().getSlog().concat("" + e.getKeyChar())); };
		for (int i = KeyEvent.VK_COMMA; i <= KeyEvent.VK_DIVIDE; ++i) {
			if (!this.handleEvent.containsKey(Arrays.asList(0, i))) {
				this.handleEvent.put(Arrays.asList(0, i), handlePressChar);
			}
		}
		int[] additionalCharacterKeycodes = new int[]{KeyEvent.VK_SPACE, KeyEvent.VK_BACK_QUOTE};
		for (int i: additionalCharacterKeycodes) { this.handleEvent.put(Arrays.asList(0, i), handlePressChar); }
	}

	// TODO: ABSTRACTIZE!
	public LinkedHashMap<List<Integer>, Consumer<KeyEvent>> getKeyHandler() {
		LinkedHashMap<List<Integer>, Consumer<KeyEvent>> combinedHandle = (LinkedHashMap)handleEvent.clone();
		if (getContext().getFocusedChild() != null) {
			LinkedHashMap<List<Integer>, Consumer<KeyEvent>> handleKey = new NotaHandler(getContext().getFocusedNota()).getKeyHandler();
			combinedHandle.putAll(handleKey);
		}
		return combinedHandle;
	}

	public Accord getContext() {
		return this.context;
	}
}
