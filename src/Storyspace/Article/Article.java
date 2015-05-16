package Storyspace.Article;

import Model.AbstractHandler;
import Model.AbstractModel;
import Storyspace.IStoryspacePanel;
import Storyspace.StoryspaceScroll;
import Storyspace.Storyspace;
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

	private List<Paragraph> parList = new ArrayList<>();

	final private static int SCROLL_BAR_WIDTH = /*25*/ 26;

	public Article(Storyspace parentStoryspace) {
		super();
        this.setBackground(Color.CYAN);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		handler = new AbstractHandler(this) {
//			public Boolean mousePressedFinal(ComboMouse combo) { return combo.leftButton; }
//			public Boolean mouseDraggedFinal(ComboMouse combo) { return combo.leftButton; }
		};
//		this.addMouseListener(handler);
//		this.addMouseMotionListener(handler);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
				sukaSdelajNormalnijRazmer();
            }
        });

		scroll = parentStoryspace.addModelChild(this);
	}

	/** @forbidden - you don't want change that method!!! these lines worth DOZENS OF HOURS */
	private void sukaSdelajNormalnijRazmer() {
		int width = scroll.getWidth() - SCROLL_BAR_WIDTH;
		int height = 0;
		for (Paragraph par: parList) {
			fixParagraphWidth(par);
			height += par.getPreferredSize().getHeight();
		}
		this.setMinimumSize(new Dimension(1, 1));
		this.setPreferredSize(new Dimension(width, height));
	}

	/** @forbidden - you don't want change that method!!! these lines worth DOZENS OF HOURS */
	private void fixParagraphWidth(Paragraph par) {
		int width = scroll.getWidth() - SCROLL_BAR_WIDTH;
		int height = par.getHeightIfWidthWas(width);
		par.setMinimumSize(new Dimension(1, 1));
		par.setPreferredSize(new Dimension(width, height));
	}

	private Paragraph addNewParagraph(String text) {
		Paragraph par = new Paragraph(this).setTextValue(text);
        parList.add(par); // logic
		this.add(par); // gui
		fixParagraphWidth(par);

		return par;
	}

	private String getWholeText() {
		String text = "";
		for (Paragraph par: parList) {
			text += par.getText() + '\n';
		}
		return text;
	}

	private Article clearChildList() {
		this.removeAll();
        this.parList.clear();
		return this;
	}

	@Override
	public StoryspaceScroll getStoryspaceScroll() { return scroll; }
	@Override
	public AbstractModel getFocusedChild() { return null; } // no Model children
	@Override
	public StoryspaceScroll getModelParent() { return StoryspaceScroll.class.cast(getParent().getParent()); } // =D
	@Override
	public AbstractHandler getHandler() { return this.handler; }

	@Override
	public JSONObject getJsonRepresentation() {
		JSONObject dict = new JSONObject();
		dict.put("className", getClass().getSimpleName());
		dict.put("text", this.getWholeText());
		return dict;
	}

	@Override
	public Article reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.clearChildList();
		String text = jsObject.getString("text");
		String[] parList = text.split("\n");
		for (String par: parList) {
			this.addNewParagraph(par);
		}
		return this;
	}
}
