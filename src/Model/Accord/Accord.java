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
import Model.Staff;
import Gui.SheetMusic;
import Model.Accord.Nota.Nota;
import Model.Staff;
import Tools.Fp;
import Tools.IModel;
import java.util.Arrays;
import java.util.List;

public class Accord implements IModel {

	public Staff parentStaff = null;

	ArrayList<Nota> notaList = new ArrayList<Nota>();
	String slog = "";

	int focusedIndex = -1;
	BufferedImage surface = null;
	Boolean surfaceChanged = true;

	// TODO: deprecated
	public Accord prev = null;
	public Accord next = null;

	public Accord(Staff parentStaff) {
		this.parentStaff = parentStaff;
	}

	public Accord add(Nota nota) {
		this.notaList.add(nota);
		nota.parentAccord = this;
		requestNewSurface();
		return this;
	}

	@Override
	// TODO: maybe instead of LinkedHashMap use JSONArray from the very begining?
	public LinkedHashMap<String, Object> getObjectState() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<>();
		dict.put("notaList", this.notaList.stream().map(n -> n.getObjectState()).toArray());
		dict.put("slog", this.slog);
		return dict;
	}

	@Override
	public Accord setObjectStateFromJson(JSONObject jsObject) throws JSONException {
		this.slog = jsObject.getString("slog");
		JSONArray notaJsonList = jsObject.getJSONArray("notaList");
		for (int idx = 0; idx < notaJsonList.length(); ++idx) {
			JSONObject childJs = notaJsonList.getJSONObject(idx);
			this.add((new Nota(this)).setObjectStateFromJson(childJs));
		}

		return this;
	}

	public BufferedImage getImage() {
		if (this.surfaceChanged) {
			this.recalcSurface();
			this.surfaceChanged = false;
		}
		return this.surface;
	}
 
	private void recalcSurface() {
		this.surface = new BufferedImage(this.getWidth(), this.getHeight() + 5, BufferedImage.TYPE_INT_ARGB);
		Graphics surface = this.surface.getGraphics();
		surface.setColor(Color.blue);

		if (getHighest().isBotommedToFitSystem()) { surface.drawString("8va", 0, 0 - 4 * parentStaff.parentSheetMusic.getStepHeight()); }
		surface.setColor(Color.black);
		
		Boolean oneOctavaLower = this.getHighest().isBotommedToFitSystem();
		for (Nota nota: getNotaList()) {
			// TODO: draw some arrow near focused nota
			int notaY = this.getLowestPossibleNotaY() - parentStaff.parentSheetMusic.getStepHeight() * (nota.getAcademicIndex() + nota.getOctava() * 7);
			notaY += oneOctavaLower ? 7 * parentStaff.parentSheetMusic.getStepHeight() : 0;
			surface.drawImage(nota.getImage(this.getFocusedNota() == nota), 0, notaY, null);
			if (oneOctavaLower ^ nota.isStriked()) {
				List<Integer> p = Fp.vectorSum(nota.getTraitCoordinates(), Arrays.asList(0, notaY, 0, notaY));
				surface.drawLine(p.get(0), p.get(1), p.get(2), p.get(3)); 
			}
			if (nota == this.getFocusedNota()) {
				List<Integer> p = nota.getAncorPoint();
				int r = Settings.inst().getStepHeight();
				surface.fillOval(p.get(0) + r * 2, notaY + p.get(1) - r, r * 2, r * 2);
			}
		}

		surface.drawString(this.getSlog(), 0, 0 + Constants.FONT_HEIGHT);
	}

	public Accord requestNewSurface() {
		this.surfaceChanged = true;
		parentStaff.requestNewSurface();
		return this;
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

	public void changeLength(int n) {
		// TODO: -_-
		requestNewSurface();
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
		return this.getLowestPossibleNotaY();
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

	public int getLowestPossibleNotaY () {
		return 50 * parentStaff.parentSheetMusic.getStepHeight();
	}

	// field getters/setters
	
	public String getSlog() {
		return this.slog;
	}

	public Accord setSlog(String value) {
		this.slog = value;
		requestNewSurface();
		return this;
	}

	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public Accord setFocusedIndex(int value) {
		value = value >= this.getNotaList().size() ? this.getNotaList().size() - 1 : value;
		value = value < -1 ? -1 : value;
		this.focusedIndex = value;
		requestNewSurface();
		return this;
	}
}
