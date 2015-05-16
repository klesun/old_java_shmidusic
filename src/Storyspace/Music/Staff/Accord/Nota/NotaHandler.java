package Storyspace.Music.Staff.Accord.Nota;

import Model.AbstractHandler;
import Model.Combo;
import Stuff.Musica.PlayMusThread;

import java.util.HashMap;

public class NotaHandler extends AbstractHandler {

	public NotaHandler(Nota context) {
		super(context);
	}

	@Override
	public Nota getContext() {
		return (Nota)super.getContext();
	}

	@Override
	protected void initActionMap() {
		addCombo(k.CTRL_MASK, k.VK_3).setDo((event) -> { getContext().triggerTupletDenominator(); }).biDirectional();
		addCombo(k.CTRL_MASK, k.VK_H).setDo(combo -> { getContext().triggerIsMuted(); }).biDirectional();
		addCombo(k.SHIFT_MASK, k.VK_3).setDo((event) -> { getContext().triggerIsSharp(); }).biDirectional();

		addCombo(0, k.VK_OPEN_BRACKET).setDo(getContext()::changeLength).setUndoChangeSign();
		addCombo(0, k.VK_CLOSE_BRACKET).setDo(getContext()::changeLength).setUndoChangeSign();

		addCombo(0, k.VK_ENTER).setDo((event) -> { PlayMusThread.playNotu(getContext()); });

		for (Integer i: Combo.getNumberKeyList()) {
			addCombo(0, i).setDo2((combo) -> {
				int lastChan = getContext().channel;
				getContext().setChannel(combo.getPressedNumber());
				return new HashMap<String, Object>() {{ put("lastChan", lastChan); }};
			}).setUndo((combo, paramsForUndo) -> { getContext().setChannel((Integer) paramsForUndo.get("lastChan")); });
		}

		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			addCombo(11, i).setDo((combo) -> { // 11 - alt+shif+ctrl
				new Nota(getContext().getParentAccord(), combo.asciiToTune()).setKeydownTimestamp(System.currentTimeMillis());
				return true;
			});
		}
	}
}
