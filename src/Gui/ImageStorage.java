package Gui;

public class ImageStorage {
	private static ImageStorage instance = null;

	public static ImageStorage inst() {
		if (ImageStorage.instance == null) {
			ImageStorage.instance = new ImageStorage();
		}
		return ImageStorage.instance;
	}
}
