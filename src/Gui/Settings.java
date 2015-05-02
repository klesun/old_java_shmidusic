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

	public void changeScale(int n) {
		this.scaleKoefficient += n;
		if (this.scaleKoefficient > -1) { this.scaleKoefficient = -1; };
		if (this.scaleKoefficient < -3) { this.scaleKoefficient = -3; };
	}

	public void scaleUp(Combo combo) {
		this.scaleKoefficient = -1;
		ImageStorage.inst().refreshImageSizes();
	}
	public void scaleDown(Combo combo) {
		this.scaleKoefficient = -3;
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
