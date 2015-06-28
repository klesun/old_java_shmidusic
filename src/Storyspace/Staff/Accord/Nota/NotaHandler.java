package Storyspace.Staff.Accord.Nota;

import Gui.Settings;
import Model.*;
import Storyspace.Staff.Accord.Accord;
import Storyspace.Staff.StaffHandler;
import Stuff.Musica.PlayMusThread;
import Stuff.OverridingDefaultClasses.TruHashMap;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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

		for (Map.Entry<Combo, ContextAction<Nota>> entry: makeStaticActionMap().entrySet()) {
			new ActionFactory(entry.getKey()).addTo(this.actionMap).setDo(() -> entry.getValue().redo(getContext()));
		}
	}

	public static Map<Combo, ContextAction<Nota>> makeStaticActionMap() {
		TruHashMap<Combo, ContextAction<Nota>> actionMap = new TruHashMap<>();
		actionMap.p(new Combo(k.CTRL_MASK, k.VK_3), mkAction(Nota::triggerTupletDenominator))
			.p(new Combo(k.CTRL_MASK, k.VK_H), mkAction(Nota::triggerIsMuted))
			.p(new Combo(k.SHIFT_MASK, k.VK_3), mkAction(Nota::triggerIsSharp))
			.p(new Combo(k.SHIFT_MASK, k.VK_BACK_QUOTE), mkAction(Nota::triggerIsLinkedToNext))

			.p(new Combo(0, k.VK_OPEN_BRACKET), mkAction(Nota::decLen))
			.p(new Combo(0, k.VK_CLOSE_BRACKET), mkAction(Nota::incLen))
			.p(new Combo(0, k.VK_PERIOD), mkAction(Nota::putDot))
			.p(new Combo(0, k.VK_COMMA), mkAction(Nota::removeDot))
			.p(new Combo(0, k.VK_DELETE), mkAction(nota -> nota.getParentAccord().remove(nota)))

			.p(new Combo(0, k.VK_ENTER), mkAction(nota -> PlayMusThread.playNotu(nota)));

		for (Combo combo: Combo.getNumberComboList(0)) {
			actionMap.p(combo, mkAction(nota -> {
				changeChannel(nota, combo.getPressedNumber());
				Settings.inst().setDefaultChannel(combo.getPressedNumber());
			}));
		}

		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			actionMap.p(entry.getKey(), mkAction(nota -> nota.getParentAccord().addNewNota(entry.getValue(), Settings.inst().getDefaultChannel())));
		}

		return actionMap;
	}

	// stupid java, stupid lambdas don't see stupid generics! that's why i was forced to create separate method to do it in one line
	private static ContextAction<Nota> mkAction(Consumer<Nota> lambda) {
		ContextAction<Nota> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	synchronized private static void changeChannel(Nota nota, int channel) {
		JSONObject js = nota.getJsonRepresentation();
		nota.getParentAccord().remove(nota);
		js.put(nota.channel.getName(), channel);
		nota.getParentAccord().addNewNota(js);
	}

	private static StaffHandler getStaffHandler(Nota n) {
		return n.getParentAccord().getParentStaff().getHandler();
	}
}
