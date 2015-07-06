package BlockSpacePkg.StaffPkg.Accord.Nota;

import Model.*;
import Stuff.Musica.PlayMusThread;
import Stuff.OverridingDefaultClasses.TruMap;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NotaHandler extends AbstractHandler {

	public NotaHandler(Nota context) {
		super(context);
	}

	@Override
	public Nota getContext() {
		return (Nota)super.getContext();
	}

	@Override
	public LinkedHashMap<Combo, ContextAction> getStaticActionMap() {
		return new LinkedHashMap<>(makeStaticActionMap());
	}

	public static LinkedHashMap<Combo, ContextAction<Nota>> makeStaticActionMap() {
		TruMap<Combo, ContextAction<Nota>> actionMap = new TruMap<>();
		actionMap
			.p(new Combo(0, k.VK_OPEN_BRACKET), mkAction(Nota::decLen).setCaption("Decrease Length"))
			.p(new Combo(0, k.VK_CLOSE_BRACKET), mkAction(Nota::incLen).setCaption("Increase Length"))
			.p(new Combo(0, k.VK_PERIOD), mkAction(Nota::putDot).setCaption("Dot"))
			.p(new Combo(0, k.VK_COMMA), mkAction(Nota::removeDot).setCaption("Undot"))
			.p(new Combo(0, k.VK_DELETE), mkAction(nota -> nota.getParentAccord().remove(nota)).setCaption("Delete"))
			.p(new Combo(0, k.VK_ENTER), mkAction(nota -> PlayMusThread.playNotu(nota)).setCaption("Play"))

			.p(new Combo(k.CTRL_MASK, k.VK_3), mkAction(Nota::triggerTupletDenominator).setCaption("Switch Triplet/Normal"))
			.p(new Combo(k.CTRL_MASK, k.VK_H), mkAction(Nota::triggerIsMuted).setCaption("Mute/Unmute"))
			.p(new Combo(k.SHIFT_MASK, k.VK_3), mkAction(Nota::triggerIsSharp).setCaption("Switch Sharp/Flat"))
			.p(new Combo(k.SHIFT_MASK, k.VK_BACK_QUOTE), mkAction(Nota::triggerIsLinkedToNext).setCaption("Link/Unlink with next"))
			;

		for (Combo combo: Combo.getNumberComboList(0)) {
			actionMap.p(combo, mkAction(nota -> {
				changeChannel(nota, combo.getPressedNumber());
				nota.getSettings().setDefaultChannel(combo.getPressedNumber());
			}).setOmitMenuBar(true));
		}

		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			actionMap.p(entry.getKey(), mkAction(nota -> nota.getParentAccord().addNewNota(entry.getValue(), nota.getSettings().getDefaultChannel()))
					.setOmitMenuBar(true));
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
}
