package Storyspace.Music.Staff.StaffConfig;

import Gui.ImageStorage;
import Gui.Settings;
import Model.AbstractHandler;
import Model.AbstractModel;
import Storyspace.Music.MusicPanel;
import Stuff.Midi.DeviceEbun;
import Storyspace.Music.Staff.Staff;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

public class StaffConfig extends AbstractModel {

	public int valueTempo = 120; // quarter beats per minute
	public int numerator = 8;

	// maybe create Channel class if we get more properties
	private int[] instrumentArray = {0, 65, 66, 43, 19, 52, 6, 91, 9, 14};
	private int[] volumeArray = {60, 60, 60, 60, 60, 60, 60, 60, 60, 60};
	private Boolean[] muteFlagArray = {false, false, false, false, false, false, false, false, false, false}; // not stored in file for now

	public StaffConfig(Staff staff) {
		super(staff);
	}

	public ConfigDialog getDialog() { return new ConfigDialog(this); }

	public Fraction getTactSize() {
		int tactNumerator = numerator * 8;
		int tactDenominator = Staff.DEFAULT_ZNAM;
		return new Fraction(tactNumerator, tactDenominator);
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

	public void syncSyntChannels() {
		ShortMessage instrMess = new ShortMessage();
		try {
			for (int i = 0; i < 10; ++i) {
				instrMess.setMessage(ShortMessage.PROGRAM_CHANGE, i, this.instrumentArray[i], 0);
				DeviceEbun.theirReceiver.send(instrMess, -1);
			}
		} catch (InvalidMidiDataException exc) { System.out.println("Midi error, could not sync channle instruments!"); }
	}

	@Override
	public void drawOn(Graphics g, int xIndent, int yIndent) {
		int dX = Settings.getNotaWidth()/5, dY = Settings.getNotaHeight()*2;
		g.drawImage(this.getImage(), xIndent - dX, yIndent - dY, null);
	}

	public BufferedImage getImage() {
		MusicPanel sheet = this.getParentStaff().getParentSheet();
		int w = Settings.getNotaWidth() * 5;
		int h = Settings.getNotaHeight() * 6;
		BufferedImage rez = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = rez.getGraphics();
		g.setColor(Color.black);

		int tz=8, tc = numerator;
		while (tz>4 && tc%2==0) {
			tz /= 2;
			tc /= 2;
		}
		int inches = Settings.getNotaHeight()*5/8, taktX= 0, taktY=Settings.getNotaHeight()*2; // 25, 80
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, inches)); // 12 - 7px width
		g.drawString(tc+"", 0 + taktX, inches*4/5 + taktY);
		int delta = 0 + (tc>9 && tz<10? inches*7/12/2: 0) + ( tc>99 && tz<100?inches*7/12/2:0 );
		g.drawString(tz+"", delta + taktX, 2*inches*4/5 + taktY);

		int tpx = 0, tpy = 0;
		g.drawImage(ImageStorage.inst().getQuarterImage(), tpx, tpy, null);
		inches = Settings.getNotaHeight()*9/20;
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, inches)); // 12 - 7px width
		g.drawString(" = "+valueTempo, tpx + Settings.getNotaWidth()*4/5, tpy + inches*4/5 + Settings.getNotaHeight()*13/20);

		return rez;
	}

	@Override
	public AbstractModel getFocusedChild() {
		return null;
	}
	@Override
	protected AbstractHandler makeHandler() { return new AbstractHandler(this) {}; }

	// field getters
	
	public Staff getParentStaff() {
		return (Staff)this.getModelParent();
	}
	public int[] getInstrumentArray() {
		return this.instrumentArray;
	}
	public int[] getVolumeArray() { return this.volumeArray; }
	public Boolean[] getMuteFlagArray() { return this.muteFlagArray; }

	public int getVolume(int channel) { return muteFlagArray[channel] ? 0 : volumeArray[channel]; }
}
