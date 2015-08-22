package org.shmidusic.sheet_music.staff.chord.nota;

import org.klesun_model.AbstractHandler;
import org.klesun_model.Combo;
import org.klesun_model.ContextAction;
import org.klesun_model.Explain;
import org.shmidusic.stuff.graphics.Settings;
import org.shmidusic.stuff.musica.PlayMusThread;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMap;
import org.json.JSONObject;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class NotaHandler extends AbstractHandler {

	public NotaHandler(NoteComponent context) {
		super(context);
	}
	private static TruMap<Combo, ContextAction<NoteComponent>> actionMap = new TruMap<>();
	static {
		actionMap
			.p(new Combo(0, k.VK_OPEN_BRACKET), mkAction(modelRecalcTacts(Nota::decLen)).setCaption("Decrease Length"))
			.p(new Combo(0, k.VK_CLOSE_BRACKET), mkAction(modelRecalcTacts(Nota::incLen)).setCaption("Increase Length"))
			.p(new Combo(0, k.VK_PERIOD), mkAction(modelRecalcTacts(Nota::putDot)).setCaption("Dot"))
			.p(new Combo(0, k.VK_COMMA), mkAction(modelRecalcTacts(Nota::removeDot)).setCaption("Undot"))
			.p(new Combo(0, k.VK_DELETE), mkAction(c -> c.getParentComponent().remove(c.note)).setCaption("Delete"))
			.p(new Combo(0, k.VK_ENTER), mkAction(c -> PlayMusThread.playNotu(c.note)).setCaption("Play"))

			.p(new Combo(k.CTRL_MASK, k.VK_3), mkAction(model(Nota::triggerTupletDenominator)).setCaption("Switch Triplet/Normal"))
			.p(new Combo(k.CTRL_MASK, k.VK_H), mkAction(model(Nota::triggerIsMuted)).setCaption("Mute/Unmute"))
			.p(new Combo(k.SHIFT_MASK, k.VK_3), mkFailableAction(NoteComponent::triggerIsSharp).setCaption("Switch Sharp/Flat"))
		.
		p(new Combo(k.SHIFT_MASK, k.VK_BACK_QUOTE), mkAction(model(Nota::triggerIsLinkedToNext)).setCaption("Link/Unlink with next"))
			.p(new Combo(k.CTRL_MASK, k.VK_I), mkAction(n -> JOptionPane.showMessageDialog(n.getFirstAwtParent(), n.note.toString())).setCaption("Info"))
		;

		for (Combo combo: Combo.getNumberComboList(0)) {
			actionMap.p(combo, mkAction(nota -> {
				changeChannel(nota, combo.getPressedNumber());
				nota.getSettings().setDefaultChannel(combo.getPressedNumber());
			}).setOmitMenuBar(true));
		}

		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			actionMap.p(entry.getKey(), mkAction(c -> c.getParentComponent().addNewNota(entry.getValue(), Settings.inst().getDefaultChannel()))
				.setOmitMenuBar(true));
		}
	}

	private static Consumer<NoteComponent> model(Consumer<Nota> modelLambda)
	{
		return c -> {
			modelLambda.accept(c.note);
			c.getParentComponent().repaint();
		};
	}

	private static Consumer<NoteComponent> modelRecalcTacts(Consumer<Nota> modelLambda)
	{
		return c -> {
			modelLambda.accept(c.note);
			c.getParentComponent().recalcTacts();
		};
	}

	@Override
	public NoteComponent getContext() {
		return (NoteComponent)super.getContext();
	}

	@Override
	public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() { return actionMap; }

	public static LinkedHashMap<Combo, ContextAction<NoteComponent>> getClassActionMap() {
		return actionMap;
	}

	// stupid java, stupid lambdas don't see stupid generics! that's why i was forced to create separate method to do it in one line
	private static ContextAction<NoteComponent> mkAction(Consumer<NoteComponent> lambda) {
		ContextAction<NoteComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<NoteComponent> mkFailableAction(Function<NoteComponent, Explain> lambda) {
		ContextAction<NoteComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	synchronized private static void changeChannel(NoteComponent nota, int channel) {
		JSONObject js = nota.note.getJsonRepresentation();
		js.put(nota.note.channel.getName(), channel);
		nota.getParentComponent().addNewNota(js);
		nota.getParentComponent().remove(nota.note);
	}
}
