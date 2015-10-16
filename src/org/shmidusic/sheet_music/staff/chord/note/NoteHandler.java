package org.shmidusic.sheet_music.staff.chord.note;

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

public class NoteHandler extends AbstractHandler {

	public NoteHandler(NoteComponent context) {
		super(context);
	}
	private static TruMap<Combo, ContextAction<NoteComponent>> actionMap = new TruMap<>();
	static {
		actionMap
			.p(new Combo(0, k.VK_OPEN_BRACKET), mkAction(modelRecalcTacts(Note::decLen)).setCaption("Decrease Length"))
			.p(new Combo(0, k.VK_CLOSE_BRACKET), mkAction(modelRecalcTacts(Note::incLen)).setCaption("Increase Length"))
			.p(new Combo(0, k.VK_PERIOD), mkAction(modelRecalcTacts(Note::putDot)).setCaption("Dot"))
			.p(new Combo(0, k.VK_COMMA), mkAction(modelRecalcTacts(Note::removeDot)).setCaption("Undot"))
			.p(new Combo(0, k.VK_DELETE), mkAction(c -> c.getParentComponent().remove(c.note)).setCaption("Delete"))
			.p(new Combo(0, k.VK_ENTER), mkAction(c -> PlayMusThread.playNote(c.note)).setCaption("Play"))

			.p(new Combo(k.CTRL_MASK, k.VK_3), mkAction(modelRecalcTacts(Note::triggerTupletDenominator)).setCaption("Switch Triplet/Normal"))
			.p(new Combo(k.CTRL_MASK, k.VK_H), mkAction(model(Note::triggerIsMuted)).setCaption("Mute/Unmute"))
			.p(new Combo(k.SHIFT_MASK, k.VK_3), mkFailableAction(NoteComponent::triggerIsSharp).setCaption("Switch Sharp/Flat"))
		.
		p(new Combo(k.SHIFT_MASK, k.VK_BACK_QUOTE), mkAction(model(Note::triggerIsLinkedToNext)).setCaption("Link/Unlink with next"))
			.p(new Combo(k.CTRL_MASK, k.VK_I), mkAction(n -> JOptionPane.showMessageDialog(n.getFirstAwtParent(), n.note.toString())).setCaption("Info"))
		;

		for (Combo combo: Combo.getNumberComboList(0)) {
			actionMap.p(combo, mkAction(note -> {
                changeChannel(note, combo.getPressedNumber());
                note.getSettings().setDefaultChannel(combo.getPressedNumber());
            }).setOmitMenuBar(true));
		}

		for (Map.Entry<Combo, Integer> entry: Combo.getComboTuneMap().entrySet()) {
			actionMap.p(entry.getKey(), mkAction(c -> c.getParentComponent().addNewNote(entry.getValue(), Settings.inst().getDefaultChannel()))
				.setOmitMenuBar(true));
		}
	}

	private static Consumer<NoteComponent> model(Consumer<Note> modelLambda)
	{
		return c -> {
			modelLambda.accept(c.note);
			c.getParentComponent().repaint();
		};
	}

	private static Consumer<NoteComponent> modelRecalcTacts(Consumer<Note> modelLambda)
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

	synchronized private static void changeChannel(NoteComponent note, int channel) {

        Boolean canChange = note.getParentComponent().chord.noteStream()
                .noneMatch(n -> n.channel.get() == channel && n.tune.get() == note.note.tune.get());

        if (canChange) {
            JSONObject js = note.note.getJsonRepresentation();
            js.put(note.note.channel.getName(), channel);
            note.getParentComponent().addNewNote(js);
            note.getParentComponent().remove(note.note);
        }
	}
}
