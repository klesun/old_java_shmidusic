package Model.Staff.Accord.Nota;

import Model.AbstractHandler;
import Model.Action;
import Model.Combo;
import Model.Staff.Accord.Accord;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class NotaHandler extends AbstractHandler {

	public NotaHandler(Nota context) {
		super(context);
	}

	@Override
	public Nota getContext() {
		return (Nota)super.getContext();
	}

	// TODO: на каждое действие должно быть своё противодействие. А ещё лямбда, которая проверяет, может ли действие быть выполнено - иначе передать инициативу родителю

	@Override
	protected void init() {
		actionMap = new LinkedHashMap<>();

		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_3), new Action().setDo((event) -> {
			getContext().setTupletDenominator(getContext().getTupletDenominator() == 3 ? 1 : 3);
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_H), new Action().setDo((event) -> {
			getContext().setIsMuted(!getContext().getIsMuted());
		}));
		actionMap.put(new Combo(KeyEvent.SHIFT_MASK, KeyEvent.VK_3), new Action().setDo((event) -> {
			getContext().triggerIsSharp();
		}));
		actionMap.put(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_2), new Action().setDo((event) -> {
			Accord accord = getContext().getParentAccord();
			new Nota(accord, getContext().tune).reconstructFromJson(getContext().getJsonRepresentation());
		}));
		actionMap.put(new Combo(0, KeyEvent.VK_ADD), new Action().setDo(getContext()::changeDur).setUndoChangeSign());
		actionMap.put(new Combo(0, KeyEvent.VK_SUBTRACT), new Action().setDo(getContext()::changeDur).setUndoChangeSign());

		Consumer<Combo> handlePressNumber = (e) -> {
			int cifra = (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') ? e.getKeyCode() - '0' : e.getKeyCode() - KeyEvent.VK_NUMPAD0;
			if (getContext().channel != cifra) {
				getContext().setChannel(cifra);
			} else {
				getContext().getParentAccord().setFocusedIndex(-1);
			}
		};
		for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; ++i) { actionMap.put(new Combo(0, i), new Action().setDo((handlePressNumber))); }
		for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; ++i) { actionMap.put(new Combo(0, i), new Action().setDo((handlePressNumber))); }
	}
}
