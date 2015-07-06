package Gui;

import BlockSpacePkg.BlockSpace;
import BlockSpacePkg.StaffPkg.StaffPanel;

// TODO: maybe move it into ImageStorage
public class Settings {

	final private BlockSpace blockSpace;

	private int scaleKoefficient = -1;
	private int defaultChannel = 0;

	public Settings(BlockSpace blockSpace) {
		this.blockSpace = blockSpace;
	}

	public void setDefaultChannel(int value) { this.defaultChannel = value; }
	public int getDefaultChannel() { return this.defaultChannel; }

	public void scale(int sign) {
		this.scaleKoefficient = sign == 1 ? -1 : -3;
		blockSpace.getImageStorage().refreshImageSizes();

		blockSpace.getChildScrollList().stream()
				.filter(s -> s.content instanceof StaffPanel)
				.forEach(s -> ((StaffPanel)s.content).surfaceCompletelyChanged());
	}

	public int getStepWidth() { return getNotaWidth(); } // nota image width (the one OS would display when you click on fil->properties)
	public int getStepHeight() { return getNotaHeight() / 8; } // half-space between two StaffPkg's lines

	/** @return - scaled Nota image from file width */
	public int getNotaWidth() {
		return Constants.NORMAL_NOTA_WIDTH + 5 * scaleKoefficient;
	}
	public int getNotaHeight() {
		return Constants.NORMAL_NOTA_HEIGHT + 8 * scaleKoefficient;
	}
}
