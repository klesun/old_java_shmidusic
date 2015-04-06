package Model.Staff.StaffConfig;

import Gui.Settings;
import Model.AbstractModel;
import Model.Staff.Accord.Nota.Nota;
import Gui.SheetPanel;
import Midi.DeviceEbun;
import Model.Staff.Staff;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

public class StaffConfig extends AbstractModel {

	public int valueTempo = 120;
	public int numerator = 8;

	private int[] instrumentArray = {64, 65, 66, 43, 19, 52, 6, 91, 9, 14};
	private int[] volumeArray = {60, 60, 60, 60, 60, 60, 60, 60, 60, 60};

	public StaffConfig(Staff staff) {
		super(staff);
	}

	@Override
	public void drawOn(Graphics g, int xIndent, int yIndent) {
		int dX = Settings.getNotaWidth()/5, dY = Settings.getNotaHeight()*2;
		g.drawImage(this.getImage(), xIndent - dX, yIndent - dY, getSheetPanel());
		int deltaY = 0, deltaX = 0;
		switch (changeMe) {
			case numerator:	deltaY += 9 * dy(); break;
			case tempo: deltaY -= 1 * dy(); break;
			default: break;
		}
		if (getParentStaff().getFocusedChild() == this) {
			g.drawImage(getParentStaff().getPointerImage(), xIndent - 7* Settings.getNotaWidth()/25 + deltaX, yIndent - dy() * 14 + deltaY, getSheetPanel());
		}
	}

	public BufferedImage getImage() {
		SheetPanel sheet = this.getParentStaff().getParentSheet();
		int w = sheet.getNotaWidth() * 5;
		int h = sheet.getNotaHeight() * 6;
		BufferedImage rez = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = rez.getGraphics();
		g.setColor(Color.black);

		int tz=8, tc = numerator;
		while (tz>4 && tc%2==0) {
			tz /= 2;
			tc /= 2;
		}
		int inches = sheet.getNotaHeight()*5/8, taktX= 0, taktY=sheet.getNotaHeight()*2; // 25, 80
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, inches)); // 12 - 7px width
		g.drawString(tc+"", 0 + taktX, inches*4/5 + taktY);
		int delta = 0 + (tc>9 && tz<10? inches*7/12/2: 0) + ( tc>99 && tz<100?inches*7/12/2:0 );
		g.drawString(tz+"", delta + taktX, 2*inches*4/5 + taktY);

		int tpx = 0, tpy = 0;
		g.drawImage(Nota.notaImg[3], tpx, tpy, null);
		inches = sheet.getNotaHeight()*9/20;
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, inches)); // 12 - 7px width
		g.drawString(" = "+valueTempo, tpx + sheet.getNotaWidth()*4/5, tpy + inches*4/5 + sheet.getNotaHeight()*13/20);

		return rez;
	}

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject dict = new JSONObject();
		dict.put("tempo", this.valueTempo);
		dict.put("numerator", this.numerator);
		dict.put("instrumentArray", new JSONArray(this.instrumentArray));
		dict.put("volumeArray", new JSONArray(this.volumeArray));

		return dict;
	}

	@Override
	public StaffConfig reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.valueTempo = jsObject.getInt("tempo");
		if (jsObject.has("numerator")) { // TODO: [deprecated], it should be always true
			this.numerator = jsObject.getInt("numerator");
		}
		if (jsObject.has("instrumentArray")) { // TODO: [deprecated], it should be always true one day
			JSONArray jsArray = jsObject.getJSONArray("instrumentArray");
			for (int i = 0; i < 10; ++i) { this.instrumentArray[i] = jsArray.getInt(i); }
		}
		if (jsObject.has("volumeArray")) { // TODO: [deprecated], it should be always true one day
			JSONArray jsArray = jsObject.getJSONArray("volumeArray");
			for (int i = 0; i < 10; ++i) { this.volumeArray[i] = jsArray.getInt(i); }
		}

		syncSyntChannels();
		return this;
	}

	@Override
	public List<? extends AbstractModel> getChildList() {
		return new ArrayList<>();
	}

	@Override
	public AbstractModel getFocusedChild() {
		return null;
	}

	@Override
	protected StaffConfigHandler makeHandler() {
		return new StaffConfigHandler(this);
	}

	// getters


	public int dx() {
		return Settings.getStepWidth();
	}

	public int dy() {
		return Settings.getStepHeight();
	}

	public SheetPanel getSheetPanel() {
		return getParentStaff().getParentSheet();
	}

	// field getters
	
	public Staff getParentStaff() {
		return (Staff)this.getParent();
	}
	public int[] getInstrumentArray() {
		return this.instrumentArray;
	}
	public int[] getVolumeArray() {
		return this.volumeArray;
	}

	public void syncSyntChannels() {
		ShortMessage instrMess = new ShortMessage();
		try {
			for (int i = 0; i < 10; ++i) {
				instrMess.setMessage(ShortMessage.PROGRAM_CHANGE, i, this.instrumentArray[i], 0);
				DeviceEbun.theirReceiver.send(instrMess, -1);
			}
		} catch (InvalidMidiDataException exc) { System.out.println("Midi error, could not sync channle instruments!"); }
	}

	// staff

	public enum WhatToChange {
		numerator,
		tempo,
	}
	public WhatToChange changeMe = WhatToChange.numerator;

	public void chooseNextParam() {
		switch (changeMe) {
			case numerator:
				changeMe = WhatToChange.tempo;
				break;
			case tempo:
				changeMe = WhatToChange.numerator;
				break;
			default:
				changeMe = WhatToChange.numerator;
				System.out.println("Неизвестный енум");
				break;
		}
	}

	public int tryToWrite( char c ) {
		if (c < '0' || c > '9') return -1;
		switch (changeMe) {
			case tempo:
				valueTempo *= 10;
				valueTempo += c - '0';
				valueTempo %= 12000;
				break;
			default:
				System.out.println("Неизвестный енум");
				break;
		}
		return 0;
	}

	public void changeValue(int n) {
		switch (changeMe) {
			case numerator:
				numerator += n;
				numerator = numerator < 1 ? 1 : numerator % 257;
				break;
			case tempo:
				valueTempo += n;
				valueTempo = valueTempo < 1 ? 1 : valueTempo % 12000;
				break;
			default: break;
		}
	}

	public void backspace() {
		switch (changeMe) {
			case tempo:
				valueTempo /= 10;
				if (valueTempo < 1) valueTempo = 1;
				break;
			default:
				System.out.println("Неизвестный енум");
				break;
		} // switch(enum)
	}
}
