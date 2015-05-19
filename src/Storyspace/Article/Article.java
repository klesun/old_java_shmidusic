package Storyspace.Article;

import Model.AbstractHandler;
import Model.AbstractModel;
import Model.Helper;
import Model.IModel;
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
        this.setBackground(Color.CYAN);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		handler = new AbstractHandler(this) {};

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

	private Paragraph addNewParagraph() {
		Paragraph par = new Paragraph(this);
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

	private List<Paragraph> getParList() { return parList; }

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

		return this;
	}
}
