package Gui;

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
		if (this.scaleKoefficient > 0) { this.scaleKoefficient = 0; };
		if (this.scaleKoefficient < -3) { this.scaleKoefficient = -3; };
	}

	public int getNotaWidth() {
		return SheetMusic.NORMAL_WIDTH + 5 * this.scaleKoefficient;
	}

	public int getNotaHeight() {
		return SheetMusic.NORMAL_HEIGHT + 8 * this.scaleKoefficient;
	}

	public int getStepWidth() {
		return this.getNotaWidth();
	}

	public int getStepHeight() {
		return this.getNotaHeight() / 8;
	}
}
