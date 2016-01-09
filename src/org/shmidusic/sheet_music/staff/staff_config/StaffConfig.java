package org.shmidusic.sheet_music.staff.staff_config;

import org.shmidusic.sheet_music.staff.StaffComponent;
import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;
import org.json.JSONArray;
import org.shmidusic.stuff.midi.DeviceEbun;
import org.shmidusic.sheet_music.staff.Staff;

import java.util.*;

import org.apache.commons.math3.fraction.Fraction;

import org.json.JSONException;
import org.json.JSONObject;

public class StaffConfig extends AbstractModel
{
	final private static int MIN_TEMPO = 15; // i doubt you would need longer. with this 1/16 lasts one second
	final private static int MAX_TEMPO = 480; // i hope no one needs more

	final private static int MAX_TACT_NUMERATOR = 32; // four whole notes

	// tempo 60 => quarter note = 1 second
    // tempo 240 => note fraction = second fraction

	// TODO: use Fraction
	final private Field<Integer> numerator = add("numerator", 8, n -> limit(n, 1, MAX_TACT_NUMERATOR)); // because 8x8 = 64; 64/64 = 1; obvious
	final private Field<Integer> tempo = add("tempo", 120, n -> limit(n, MIN_TEMPO, MAX_TEMPO));
	final public Field<Integer> keySignature = add("keySignature", 0);
	final public Field<Boolean> useHardcoreSynthesizer = add("useHardcoreSynthesizer", false);

	final public Arr<Channel> channelList = add("channelList", makeChannelList(), Channel.class).setOmitDefaultFromJson(true);

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

	public ConfigDialog getDialog() { return new ConfigDialog(this); }

	// temporary solution
	public StaffConfigComponent makeComponent(StaffComponent parentForComponent)
	{
		return new StaffConfigComponent(parentForComponent, this);
	}

    // TODO: MUAHHH! recalcTacts on change!!!!
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
					channelArray.put(i, channelArray.getJSONObject(i).put("channelNumber", i + 1));
				}
			}
			jsObject.put("channelList", channelArray);
		}

		super.reconstructFromJson(jsObject);

		TreeSet<Channel> resultChannelSet = this.makeChannelList();
		for (Channel channelFromJson: channelList) {
			resultChannelSet.remove(channelFromJson); // =D
			resultChannelSet.add(channelFromJson); // =D
			// cuz i wanna overwrite old key
		}

		this.channelList.setFromList(resultChannelSet);

		syncSyntChannels();
		return this;
	}

	public void syncSyntChannels() {
		for (int i = 0; i < Channel.CHANNEL_COUNT; ++i) {
			DeviceEbun.setInstrument(i, channelList.get(i).getInstrument());
			DeviceEbun.setVolume(i, channelList.get(i).getVolume());
		}
	}

	public static void syncSyntChannels(AbstractModel c) { ((StaffConfig)c).syncSyntChannels(); }

	// field getters

	public Integer getTempo() { return this.tempo.get(); }
	public StaffConfig setTempo(int value) { this.tempo.set(value); return this; }
	public Integer getNumerator() { return this.numerator.get(); }
	public StaffConfig setNumerator(int value) { this.numerator.set(value); return this; }

	public KeySignature getSignature() {
		return new KeySignature(keySignature.get());
	}
}
