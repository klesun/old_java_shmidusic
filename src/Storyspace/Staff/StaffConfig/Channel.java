package Storyspace.Staff.StaffConfig;

import Model.AbstractModel;
import Model.Field.ModelField;
import Model.IComponentModel;

public class Channel extends AbstractModel {

	private ModelField<Integer> instrument = h.addField("instrument", 0);
	private ModelField<Integer> volume = h.addField("volume", 0);
	private ModelField<Boolean> isMuted = h.addField("isMuted", false);

	public Channel(StaffConfig parent) { super(parent); }

	public Channel setInstrument(int value) { instrument.setValue(value); return this; }
	public Channel setVolume(int value) { volume.setValue(value); return this; }
	public Channel setIsMuted(Boolean value) { isMuted.setValue(value); return this; }

	public int getInstrument() { return instrument.getValue(); }
	public int getVolume() { return volume.getValue(); }
	public Boolean getIsMuted() { return isMuted.getValue(); }
}
