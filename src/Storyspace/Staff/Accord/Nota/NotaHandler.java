package Storyspace.Staff.Accord.Nota;

import Model.AbstractHandler;
import Model.Combo;
import Storyspace.Staff.StaffHandler;
import Stuff.Musica.PlayMusThread;
import org.json.JSONObject;

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
		addCombo(k.SHIFT_MASK, k.VK_BACK_QUOTE).setDo((event) -> { getContext().triggerIsLinkedToNext(); }).biDirectional();

		// TODO: these four shortcuts actually totally break our undo-redo system. Try inc len to the edge, move pointer, inc it another time then ctrl-z and OOPS!
		addCombo(0, k.VK_OPEN_BRACKET).setDo(getContext()::changeLength).setUndoChangeSign();
		addCombo(0, k.VK_CLOSE_BRACKET).setDo(getContext()::changeLength).setUndoChangeSign();
		addCombo(0, k.VK_PERIOD).setDo(getContext()::dot).setUndoChangeSign();
		addCombo(0, k.VK_COMMA).setDo(getContext()::dot).setUndoChangeSign();

		addCombo(0, k.VK_ENTER).setDo((event) -> { PlayMusThread.playNotu(getContext()); });

		for (Integer i: Combo.getNumberKeyList()) {
			addCombo(0, i).setDo((combo) -> {
				int newChan = combo.getPressedNumber();
				changeChannel(getContext(), newChan);
				getStaffHandler().setDefaultChannel(newChan);
			});
		}

		for (Integer i: Combo.getAsciTuneMap().keySet()) {
			addCombo(Combo.getAsciiTuneMods(), i).setDo((combo) -> { // 11 - alt+shif+ctrl
				getContext().getParentAccord().addNewNota(combo.asciiToTune(), getStaffHandler().getDefaultChannel());
				return true;
			});
		}
	}

	synchronized private static void changeChannel(Nota nota, int channel) {
		JSONObject js = nota.getJsonRepresentation();
		nota.getParentAccord().remove(nota);
		js.put(nota.channel.getName(), channel);
		nota.getParentAccord().addNewNota(js);
	}

	private StaffHandler getStaffHandler() {
		return getContext().getParentAccord().getParentStaff().getHandler();
	}
}
