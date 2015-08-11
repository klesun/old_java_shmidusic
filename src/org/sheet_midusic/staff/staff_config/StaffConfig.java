package org.sheet_midusic.staff.staff_config;

import org.sheet_midusic.staff.staff_panel.StaffComponent;
import org.sheet_midusic.stuff.graphics.ImageStorage;
import org.sheet_midusic.stuff.graphics.Settings;
import org.sheet_midusic.stuff.graphics.ShapeProvider;
import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;
import org.sheet_midusic.staff.MidianaComponent;
import org.json.JSONArray;
import org.sheet_midusic.stuff.Midi.DeviceEbun;
import org.sheet_midusic.staff.Staff;

import java.awt.*;
import java.util.*;
import java.util.function.BiConsumer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.apache.commons.math3.fraction.Fraction;

import org.json.JSONException;
import org.json.JSONObject;

public class StaffConfig extends AbstractModel
{
	final private static int MIN_TEMPO = 15; // i doubt you would need longer. with this 1/16 lasts one second
	final private static int MAX_TEMPO = 480; // i hope no one needs more

	final private static int MAX_TACT_NUMERATOR = 32; // four whole notas

	// tempo 60 => quarter nota = 1 second

	// TODO: use Fraction
	final private Field<Integer> numerator = new Field<>("numerator", 8, this, n -> limit(n, 1, MAX_TACT_NUMERATOR)); // h.addField("numerator", 8); // because 8x8 = 64; 64/64 = 1; obvious
	final private Field<Integer> tempo = new Field<>("tempo", 120, this, n -> limit(n, MIN_TEMPO, MAX_TEMPO)); // h.addField("tempo", 120);
	final public Field<Integer> keySignature = addField("keySignature", 0);
	final public Field<Boolean> useHardcoreSynthesizer = addField("useHardcoreSynthesizer", false);

	final private Staff staff;

	// TODO: make it ordered Set instead of List
	final public Arr<Channel> channelList = new Arr<>("channelList", makeChannelList(), this, Channel.class).setOmitDefaultFromJson(true);

	private TreeSet<Channel> makeChannelList() {
		TreeSet<Channel> list = new TreeSet<>();

		for (int i = 0; i < Channel.CHANNEL_COUNT; ++i) {
			JSONObject state = new JSONObject().put("channelNumber", i);
			Channel channel = new Channel();
			channel.reconstructFromJson(state);

			list.add(channel);
		}

		return list;
	}

	public StaffConfig(Staff staff) {
		this.staff = staff;
	}

	public ConfigDialog getDialog() { return new ConfigDialog(this); }

	// temporary solution
	public StaffConfigComponent makeComponent(StaffComponent parentForComponent)
	{
		return new StaffConfigComponent(parentForComponent, this);
	}

	public Fraction getTactSize() {
		int tactNumerator = getNumerator() * 8;
		int tactDenominator = Staff.DEFAULT_ZNAM;
		return new Fraction(tactNumerator, tactDenominator);
	}

	@Override
	public StaffConfig reconstructFromJson(JSONObject jsObject) throws JSONException {

		// TODO: temporary hack for compatibility with old files, that does not have this final field
		if (jsObject.has("channelList")) {
			JSONArray channelArray = jsObject.getJSONArray("channelList");
			if (channelArray.length() > 0 && !channelArray.getJSONObject(0).has("channelNumber")) {
				for (int i = 0; i < channelArray.length(); ++i) {
					channelArray.put(i, channelArray.getJSONObject(i).put("channelNumber", i));
				}
			}
			jsObject.put("channelList", channelArray);
		}

		super.reconstructFromJson(jsObject);

		TreeSet<Channel> resultChannelSet = this.makeChannelList();
		for (Channel channelFromJson: channelList.get()) {
			resultChannelSet.remove(channelFromJson); // =D
			resultChannelSet.add(channelFromJson); // =D
			// cuz i wanna overwrite old key
		}

		this.channelList.set(resultChannelSet);

		syncSyntChannels();
		return this;
	}

	public void syncSyntChannels() {
		ShortMessage instrMess = new ShortMessage();
		try {
			for (int i = 0; i < getChannelList().size(); ++i) {
				instrMess.setMessage(ShortMessage.PROGRAM_CHANGE, i, channelList.get(i).getInstrument(), 0);
				DeviceEbun.getPlaybackReceiver().send(instrMess, -1);
			}
		} catch (InvalidMidiDataException exc) { System.out.println("Midi error, could not sync channel instruments!"); }
	}

	public static void syncSyntChannels(AbstractModel c) { ((StaffConfig)c).syncSyntChannels(); }

	public void drawOn(Graphics2D g, int xIndent, int yIndent, Boolean completeRepaint) {
		int dX = dx()/5, dY = Settings.inst().getNotaHeight() * 2;
		drawImage(g, xIndent - dX, yIndent - dY);

		drawSignature(g, xIndent + dX / 4, yIndent);
	}

	private void drawSignature(Graphics2D g, int x, int y)
	{
		KeySignature siga = new KeySignature(keySignature.get());

		ShapeProvider shaper = new ShapeProvider(Settings.inst(), g, ImageStorage.inst());

		BiConsumer<Integer, Integer> paintEbony = keySignature.get() > 0
				? shaper::drawSharpSign
				: shaper::drawFlatSign;

		int doPositionY = y + 10 * dy(); // y is toppest Staff line
		int i = 0;
		for (int ivory: siga.getAffectedIvorySet()) {
			int positionY = doPositionY - ivory * dy();

			if (ivory < KeySignature.SO) {
				positionY -= 7 * dy();
			}

			// dealing with them covering one another
			int xShift = i * dx() / 2;
			if (i > 3) { xShift -= dx(); }

			paintEbony.accept(x + xShift, positionY);

			++i;
		}
	}

	public void drawImage(Graphics2D g, int x, int y)
	{
		g.setColor(Color.black);
		int inches = Settings.inst().getNotaHeight()*5/8, taktY = Settings.inst().getNotaHeight()*2; // 25, 80
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, inches)); // 12 - 7px width

		int tz = 8, tc = getNumerator(); // tz - denominator, tc - numerator
		while (tz>4 && tc%2==0) {
			tz /= 2;
			tc /= 2;
		}

		g.drawString(tc+"", x - dx() / 2, y + inches*4/5 + taktY);
		g.drawString(tz+"", x - dx() / 2, y + 2 * inches*4/5 + taktY);

		int tpx = x, tpy = y + dy() * 2;
		g.drawImage(ImageStorage.inst().getQuarterImage(), tpx, tpy, null);
		inches = Settings.inst().getNotaHeight() * 9/20;
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, inches)); // 12 - 7px width
		g.drawString(" = " + getTempo(), tpx + dx() * 4 / 5, tpy + inches * 4 / 5 + Settings.inst().getNotaHeight() * 13 / 20);
	}

	final private int dx() { return Settings.inst().getStepWidth(); }
	final private int dy() { return Settings.inst().getStepHeight(); }

	// field getters

	public Integer getTempo() { return this.tempo.get(); }
	public StaffConfig setTempo(int value) { this.tempo.set(value); return this; }
	public Integer getNumerator() { return this.numerator.get(); }
	public StaffConfig setNumerator(int value) { this.numerator.set(value); return this; }
	public TreeSet<Channel> getChannelList() { return (TreeSet<Channel>)this.channelList.get(); }

	public KeySignature getSignature() {
		return new KeySignature(keySignature.get());
	}

	public int getVolume(int channel) {
		Channel chan = channelList.get(channel);
		return chan.getIsMuted() ? 0 : chan.getVolume();
	}
}
