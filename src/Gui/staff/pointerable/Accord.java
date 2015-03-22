package Gui.staff.pointerable;

import Gui.staff.Pointer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Gui.Constants;
import Gui.staff.Staff;
import Gui.SheetMusic;
import Gui.staff.Staff;

public class Accord extends Pointerable {

	Staff parentStaff = null;

	ArrayList<Nota> notaList = new ArrayList<Nota>();
	String slog = "";

	public Accord(Staff parentStaff) {
		this.parentStaff = parentStaff;
	}

	public Accord add(Nota nota) {
		this.notaList.add(nota);
		return this;
	}

	public LinkedHashMap<String, Object> getObjectState() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<String, Object>();
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
			this.add((new Nota(63)).setObjectStateFromJson(childJs));
		}

		return this;
	}

	@Override
	public BufferedImage getImage() {
		SheetMusic sheet = this.parentStaff.parentSheetMusic;
		BufferedImage img = new BufferedImage(sheet.getStepWidth() * 2, sheet.getStepHeight() * 14, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();

		// TODO: not finished
		for (Nota nota: this.notaList) {
			int yIndent = sheet.getStepHeight() * nota.getAcademicIndex() + nota.getOctava() * 7;
			g.drawImage(nota.getImage(), 0, yIndent, null);
			
			// TODO: lame, it should be in Nota.getImage
//			if (nota.isBemol) {
//				g.drawImage(vseKartinki[2], gPos-(int)Math.round(0.5* STEPX), thisY + 3* STEPY +2, this);
//			}

//			if (theNota.cislic  % 3 == 0) g.fillOval(gPos + notaWidth*4/5, thisY + notaHeight*7/8, notaHeight/8, notaHeight/8);
		}
		return img;
	}
	
	// responsees to events (actions)
	
	public void triggerTuplets(int denominator) {
		if (getFocused() != null) {
			getFocused().setTupletDenominator(getFocused().getTupletDenominator() == 1 ? denominator : 1);
		} else {
			for (Nota nota: this.getNotaList()) {
				nota.setTupletDenominator(nota.getTupletDenominator() == 1 ? denominator : 1);
			}
		}
	}

	@Override
	public void changeDur(int i, boolean b) {
		// should not be used, cause interface is not ready for accords
	}

	// getters/setters

	public String getSlog() {
		return this.slog;
	}

	public Accord setSlog(String value) {
		this.slog = value;
		return this;
	}

	// implements(Pointerable)
	public int getWidth() {
		int width = (int)Math.ceil( this.slog.length() * Constants.FONT_WIDTH / (Constants.STEP_H * 2) );
		return width > 0 ? width : 1;
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

	public Nota getFocused() {
		return getFocusedIndex() > -1 ? this.getNotaList().get(getFocusedIndex()) : null;
	}

	public int getFocusedIndex() {
		return Pointer.nNotiVAccorde;
	}

	public void setNotaList(ArrayList<Nota> notaList) {
		this.notaList = notaList;
	}
}
