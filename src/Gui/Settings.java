package Gui;

import Model.ActionResult;

import Storyspace.Staff.StaffPanel;
import Storyspace.Storyspace;

import java.util.stream.Stream;

// TODO: maybe move it into ImageStorage
public class Settings {

	final private Storyspace storyspace;

	private int scaleKoefficient = -1;
	private int defaultChannel = 0;

	public Settings(Storyspace storyspace) {
		this.storyspace = storyspace;
	}

	public void setDefaultChannel(int value) { this.defaultChannel = value; }
	public int getDefaultChannel() { return this.defaultChannel; }

	public ActionResult scale(int sign) {
		this.scaleKoefficient = sign == 1 ? -1 : -3;
		storyspace.getImageStorage().refreshImageSizes();

		storyspace.getChildScrollList().stream()
				.filter(s -> s.content instanceof StaffPanel)
				.forEach(s -> ((StaffPanel)s.content).surfaceCompletelyChanged());

		return new ActionResult("defaultly passed to parent");
	}

	public int getStepWidth() { return getNotaWidth(); } // nota image width (the one OS would display when you click on fil->properties)
	public int getStepHeight() { return getNotaHeight() / 8; } // half-space between two Staff's lines

	/** @return - scaled Nota image from file width */
	public int getNotaWidth() {
		return Constants.NORMAL_NOTA_WIDTH + 5 * scaleKoefficient;
	}
	public int getNotaHeight() {
		return Constants.NORMAL_NOTA_HEIGHT + 8 * scaleKoefficient;
	}
}
