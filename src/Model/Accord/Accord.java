package Model.Accord;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Gui.Constants;
import Gui.Settings;
import Model.AbstractModel;
import Model.Accord.Nota.Nota;
import Model.Staff;
import Tools.Fp;
import Model.IModel;
import java.util.Arrays;
import java.util.List;

public class Accord extends AbstractModel {

	ArrayList<Nota> notaList = new ArrayList<Nota>();
	String slog = "";

	public Staff parentStaff = null;
	int focusedIndex = -1;

	public Accord(Staff parent) {
		super(parent);
	}

	public Accord add(Nota nota) {
		this.notaList.add(nota);
		return this;
	}

	@Override
	// TODO: maybe instead of LinkedHashMap use JSONArray from the very begining?
	public LinkedHashMap<String, Object> getJsonRepresentation() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<>();
		dict.put("notaList", this.notaList.stream().map(n -> n.getJsonRepresentation()).toArray());
		dict.put("slog", this.slog);
		return dict;
	}

	@Override
	public Accord reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.slog = jsObject.getString("slog");
		JSONArray notaJsonList = jsObject.getJSONArray("notaList");
		for (int idx = 0; idx < notaJsonList.length(); ++idx) {
			JSONObject childJs = notaJsonList.getJSONObject(idx);
			this.add((new Nota(this)).reconstructFromJson(childJs));
		}

		return this;
	}
 
	@Override
	public void drawOn(Graphics surface, int x, int y) {
		surface.setColor(Color.blue);

		if (getHighest().isBotommedToFitSystem()) { surface.drawString("8va", x, y - 4 * getParentStaff().parentSheetMusic.dy()); }
		surface.setColor(Color.black);
		
		Boolean oneOctavaLower = this.getHighest().isBotommedToFitSystem();
		for (Nota nota: getNotaList()) {
			int notaY = y + getLowestPossibleNotaRelativeY() - Settings.getStepHeight() * nota.getAbsoluteAcademicIndex();
			notaY += oneOctavaLower ? 7 * getParentStaff().parentSheetMusic.dy() : 0;
			nota.drawOn(surface, x, notaY);
			if (nota.isStriked() != oneOctavaLower) {
				List<Integer> p = Fp.vectorSum(nota.getTraitCoordinates(), Arrays.asList(x, notaY, x, notaY));
				surface.drawLine(p.get(0), p.get(1), p.get(2), p.get(3)); 
			}
			if (nota == this.getFocusedNota()) {
				List<Integer> p = nota.getAncorPoint();
				int r = Settings.inst().getStepHeight();
				surface.fillOval(x + p.get(0) + r * 2, notaY + p.get(1) - r, r * 2, r * 2);
			}
		}
		surface.drawString(this.getSlog(), x, y + Constants.FONT_HEIGHT);
	}

	// responsees to events (actions)
	
	public void triggerTuplets(int denominator) {
		if (getFocusedNota() != null) {
			getFocusedNota().setTupletDenominator(getFocusedNota().getTupletDenominator() == 1 ? denominator : 1);
		} else {
			for (Nota nota: this.getNotaList()) {
				nota.setTupletDenominator(nota.getTupletDenominator() == 1 ? denominator : 1);
			}
		}
	}

	public void moveFocus(int n) {
		if (this.getFocusedIndex() + n > this.getNotaList().size() - 1) {
			this.setFocusedIndex(-1);
		} else {
			this.setFocusedIndex(this.getFocusedIndex() + n);
		}
	}

	// getters/setters

	public int getWidth() {
		return Math.max(this.getSlog().length() * Constants.FONT_WIDTH, this.getEarliest().getWidth());
	}

	// implements(Pointerable)
	public int getTakenStepCount() {
//		int width = (int)Math.ceil( this.slog.length() * Constants.FONT_WIDTH / (Constants.STEP_H * 2) );
//		return width > 0 ? width : 1;
		return 2;
	}

	public int getHeight() {
		return this.getLowestPossibleNotaRelativeY();
	}

	public ArrayList<Nota> getNotaList() {
		return this.notaList;
	}

	public Nota getEarliest() {
		return this.getNotaList().stream().reduce(null, (a, b) -> a != null && a.keydownTimestamp < b.keydownTimestamp ? a : b);
	}

	public Nota getHighest() {
		return this.getNotaList().stream().reduce(null, (a, b) -> a != null && a.tune > b.tune ? a : b);
	}

	public Nota getShortest() {
		return this.getNotaList().stream().reduce(null, (a, b) -> a != null && !a.isLongerThan(b) ? a : b);
	}

	public Nota getFocusedNota() {
		return getFocusedIndex() > -1 ? this.getNotaList().get(getFocusedIndex()) : null;
	}

	public int getLowestPossibleNotaRelativeY () {
		return 50 * getParentStaff().parentSheetMusic.dy();
	}

	// field getters/setters

	public Staff getParentStaff() {
		return (Staff)this.getParent();
	}
	
	public String getSlog() {
		return this.slog;
	}

	public Accord setSlog(String value) {
		this.slog = value;
		return this;
	}

	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public Accord setFocusedIndex(int value) {
		value = value >= this.getNotaList().size() ? this.getNotaList().size() - 1 : value;
		value = value < -1 ? -1 : value;
		this.focusedIndex = value;
		return this;
	}

	@Override
	public List<? extends AbstractModel> getChildList() {
		return this.getNotaList();
	}

	@Override
	public AbstractModel getFocusedChild() {
		return this.getFocusedNota();
	}

	@Override
	protected Boolean undoFinal() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected Boolean redoFinal() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
