package org.blockspace.article;

import org.sheet_midusic.stuff.graphics.Constants;
import org.sheet_midusic.stuff.graphics.ImageStorage;
import org.klesun_model.*;
import org.klesun_model.field.Field;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;
import org.sheet_midusic.stuff.tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;


public class Paragraph extends JTextArea implements IComponent, IModel {

	private Article parent = null;
	private AbstractHandler handler = null;
	private Helper modelHelper = new Helper(this);

	private Field<Integer> score = addField("score", 0);
	private Map<String, CatchPhrase> catchPhrases = new HashMap<>();

	private static TruMap<Combo, ContextAction<Paragraph>> actionMap = new TruMap<>();
	static {

		Combo.getNumberComboList(KeyEvent.CTRL_MASK).forEach(c -> actionMap.p(c, mkAction(p -> p.setSelectedScore(c.getPressedNumber())).setOmitMenuBar(true)));

		actionMap
			.p(new Combo(0, KeyEvent.VK_DOWN), mkFailableAction(p -> new Explain(p.getRowIndex() < p.getRowCount() - 1, "No more rows")).setOmitMenuBar(true))
			.p(new Combo(0, KeyEvent.VK_UP), mkFailableAction(p -> new Explain(p.getRowIndex() > 0, "First row")).setOmitMenuBar(true))
			.p(new Combo(0, KeyEvent.VK_LEFT), mkFailableAction(p -> new Explain(p.getCaretPosition() > 0, "Start of text")).setOmitMenuBar(true))
			.p(new Combo(0, KeyEvent.VK_BACK_SPACE), mkFailableAction(p -> new Explain(p.getCaretPosition() > 0, "Start of text")).setOmitMenuBar(true))
			.p(new Combo(0, KeyEvent.VK_RIGHT), mkFailableAction(p -> new Explain(p.getCaretPosition() < p.getText().length(), "End of text")).setOmitMenuBar(true))
			.p(new Combo(0, KeyEvent.VK_DELETE), mkFailableAction(p -> new Explain(p.getCaretPosition() < p.getText().length(), "End of text")).setOmitMenuBar(true))
		;
	}

	public Paragraph(Article parent) {
		setLineWrap(true);
		setWrapStyleWord(true);
		setTabSize(4);
		super.setFont(Constants.PROJECT_FONT);

		handler = new AbstractHandler(this) {
			public Boolean mousePressedFinal(ComboMouse combo) { return combo.leftButton; }
			public Boolean mouseDraggedFinal(ComboMouse combo) { return combo.leftButton; }

			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() { return actionMap; }

			public Paragraph getContext() { return (Paragraph)super.getContext(); }
		};
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);
		this.addKeyListener(handler);

		Paragraph par = this;

