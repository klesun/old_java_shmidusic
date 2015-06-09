package Storyspace.Article;

import Gui.ImageStorage;
import Model.AbstractModel;
import Model.Field.Field;
import org.apache.commons.math3.fraction.Fraction;

import java.awt.*;

public class CatchPhrase extends AbstractModel {

	private Field<String> text = h.addField("text", "");
	private Field<Integer> score = h.addField("score", 4);

	public CatchPhrase(Paragraph parent, String text) {
		super(parent);
		setText(text);
	}

	// model field getters/setter

	// getters
	public String getText() { return text.getValue(); }
	public int getScore() { return score.getValue(); }
	// setters
	public CatchPhrase setScore(int value) { score.setValue(value); return this; }
	// text should be immutable
	private CatchPhrase setText(String value) { text.setValue(value); return this; }

	public Color getColor() {
		Color bad = new Color(255, 0, 0, 63);
		Color good = new Color(0, 255, 0, 63);
		Fraction factor = new Fraction(getScore(), 9);

		return ImageStorage.getBetween(bad, good, factor);
	}
}
