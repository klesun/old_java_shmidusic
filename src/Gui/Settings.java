package Gui;

import Model.Combo;

public class Settings {

	private static Settings instance = null;

	private int scaleKoefficient = -1;

	public static Settings inst() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

	public void scale(Combo combo) {
		this.scaleKoefficient = combo.getSign() == 1 ? -1 : -3;
		ImageStorage.inst().refreshImageSizes();
	}

	public static int getNotaWidth() {
		return Constants.NORMAL_NOTA_WIDTH + 5 * inst().scaleKoefficient;
	}
	public static int getNotaHeight() {
		return Constants.NORMAL_NOTA_HEIGHT + 8 * inst().scaleKoefficient;
	}
	public static int getStepWidth() {
		return inst().getNotaWidth();
	}
	public static int getStepHeight() {
		return inst().getNotaHeight() / 8;
	}
}
