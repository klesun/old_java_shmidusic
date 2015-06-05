package Storyspace.Article;

import Model.*;
import Storyspace.IStoryspacePanel;
import Storyspace.StoryspaceScroll;
import Storyspace.Storyspace;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class Article extends JPanel implements IStoryspacePanel {

	private StoryspaceScroll scroll = null;
	private AbstractHandler handler = null;

	private Helper modelHelper = new Helper(this);

	private List<Paragraph> parList = new ArrayList<>();

	final private static int SCROLL_BAR_WIDTH = /*25*/ 26;

	public Article(Storyspace parentStoryspace) {
		super();
        this.setBackground(Color.DARK_GRAY);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		handler = new AbstractHandler(this) {
			protected void initActionMap() {
				addCombo(0, k.VK_DOWN).setDo(getContext()::focusNext);
				addCombo(0, k.VK_RIGHT).setDo(getContext()::focusNext);
				addCombo(0, k.VK_UP).setDo(getContext()::focusBack);
				addCombo(0, k.VK_LEFT).setDo(getContext()::focusBack);
				addCombo(0, k.VK_BACK_SPACE).setDo(getContext()::mergeBack);
				addCombo(0, k.VK_DELETE).setDo(getContext()::mergeNext);
			}
			public Article getContext() {
				return (Article)super.getContext();
			}
		};

        addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				for (Paragraph par: parList) { fixParagraphWidth(par); }
				sukaSdelajNormalnijRazmer();
			}
		});

		scroll = parentStoryspace.addModelChild(this);

		this.addNewParagraph();
	}

	/** @forbidden - you don't want change that method!!! these lines worth DOZENS OF HOURS */
	public void sukaSdelajNormalnijRazmer() {
		int width = scroll.getWidth() - SCROLL_BAR_WIDTH;
		int height = 0;
		for (Paragraph par: parList) {
			height += par.getPreferredSize().getHeight();
		}
		this.setMinimumSize(new Dimension(1, 1));
		this.setPreferredSize(new Dimension(width, height));
	}

	/** @forbidden - you don't want change that method!!! these lines worth DOZENS OF HOURS */
	public void fixParagraphWidth(Paragraph par) { // TODO: move to Paragraph
		int width = scroll.getWidth() - SCROLL_BAR_WIDTH;
		int height = par.getHeightIfWidthWas(width);
		par.setMinimumSize(new Dimension(1, 1));
		par.setPreferredSize(new Dimension(width, height));
	}

	public Paragraph addNewParagraph(int index) {
		Paragraph par = new Paragraph(this);
		if (index > -1) { parList.add(index, par);
		} else { parList.add(par); }
		this.add(par, index);

		return par;
	}

	public Paragraph addNewParagraph() {
		return addNewParagraph(-1);
	}

	public Article removeParagraph(Paragraph par) {
		parList.remove(par);
		this.remove(par);
		return this;
	}

	private Article clearChildList() {
		this.removeAll();
        this.parList.clear();
		return this;
	}

	public List<Paragraph> getParList() { return parList; }

	@Override
	public StoryspaceScroll getStoryspaceScroll() { return scroll; }
	@Override
	public Paragraph getFocusedChild() {
		Component focused = getModelParent().getModelParent().getWindow().getFocusOwner();
		return focused instanceof Paragraph && parList.contains(focused)
				? (Paragraph)focused
				: null;
	}
	@Override
	public StoryspaceScroll getModelParent() { return getStoryspaceScroll(); }
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

	@Override
	public void getJsonRepresentation(JSONObject dict) {
		dict.put("paragraphList", new JSONArray(getParList().stream().map(p -> Helper.getJsonRepresentation(p)).toArray()));
	}

	@Override
	public Article reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.clearChildList();

		JSONArray parJsonList = jsObject.getJSONArray("paragraphList");
		for (int idx = 0; idx < parJsonList.length(); ++idx) {
			JSONObject childJs = parJsonList.getJSONObject(idx);
			this.addNewParagraph().reconstructFromJson(childJs);
		}

		sukaSdelajNormalnijRazmer();
		return this;
	}

	private int getFocusedIndex() {
		return this.getFocusedChild() == null
				? -1
				: this.parList.indexOf(getFocusedChild());
	}

	private Article setFocusedIndex(int value) {
		if (parList.size() > 0) {
			int idx = limit(value, 0, parList.size() - 1);
			parList.get(idx).requestFocus();
		}
		return this;
	}

	// event handles

	public void focusNext(Combo c) { this.setFocusedIndex(this.getFocusedIndex() + 1); }
	public void focusBack(Combo c) { this.setFocusedIndex(this.getFocusedIndex() - 1); }

	public void mergeBack(Combo c) {
		if (getFocusedIndex() > 0) {
			Paragraph back = getParList().get(getFocusedIndex() - 1);
			int caretPos = back.getText().length();
			getFocusedChild().mergeBackTo(back);
			back.requestFocus();
			back.setCaretPosition(caretPos);
		}
	}
	public void mergeNext(Combo c) {
		if (getFocusedIndex() < getParList().size() - 1 && getFocusedIndex() > -1) {
			SwingUtilities.invokeLater(() -> {
				int caretPos = getFocusedChild().getText().length();
				Paragraph next = getParList().get(getFocusedIndex() + 1);
				next.mergeBackTo(getFocusedChild());
				getFocusedChild().setCaretPosition(caretPos);
			});
		}
	}

	final protected static int limit(int value, int min, int max) { return Math.min(Math.max(value, min), max); }
}
