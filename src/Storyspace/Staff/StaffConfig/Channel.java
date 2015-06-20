package Storyspace.Staff.StaffConfig;

import Model.AbstractModel;
import Model.Field.Field;

public class Channel extends AbstractModel {

	private Field<Integer> instrument = h.addField("instrument", 0);
	private Field<Integer> volume = h.addField("volume", 60);
	private Field<Boolean> isMuted = h.addField("isMuted", false);

	public Channel(StaffConfig parent) { super(parent); }

	public Channel setInstrument(int value) { instrument.set(value); return this; }
	public Channel setVolume(int value) { volume.set(value); return this; }
	public Channel setIsMuted(Boolean value) { isMuted.set(value); return this; }

	public int getInstrument() { return instrument.get(); }
	public int getVolume() { return volume.get(); }
	public Boolean getIsMuted() { return isMuted.get(); }
}
