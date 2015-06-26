package Gui;

import Model.Combo;

public class Settings {

	private static Settings instance = null;

	private int scaleKoefficient = -1;
	private int defaultChannel = 0;

	public static Settings inst() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

//	public void scale(Combo combo) {
//		scale(combo.getSign());
//	}

	public void setDefaultChannel(int value) { this.defaultChannel = value; }
	public int getDefaultChannel() { return this.defaultChannel; }

	public void scale(int sign) {
		this.scaleKoefficient = sign == 1 ? -1 : -3;
		ImageStorage.inst().refreshImageSizes();
	}

	public static int getStepWidth() { return inst().getNotaWidth(); } // nota image width (the one OS would display when you click on fil->properties)
	public static int getStepHeight() { return inst().getNotaHeight() / 8; } // half-space between two Staff's lines

	public static int getNotaWidth() {
		return Constants.NORMAL_NOTA_WIDTH + 5 * inst().scaleKoefficient;
	}
	public static int getNotaHeight() {
		return Constants.NORMAL_NOTA_HEIGHT + 8 * inst().scaleKoefficient;
	}
}
