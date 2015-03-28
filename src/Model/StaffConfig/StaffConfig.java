package Model.StaffConfig;

import Model.Accord.Nota.Nota;
import Gui.SheetMusic;
import Midi.DeviceEbun;
import Model.Staff;
import Model.IModel;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.LinkedHashMap;

import javax.sound.midi.InvalidMidiDataException;

import org.json.JSONException;
import org.json.JSONObject;

public class StaffConfig implements IModel {	

	public Staff parentStaff;

	public int valueTempo = 120;
	public int valueInstrument = 0;
	public double valueVolume = 0.5;
	public int numerator = 8;
	public int znamen = Staff.DEFAULT_ZNAM;

	public StaffConfig(Staff staff) {
		this.parentStaff = staff;
		znamen = 8;
		numerator = 8;
	}

	public BufferedImage getImage() {
		SheetMusic sheet = this.getParentStaff().parentSheetMusic;
		int w = sheet.getNotaWidth() * 5;
		int h = sheet.getNotaHeight() * 6;
		BufferedImage rez = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = rez.getGraphics();
		g.setColor(Color.black);

		int tz=znamen, tc = numerator;
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

		// instrument
		tpy = sheet.getNotaHeight();
		g.drawImage(SheetMusic.vseKartinki[5], 0, tpy, null);
		g.setFont(new Font(Font.SERIF, Font.BOLD, inches)); // 12 - 7px width
		g.setColor(Color.decode("0xaa00ff"));
		g.drawString(" "+valueInstrument, 0 + sheet.getNotaWidth()*3/5, tpy + inches*4/5 + sheet.getNotaHeight()*11/20);

		// Volume
		tpx = 0; tpy = sheet.getNotaHeight()*37/10;
		g.drawImage(SheetMusic.vseKartinki[4], tpx+sheet.getNotaWidth()*2/25, tpy, null);
		inches = sheet.getNotaHeight()*3/10;
		g.setColor(Color.decode("0x00A13E"));
		g.drawString((int)(valueVolume*100)+"%", tpx, tpy + inches*4/5 + sheet.getNotaHeight()*2/5);

		return rez;
	}

	@Override
	public LinkedHashMap<String, Object> getJsonRepresentation() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<String, Object>();
		dict.put("tempo", this.valueTempo);
		dict.put("volume", this.valueVolume);
		dict.put("instrument", this.valueInstrument);
		dict.put("denominator", this.znamen);

		return dict;
	}

	@Override
	public StaffConfig reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.valueTempo = jsObject.getInt("tempo");
		this.valueVolume = jsObject.getDouble("volume");
		this.valueInstrument = jsObject.getInt("instrument");
		this.znamen = jsObject.getInt("denominator");

		return this;
	}

	public StaffConfig update(StaffConfig rival) throws JSONException {
		JSONObject js = new JSONObject("{}"); 
		js = new JSONObject(js.put("lol", rival.getJsonRepresentation()).get("lol").toString());
		this.reconstructFromJson(js);
		return this;
	}

	// field getters
	
	public Staff getParentStaff() {
		return this.parentStaff;
	}

	public enum WhatToChange {
		numerator,
		tempo,
		volume,
		instrument;
	}
	public WhatToChange changeMe = WhatToChange.numerator;

	public void chooseNextParam() {
		switch (changeMe) {
			case numerator:
				changeMe = WhatToChange.tempo;
				break;
			case tempo:
				changeMe = WhatToChange.volume;
				break;
			case volume:
				changeMe = WhatToChange.instrument;
				break;
			case instrument:
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
			case instrument:
				valueInstrument *= 10;
				valueInstrument += c - '0';
				valueInstrument %= 256;
				break;
			case volume:
				valueVolume *= 10;
				System.out.println("c="+c+" c-'0'="+(c-'0'));
				valueVolume += ((double)(c-'0'))/100;
				if (valueVolume > 2.54) valueVolume = 2.54;
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
			case instrument:
				this.valueInstrument += n;
				this.valueInstrument = valueInstrument < 0 ? 0 : valueInstrument % 256;
				try {
					DeviceEbun.changeInstrument(this.valueInstrument);
				} catch (InvalidMidiDataException e) { System.out.println("Сука инструмент менять нихуя не получается"); }
				break;
			case volume:
				valueVolume += ((double)n)/100;
				if (valueVolume < 0) valueVolume = 0;
				if (valueVolume > 2.54) valueVolume = 2.54;
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
			case volume:
				int tmp = (int)(valueVolume*100);
				tmp /= 10;
				valueVolume = ((double)tmp)/100;
				break;
			case instrument:
				valueInstrument /= 10;
				break;
			default:
				System.out.println("Неизвестный енум");
				break;
		} // switch(enum)
	}
}
