
package Model.Accord;

import Model.Accord.Nota.Nota;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.*;

public class AccordHandler {
	
	private Accord context = null;
	LinkedHashMap<List<Integer>, Consumer<KeyEvent>> handleEvent = new LinkedHashMap<>();
	
	public AccordHandler(Accord context) {
		this.context = context;
		this.init();
	}

	public Boolean handleKey(KeyEvent e) {
		List<Integer> key = Arrays.asList(e.getModifiers(), e.getKeyCode());
		if (handleEvent.containsKey(key)) {
			Consumer<KeyEvent> handle = handleEvent.get(key);
			handle.accept(e);
			getContext().getParentStaff().parentSheetMusic.parentWindow.keyHandler.requestNewSurface();
			return true;
		}
		return false;
	}

	private void init() {
		this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_T), (event) -> {
			getContext().triggerTuplets(3); // TODO: do something, so if child event handled - parent no need
		});
		this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_DOWN), (event) -> {
			getContext().moveFocus(+1);
		});
		this.handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_UP), (event) -> {
			getContext().moveFocus(-1);
		});

		this.handleEvent.put(Arrays.asList(KeyEvent.SHIFT_MASK, KeyEvent.VK_3), (event) -> {
			if (getContext().getFocusedNota() == null) {
				for (Nota nota: getContext().getNotaList()) { nota.triggerIsSharp(); }
			} else {
				getContext().getFocusedNota().triggerIsSharp(); // same here. may create dictionary<[KeyMask, KeyCode], Boolean> and pass from children, or just die(-100) in child, cause we have only one event. yes... die(-100) i like more
			}
		});

		this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_ADD), (event) -> {
			if (getContext().getFocusedIndex() == -1) {
				for (Nota nota: getContext().getNotaList()) { nota.changeDur(1); }
			} else {
				getContext().getFocusedNota().changeDur(1);
			}
		});
		this.handleEvent.put(Arrays.asList(0, KeyEvent.VK_SUBTRACT), (event) -> {
			if (getContext().getFocusedIndex() == -1) {
				for (Nota nota: getContext().getNotaList()) { nota.changeDur(-1); }
			} else {
				getContext().getFocusedNota().changeDur(-1);
			}
		});
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

		Consumer<KeyEvent> handlePressNumber = (e) -> {
			int cifra = (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') ? e.getKeyCode() - '0' : e.getKeyCode() - KeyEvent.VK_NUMPAD0;
			Nota nota = getContext().getFocusedNota();
			if (nota != null) {
				if (nota.channel != cifra) {
					nota.setChannel(cifra);
				} else {
					getContext().setFocusedIndex(-1);
				}
			} else {
				cifra = Math.min(cifra, getContext().getNotaList().size());
				getContext().setFocusedIndex(cifra);
			}
		};
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { this.handleEvent.put(Arrays.asList(0, i), handlePressNumber); }
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; ++i) { this.handleEvent.put(Arrays.asList(0, i), handlePressNumber); }

		Consumer<KeyEvent> handlePressChar = (e) -> { getContext().setSlog(getContext().getSlog().concat("" + e.getKeyChar())); };
		for (int i = KeyEvent.VK_COMMA; i <= KeyEvent.VK_DIVIDE; ++i) {
			if (!this.handleEvent.containsKey(Arrays.asList(0, i))) {
				this.handleEvent.put(Arrays.asList(0, i), handlePressChar);
			}
		}
	}

	public Accord getContext() {
		return this.context;
	}
}
