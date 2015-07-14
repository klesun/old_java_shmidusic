package blockspace.staff.StaffConfig;

import model.AbstractModel;
import model.field.Field;

public class Channel extends AbstractModel {

	final public static int CHANNEL_COUNT = 16;

	private Field<Integer> instrument = new Field<>("instrument", 0, this, i -> limit(i, 0, 127)); // why store the constant here if may in ImageStorage =D
	private Field<Integer> volume = new Field<>("volume", 60, this, v -> limit(v, 0, 127));
	private Field<Boolean> isMuted = new Field<>("isMuted", false, this);

	public Channel(StaffConfig parent) { super(parent); }

	public Channel setInstrument(int value) { instrument.set(value); return this; }
	public Channel setVolume(int value) { volume.set(value); return this; }
	public Channel setIsMuted(Boolean value) { isMuted.set(value); return this; }

	public int getInstrument() { return instrument.get(); }
	public int getVolume() { return volume.get(); }
	public Boolean getIsMuted() { return isMuted.get(); }
}
