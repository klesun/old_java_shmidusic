package Storyspace.Staff.Accord;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

import Model.Combo;
import Model.Field.Arr;
import Model.Field.Field;
import Storyspace.Staff.MidianaComponent;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Gui.Constants;
import Gui.Settings;
import Storyspace.Staff.Accord.Nota.Nota;
import Storyspace.Staff.Staff;
import Stuff.Tools.Fp;

public class Accord extends MidianaComponent {

	private Field<Boolean> isDiminendo = h.addField("isDiminendo", false);
	private Arr<Nota> notaList = (Arr<Nota>)h.addField("notaList", new ArrayList<>(), Nota.class);

	String slog = "";

	int focusedIndex = -1;

	public Accord(Staff parent) { super(parent); }

	public Nota addNewNota() {
		Nota nota = new Nota(this).setKeydownTimestamp(System.currentTimeMillis());
		getNotaList().add(nota);
		Collections.sort(getNotaList());
		return nota;
	}
 
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
			getNotaList().remove(focusedIndex--);
		}
	}

	// getters/setters

	public int getHeight() {
		return this.getLowestPossibleNotaY();
	}

	public List<Nota> getNotaList() {
		return notaList.getValue();
	}

	public long getEarliestKeydown() {
		Nota nota = this.getNotaList().stream().reduce(null, (a, b) -> a != null && a.keydownTimestamp < b.keydownTimestamp ? a : b);
		return nota != null ? nota.keydownTimestamp : 0;
	}

	public Boolean isHighestBotommedToFitSystem() {
		Nota nota = this.getNotaList().stream().reduce(null, (a, b) -> a != null && a.getTune() > b.getTune() ? a : b);
		return nota != null ? nota.isBotommedToFitSystem() : false;
	}

	public Nota findByTuneAndChannel(int tune, int channel) {
		return this.getNotaList().stream().filter(n -> n.getTune() == tune && n.getChannel() == channel).findFirst().orElse(null);
	}

	public int getShortestTime() {
		Nota nota = this.getNotaList().stream().reduce(null, (a, b) -> a != null && !a.isLongerThan(b) && !a.getIsMuted() ? a : b);
		return nota != null ? nota.getTimeMilliseconds(false) : 0;
	}

	public Fraction getShortestFraction() {
		Nota nota = this.getNotaList().stream().reduce(null, (a, b) -> a != null && !a.isLongerThan(b) ? a : b);
		return nota != null ? nota.getLength() : new Fraction(0);
	}

	public Nota getFocusedNota() {
		return getFocusedIndex() > -1 ? this.getNotaList().get(getFocusedIndex()) : null;
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

	public Boolean getIsDiminendo() { return isDiminendo.getValue(); }
	public void setIsDiminendo(Boolean value) { isDiminendo.setValue(value); }

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

}
