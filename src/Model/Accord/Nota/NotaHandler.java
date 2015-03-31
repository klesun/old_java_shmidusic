package Model.Accord.Nota;

import Model.Accord.Accord;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class NotaHandler {

	private Nota context = null;
	private LinkedHashMap<List<Integer>, Consumer<KeyEvent>> handleEvent = null;

	public NotaHandler(Nota context) {
		this.context = context;
		this.init();
	}

	// TODO: на каждое действие должно быть своё противодействие. А ещё лямбда, которая проверяет, может ли действие быть выполнено - иначе передать инициативу родителю

	private void init() {
		handleEvent = new LinkedHashMap<>();

		handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_3), (event) -> {
			getContext().setTupletDenominator(getContext().getTupletDenominator() == 3 ? 1 : 3);
		});
		handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_H), (event) -> {
			getContext().setIsMuted(!getContext().getIsMuted());
		});
		handleEvent.put(Arrays.asList(KeyEvent.SHIFT_MASK, KeyEvent.VK_3), (event) -> {
			getContext().triggerIsSharp();
		});
		handleEvent.put(Arrays.asList(KeyEvent.CTRL_MASK, KeyEvent.VK_2), (event) -> {
			Accord accord = getContext().getParentAccord();
			new Nota(accord).reconstructFromJson(getContext().getJsonRepresentation());
		});
		handleEvent.put(Arrays.asList(0, KeyEvent.VK_ADD), (event) -> {
			getContext().changeDur(1);
		});
		handleEvent.put(Arrays.asList(0, KeyEvent.VK_SUBTRACT), (event) -> {
			getContext().changeDur(-1);
		});
		handleEvent.put(Arrays.asList(0, KeyEvent.VK_DELETE), (event) -> {
			getContext().getParentAccord().deleteFocused();
		});
		Consumer<KeyEvent> handlePressNumber = (e) -> {
			int cifra = (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') ? e.getKeyCode() - '0' : e.getKeyCode() - KeyEvent.VK_NUMPAD0;
			if (getContext().channel != cifra) {
				getContext().setChannel(cifra);
			} else {
				getContext().getParentAccord().setFocusedIndex(-1);
			}
		};
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { handleEvent.put(Arrays.asList(0, i), handlePressNumber); }
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; ++i) { handleEvent.put(Arrays.asList(0, i), handlePressNumber); }
	}

	public LinkedHashMap<List<Integer>, Consumer<KeyEvent>> getKeyHandler() {
		return handleEvent;
	}

	public Nota getContext() {
		return this.context;
	}

}
