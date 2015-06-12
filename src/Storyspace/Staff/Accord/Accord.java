package Storyspace.Staff.Accord;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

import Model.Combo;
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

	ArrayList<Nota> notaList = new ArrayList<Nota>();
	String slog = "";

	public Staff parentStaff = null;
	int focusedIndex = -1;

	public Accord(Staff parent) {
		super(parent);
	}

	public Accord add(Nota nota) {
		this.notaList.add(nota);
		Collections.sort(this.notaList);
		return this;
	}

	@Override
	public void getJsonRepresentation(JSONObject dict) {
		dict.put("notaList", new JSONArray(getNotaList().stream().map(n -> n.getJsonRepresentation()).toArray()));
		dict.put("slog", this.slog);
	}

	@Override
	public Accord reconstructFromJson(JSONObject jsObject) throws JSONException {
		JSONArray notaJsonList = jsObject.getJSONArray("notaList");
		for (int idx = 0; idx < notaJsonList.length(); ++idx) {
			JSONObject childJs = notaJsonList.getJSONObject(idx);
			new Nota(this, 0).reconstructFromJson(childJs);
		}
		this.slog = jsObject.getString("slog");
		return this;
	}
 
	@Override
	public void drawOn(Graphics surface, int x, int y) {
		Boolean oneOctaveLower = false;
		// i don't like this
		if (getNotaList().size() > 0) {
			surface.setColor(Color.blue);
			oneOctaveLower = this.isHighestBotommedToFitSystem();
			if (oneOctaveLower) {
				surface.drawString("8va", x, y + 4 * dy());
			}
		}

		surface.setColor(Color.black);
		for (int i = 0; i < getNotaList().size(); ++i) {
			Nota nota = getNotaList().get(i);
			int notaY = y + getLowestPossibleNotaY() - dy() * nota.getAbsoluteAcademicIndex() + (oneOctaveLower ? 7 * dy() : 0);
			int notaX = i > 0 && getNotaList().get(i - 1).getAbsoluteAcademicIndex() == nota.getAbsoluteAcademicIndex()
				? x + Settings.getStepWidth() / 3 // TODO: draw them flipped
				: x;

			if (nota == this.getFocusedNota()) {
				List<Integer> p = nota.getAncorPoint();
				int r = Settings.getStepHeight();
				surface.setColor(Color.red);
				surface.fillOval(x + p.get(0) + r * 2, notaY + p.get(1) - r, r * 2, r * 2);
			}
			
			nota.drawOn(surface, notaX, notaY);
			if (nota.isStriked() != oneOctaveLower) {
				List<Integer> p = Fp.vectorSum(nota.getTraitCoordinates(), Arrays.asList(x, notaY, x, notaY));
				surface.drawLine(p.get(0), p.get(1), p.get(2), p.get(3)); 
			}
		}

		surface.setColor(Color.BLACK);
		surface.drawString(this.getSlog(), x, y + Constants.FONT_HEIGHT);
	}

	// responsees to events (actions)

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

	public ArrayList<Nota> getNotaList() {
		return this.notaList;
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
