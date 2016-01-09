package org.shmidusic.sheet_music.staff.staff_config;

import org.klesun_model.AbstractModel;
import org.klesun_model.field.Field;
import org.json.JSONObject;
import org.klesun_model.field.IField;

public class Channel extends AbstractModel implements Comparable<Channel>  {

	final public static int CHANNEL_COUNT = 16; // 1-15. 0th ignores volume change; 16th throws MidiDataChannelOutOfRangeBlaBla exception

	// TODO: 0-th channel does not exist - do something with that
	final public Field<Integer> channelNumber = add("channelNumber", Integer.class);
	final private Field<Integer> instrument = add("instrument", 0, i -> limit(i, 0, 127)).setOmitDefaultFromJson(true);
	final private Field<Integer> volume = add("volume", 50, v -> limit(v, 0, 127)).setOmitDefaultFromJson(true);
	final private Field<Boolean> isMuted = add("isMuted", false);

	public Channel setInstrument(int value) { instrument.set(value); return this; }
	public Channel setVolume(int value) { volume.set(value); return this; }
	public Channel setIsMuted(Boolean value) { isMuted.set(value); return this; }

	public Integer getInstrument() { return instrument.get(); }
	public Integer getVolume() { return volume.get(); }
	public Boolean getIsMuted() { return isMuted.get(); }

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject result = super.getJsonRepresentation();
		if (result.keySet().size() == 1 && result.has("channelNumber")) {
			// no useful information
			return new JSONObject();
		} else {
			return result;
		}
	}

	@Override
	public int compareTo(Channel c) {
		return this.channelNumber.get() - c.channelNumber.get();
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Channel) && compareTo((Channel)o) == 0;
	}

	/** @override me please! */
	public Boolean mustBeStored() {
		return getFieldStorage().values().stream().filter(f -> f != channelNumber).anyMatch(IField::mustBeStored);
	}
}