		this.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {

				SwingUtilities.invokeLater(par::splitIfGotLineBreaks); // invokeLater() because java.lang.IllegalStateException: Attempt to mutate in notification
				updateHighlightedWords();

				parent.fixParagraphWidth(par);
				parent.sukaSdelajNormalnijRazmer();
			}
			public void removeUpdate(DocumentEvent e) {
				updateHighlightedWords();

				parent.fixParagraphWidth(par);
				parent.sukaSdelajNormalnijRazmer();
			}
			public void changedUpdate(DocumentEvent e) {}
		});

		this.parent = parent;
	}

	private static ContextAction<Paragraph> mkAction(Consumer<Paragraph> lambda) {
		ContextAction<Paragraph> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<Paragraph> mkFailableAction(Function<Paragraph, Explain> lambda) {
		ContextAction<Paragraph> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	public int getRowIndex() {
		// Костыль могёт - костыль вперёд!

		Rectangle r = null;
		try { r = modelToView(getCaretPosition()); }
		catch (BadLocationException e) { Logger.fatal(e, "It should never happen"); }

		return (r.y + r.height) / getFontMetrics(getFont()).getHeight() - 1;

	}

	public int getRowCount() {
		Rectangle r = null;
		try { r = modelToView(getText().length()); }
		catch (BadLocationException e) { Logger.fatal(e, "It should never happen"); }

		return (r.y + r.height) / getFontMetrics(getFont()).getHeight() - 1;
	}

	public int getHeightIfWidthWas(int width) {

		JTextArea par = new JTextArea(this.getText());
		par.setLineWrap(true);
		par.setWrapStyleWord(true);
		par.setFont(this.getFont());

		par.setSize(new Dimension(width, 1));

		Rectangle r = null;
		try { r = par.modelToView(par.getText().length()); }
		catch (BadLocationException e) { Runtime.getRuntime().exit("Lolwhat?".length()); }

		return r.y + r.height + 1 * getFontMetrics(getFont()).getHeight(); // + 1 cuz i wanna separate them
	}

	// field getters/setters

	public Integer getScore() { return score.get(); }
	public Paragraph setScore(Integer value) {
		score.set(value);
		updateBgColor();
		return this;
	}

	public CatchPhrase addCatchPhrase(String text) {
		CatchPhrase phrase = new CatchPhrase(text);
		catchPhrases.put(text, phrase);
		return phrase;
	}

	public void removeCatchPhrase(CatchPhrase quote) {
		catchPhrases.remove(quote.getText());
	}

	public Paragraph setSelectedScore(Integer value) {
		if (getSelectedText() != null) {
			addCatchPhrase(getSelectedText()).setScore(value);
			updateHighlightedWords();
		} else {
			setScore(value);
		}
		updateBgColor();
		return this;
	}

	@Override
	public IComponent getFocusedChild() { return null; }
	@Override
	public Article getModelParent() { return parent; }
	@Override
	public AbstractHandler getHandler() { return handler; }
	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject phraseDict = new JSONObject();
		for (Entry<String, CatchPhrase> entry: catchPhrases.entrySet()) {
			phraseDict.put(entry.getKey(), entry.getValue().getJsonRepresentation());
		}

		return new JSONObject()
			.put("score", score.get())
			.put("text", getText())
			.put("catchPhrases", phraseDict);
	}
	@Override
	public IModel reconstructFromJson(JSONObject jsObject) throws JSONException {
		clearChildList();
		IModel.super.reconstructFromJson(jsObject);
		setText(jsObject.getString("text"));
		if (jsObject.has("catchPhrases")) {
			JSONObject phraseDict = jsObject.getJSONObject("catchPhrases");
			Iterator<String> keys = phraseDict.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				addCatchPhrase(key).reconstructFromJson(phraseDict.getJSONObject(key));
			}
		}

		updateBgColor();
		updateHighlightedWords();
		parent.fixParagraphWidth(this);
		return this;
	}

	@Override
	public Cursor getDefaultCursor() {
		return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
	}

	private void clearChildList() { catchPhrases.clear(); }

	@Override
	public void setText(String value) {
		super.setText(value);
		updateHighlightedWords();
	}


	// event handles

	private void updateBgColor() {
		Color bad = new Color(255, 191, 191);
		Color good = new Color(191, 255, 191);
		Fraction factor = new Fraction(getScore(), 9);

		setBackground(getScore() == 0 ? Color.WHITE : ImageStorage.getBetween(bad, good, factor));
	}

	private Paragraph updateHighlightedWords() {

		getHighlighter().removeAllHighlights();
		List<CatchPhrase> removeEm = new ArrayList<>();

		for (Map.Entry<String, CatchPhrase> entry: catchPhrases.entrySet()) {
			CatchPhrase quote = entry.getValue();

			if (getText().indexOf(quote.getText()) > -1) {

				HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(quote.getColor());
				int p0 = getText().indexOf(quote.getText());
				int p1 = p0 + quote.getText().length();

				try { getHighlighter().addHighlight(p0, p1, painter); }
				catch (BadLocationException e) { Logger.fatal(e, "Этого никогда не произойдёт " + e.offsetRequested()); }

			} else { removeEm.add(quote); }
		}

		removeEm.forEach(this::removeCatchPhrase);

		return this;
	}

	private void splitIfGotLineBreaks() {

		// reserving links to CatchPhrases to be able to copy 'em, cuz when we do setText() they would disappear from Par
		List<CatchPhrase> rezQuotes = new ArrayList<>(catchPhrases.values());

		String[] pars = getText().split("\n");

		if (pars.length > 1) {
			this.replaceRange("", pars[0].length(), this.getText().length());
		}

		for (int idx = 1; idx < pars.length; ++idx) {
			Paragraph par = parent.addNewParagraph(parent.getParList().indexOf(this) + 1);
			par.setScore(getScore()).setText(pars[idx]);
			par.consumeQuoteList(rezQuotes).updateHighlightedWords().requestFocus();
		}
	}

	public Paragraph consumeQuoteList(List<CatchPhrase> quotes) {
		for (CatchPhrase quote: quotes) {
			this.addCatchPhrase(quote.getText()).setScore(quote.getScore());
		}
		return this;
	}

	public void mergeBackTo(Paragraph bequested) {
		bequested.append(this.getText());
		bequested.catchPhrases.putAll(this.catchPhrases);
		bequested.updateHighlightedWords();
		parent.removeParagraph(this);
	}
}
