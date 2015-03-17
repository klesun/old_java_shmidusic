package Pointerable;

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
import Gui.DrawPanel;

public class Accord extends Pointerable implements IAccord { // TODO: remove this interface, once Nota does not store the accord

	ArrayList<Nota> notaList = new ArrayList<Nota>();
	String slog = "";

	// Implement
	public Accord add(Nota nota) {
		this.notaList.add(nota);
		return this;
	}

	// Implement
	public LinkedHashMap<String, Object> getExternalRepresentationSuccessed() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<String, Object>();
		dict.put("notaList", this.notaList.stream().map(n -> n.getExternalRepresentation()).toArray());
		dict.put("slog", this.slog);
		return dict;
	}

	@Override
	public Accord reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.slog = jsObject.getString("slog");
		JSONArray notaJsonList = jsObject.getJSONArray("notaList");
		for (int idx = 0; idx < notaJsonList.length(); ++idx) {
			JSONObject childJs = notaJsonList.getJSONObject(idx);
			this.add((new Nota(63)).reconstructFromJson(childJs));
		}

		return this;
	}

	@Override
	public BufferedImage getImage() {
		BufferedImage img = new BufferedImage(DrawPanel.STEPX * 2, DrawPanel.STEPY * 14, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();

		// TODO: not finished
		for (Nota nota: this.notaList) {
			int yIndent = DrawPanel.STEPY * nota.getAcademicIndex() + nota.getOctava() * 7;
			g.drawImage(nota.getImage(), 0, yIndent, null);
			
			// TODO: lame, it should be in Nota.getImage
//			if (nota.isBemol) {
//				g.drawImage(vseKartinki[2], gPos-(int)Math.round(0.5* STEPX), thisY + 3* STEPY +2, this);
//			}

//			if (theNota.cislic  % 3 == 0) g.fillOval(gPos + notaWidth*4/5, thisY + notaHeight*7/8, notaHeight/8, notaHeight/8);
		}
		return img;
	}

	@Override
	public void changeDur(int i, boolean b) {
		// should not be used, cause interface is not ready for accords
	}

	// getters/setters

	// implements(IAccord)
	public String getSlog() {
		return this.slog;
	}

	// implements(IAccord)
	public IAccord setSlog(String value) {
		this.slog = value;
		return this;
	}

	// implements(Pointerable)
	public int getWidth() {
		int width = (int)Math.ceil( this.slog.length() * Constants.FONT_WIDTH / (Constants.STEP_H * 2) );
		return width > 0 ? width : 1;
	}

	// implements(IAccord)
	public ArrayList<Nota> getNotaList() {
		return this.notaList;
	}

	// implements(IAccord)
	public Nota getEarliest() {
		return this.getNotaList().stream().reduce(null, (a, b) -> a != null && a.keydownTimestamp < b.keydownTimestamp ? a : b);
	}

	public Nota getHighest() {
		return this.getNotaList().stream().reduce(null, (a, b) -> a != null && a.tune > b.tune ? a : b);
	}

	public Nota getShortest() {
		return this.getNotaList().stream().reduce(null, (a, b) -> a != null && !a.isLongerThan(b) ? a : b);
	}

	public void setNotaList(ArrayList<Nota> notaList) {
		this.notaList = notaList;
	}
}
