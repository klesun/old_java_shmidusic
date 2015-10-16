package org.shmidusic.stuff.graphics;

// TODO: maybe move it into ImageStorage
public class Settings {

//	final private BlockSpace blockSpace;

	private static Settings inst = null;

	private int scaleKoefficient = -1;
	private int defaultChannel = 0;
//
//	public Settings(BlockSpace blockSpace) {
//		this.blockSpace = blockSpace;
//	}

	public void setDefaultChannel(int value) { this.defaultChannel = value; }
	public int getDefaultChannel() { return this.defaultChannel; }

	public static Settings inst()
	{
		if (inst == null) {
			inst = new Settings();
		}
		return inst;
	}

	public void scale(int sign) {
		this.scaleKoefficient = sign == 1 ? -1 : -3;
		ImageStorage.inst().refreshImageSizes();
	}

	public int getStepWidth() { return getNoteWidth(); } // note image width (the one OS would display when you click on file->properties)
	public int getStepHeight() { return getNoteHeight() / 8; } // half-space between two staff's lines

	/** @return - scaled note image from file width */
	public int getNoteWidth() {
		return Constants.NORMAL_NOTE_WIDTH + 5 * scaleKoefficient;
	}
	public int getNoteHeight() {
		return Constants.NORMAL_NOTE_HEIGHT + 8 * scaleKoefficient;
	}
}
