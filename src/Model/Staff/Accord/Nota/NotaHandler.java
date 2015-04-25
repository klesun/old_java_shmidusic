package Model.Staff.Accord.Nota;

import Model.AbstractHandler;
import Model.ActionFactory;
import Model.Combo;
import Model.Staff.Accord.Accord;
import Model.Staff.Staff;
import Musica.PlayMusThread;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class NotaHandler extends AbstractHandler {

	public NotaHandler(Nota context) {
		super(context);
	}

	@Override
	public Nota getContext() {
		return (Nota)super.getContext();
	}

	@Override
	protected void init() {
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_3)).addTo(actionMap).setDo((event) -> {
			getContext().setTupletDenominator(getContext().getTupletDenominator() == 3 ? 1 : 3);
		}).biDirectional();
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_H)).addTo(actionMap).setDo((event) -> {
			getContext().setIsMuted(!getContext().getIsMuted());
		}).biDirectional();
		new ActionFactory(new Combo(KeyEvent.SHIFT_MASK, KeyEvent.VK_3)).addTo(actionMap).setDo((event) -> {
			getContext().triggerIsSharp();
		}).biDirectional();
		new ActionFactory(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_2)).addTo(actionMap).setDo2((event) -> {
			Accord accord = getContext().getParentAccord();
			Nota clonedNota = new Nota(accord, getContext().tune).reconstructFromJson(getContext().getJsonRepresentation());
			return new HashMap<String, Object>(){{ put("clonedNota", clonedNota); }};
		}).setUndo((combo, paramsForUndo) -> {
			Accord accord = getContext().getParentAccord();
			accord.setFocusedIndex(accord.getNotaList().indexOf(paramsForUndo.get("clonedNota"))).deleteFocused();
			accord.setFocusedIndex(accord.getNotaList().indexOf(getContext()));
		});

		for (Integer i: Arrays.asList(KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET)) {
			new ActionFactory(new Combo(0, i)).addTo(actionMap).setDo(getContext()::changeDur).setUndoChangeSign(); }

		new ActionFactory(new Combo(0, KeyEvent.VK_ENTER)).addTo(actionMap).setDo((event) -> { PlayMusThread.playNotu(getContext()); });

		for (Integer i: Combo.getNumberKeyList()) {	new ActionFactory(new Combo(0, i)).addTo(actionMap).setDo2((combo) -> {
			int lastChan = getContext().channel;
			getContext().setChannel(combo.getPressedNumber());
			return new HashMap<String, Object>() {{
				put("lastChan", lastChan);
			}};
		}).setUndo((combo, paramsForUndo) -> {
			getContext().setChannel((Integer) paramsForUndo.get("lastChan"));
		});}

		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			new ActionFactory(new Combo(11, i)).addTo(actionMap).setDo((combo) -> { // 11 - alt+shif+ctrl

				// TODO: move stuff like constants and mode into the handler

				if (getContext().getParentAccord().getParentStaff().mode == Staff.aMode.passive ||
					getContext().getParentAccord().getParentStaff().mode == Staff.aMode.playin) {
					// Показать, какую ноту ты нажимаешь
					return false;
				} else {
					new Nota(getContext().getParentAccord(), combo.asciiToTune()).setKeydownTimestamp(System.currentTimeMillis());
					return true;
				}
			});
		}
	}
}
