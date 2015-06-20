package Storyspace.Staff.Accord;

import java.awt.Graphics;
import java.util.*;

import Model.Action;
import Model.Combo;
import Model.Field.Arr;
import Model.Field.Field;
import Model.SimpleAction;
import Storyspace.Staff.MidianaComponent;
import org.apache.commons.math3.fraction.Fraction;

import Storyspace.Staff.Accord.Nota.Nota;
import Storyspace.Staff.Staff;
import org.json.JSONObject;

public class Accord extends MidianaComponent {

	private Field<Boolean> isDiminendo = new Field<>("isDiminendo", false, this);
	public Arr<Nota> notaList = new Arr<>("notaList", new TreeSet<>(), this, Nota.class);

	String slog = "";

	int focusedIndex = -1;

	public Accord(Staff parent) { super(parent); }
 
	@Override
	public void drawOn(Graphics surface, int x, int y) {
		new AccordPainter(this, surface, x, y).draw();
	}

	// responses to events (actions)

	public Boolean moveFocus(Combo combo) {
		int n = combo.getSign();
		int wasIndex = getFocusedIndex();
		if (this.getFocusedIndex() + n > this.getNotaList().size() - 1) {
			this.setFocusedIndex(-1);
		} else if (this.getFocusedIndex() + n < -1) {
			this.setFocusedIndex(this.getNotaList().size() - 1);
		} else {
			this.setFocusedIndex(this.getFocusedIndex() + n);
		}
		return wasIndex != getFocusedIndex();
	}

	public void deleteFocused() {
		if (getFocusedNota() != null) {
			remove(getFocusedNota());
			setFocusedIndex(getFocusedIndex() - 1);
		}
	}

	// getters/setters

	public int getHeight() {
		return this.getLowestPossibleNotaY();
	}

	public Collection<Nota> getNotaList() {
		return (TreeSet)notaList.get();
	}

	public long getEarliestKeydown() {
		Nota nota = this.getNotaList().stream().reduce(null, (a, b) -> a != null && a.keydownTimestamp < b.keydownTimestamp ? a : b);
		return nota != null ? nota.keydownTimestamp : 0;
	}

	public Boolean isHighestBotommedToFitSystem() {
		Nota nota = this.getNotaList().stream().reduce(null, (a, b) -> a != null && a.tune.get() > b.tune.get() ? a : b);
		return nota != null ? nota.isBotommedToFitSystem() : false;
	}

	public Nota findByTuneAndChannel(int tune, int channel) {
		return this.getNotaList().stream().filter(n -> n.tune.get() == tune && n.getChannel() == channel).findFirst().orElse(null);
	}

	public int getShortestTime() {
		return Nota.getTimeMilliseconds(getShortestFraction(), getParentStaff().getConfig().getTempo());
	}

	public Fraction getShortestFraction() {
		Nota nota = this.getNotaList().stream().reduce(null, (a, b) -> a != null && !a.isLongerThan(b) && !a.getIsMuted() ? a : b);
		return nota != null ? nota.length.get() : new Fraction(0);
	}

	synchronized public Nota getFocusedNota() {
		return getFocusedIndex() > -1 ? this.notaList.get(getFocusedIndex()) : null;
	}

	public int getLowestPossibleNotaY() {
		return 50 * dy();
	}

	// field getters/setters

	public Staff getParentStaff() {
		return (Staff)this.getModelParent();
	}
	public String getSlog() {
		return this.slog;
	}
	public Accord setSlog(String value) { this.slog = value; return this; }
	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public Boolean getIsDiminendo() { return isDiminendo.get(); }
	public void setIsDiminendo(Boolean value) { isDiminendo.set(value); }

	public void triggerIsDiminendo(Combo c) {
		setIsDiminendo(!getIsDiminendo());
	}

	public Accord getNext() {
		int nextIndex = getParentStaff().getAccordList().indexOf(this) + 1;
		return nextIndex < getParentStaff().getAccordList().size()
				? getParentStaff().getAccordList().get(nextIndex)
				: null;
	}

	public Accord setFocusedIndex(int value) {
		value = value >= this.getNotaList().size() ? this.getNotaList().size() - 1 : value;
		value = value < -1 ? -1 : value;
		this.focusedIndex = value;
		return this;
	}

	@Override
	public Nota getFocusedChild() {
		return this.getFocusedNota();
	}
	@Override
	protected AccordHandler makeHandler() {
		return new AccordHandler(this);
	}

	// event handles

	public Nota addNewNota(int tune, int channel) {
		return add(new Nota(this).setTune(tune).setChannel(channel)
			.setKeydownTimestamp(System.currentTimeMillis()));
	}

	public Nota addNewNota(JSONObject newNotaJs) {
		return add(new Nota(this).reconstructFromJson(newNotaJs)
			.setKeydownTimestamp(System.currentTimeMillis()));
	}

	public Nota add(Nota nota) {
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> getNotaList().add(nota))
			.setUndo(() -> getNotaList().remove(nota)));
		return nota;
	}

	public void remove(Nota nota) {
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> getNotaList().remove(nota))
			.setUndo(() -> getNotaList().add(nota)));
	}

}
