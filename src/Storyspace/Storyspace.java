package Storyspace;

import Main.MajesticWindow;
import Model.*;
import Storyspace.Image.ImagePanel;
import Storyspace.Staff.StaffPanel;
import Storyspace.Article.Article;
import Stuff.OverridingDefaultClasses.Scroll;
import Stuff.Tools.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Storyspace extends JPanel implements IComponentModel {

	private MajesticWindow window = null;

	@Deprecated // no need to have direct access to them and makes confusion
	private ArrayList<IStoryspacePanel> modelChildList = new ArrayList<>();

	private AbstractHandler handler = null;
	private Helper modelHelper = new Helper(this);

	public Storyspace(MajesticWindow window) {
		this.window = window;
		setLayout(null);
		setFocusable(true);

		handler = new StoryspaceHandler(this);
		addKeyListener(handler);
		addMouseMotionListener(handler);
		addMouseListener(handler);

		this.setBackground(Color.DARK_GRAY);
	}

	public StoryspaceScroll addModelChild(IStoryspacePanel child) {
		modelChildList.add(child);
		StoryspaceScroll scroll = new StoryspaceScroll(child, this);
		this.add(scroll);
		this.validate();
		child.requestFocus();
		return scroll;
	}

	public void removeModelChild(IStoryspacePanel child) {
		this.remove(child.getStoryspaceScroll());
		modelChildList.remove(child);
		repaint();
	}

	private void clearChildList() {
		this.removeAll();
		this.modelChildList.clear();
	}

	// getters

	public MajesticWindow getWindow() { return this.window; }

	// overriding IModel

	@Override
	public IComponentModel getModelParent() { return null; } // Storyspace is root parent
	@Override
	public StoryspaceScroll getFocusedChild() { // i think, i don't get awt philosophy...
		Component focused = window.getFocusOwner();
		if (focused instanceof IModel) {
			IModel model = (IModel)focused;
			while (model != null) {
				if (modelChildList.contains(model)) {
					return IStoryspacePanel.class.cast(model).getStoryspaceScroll();
				}
				model = model.getModelParent();
			}
		}

		return null;
	}
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

	@Override
	public void getJsonRepresentation(JSONObject dict) {
		dict.put("childBlockList", new JSONArray(modelChildList.stream().map(child -> {
			JSONObject childJs = Helper.getJsonRepresentation((IModel) child);
			childJs.put("scroll", Helper.getJsonRepresentation(IStoryspacePanel.class.cast(child).getStoryspaceScroll()));
			childJs.put("className", child.getClass().getSimpleName());
			return childJs;
		}).toArray()));
	}
	@Override
	public Storyspace reconstructFromJson(JSONObject jsObject) throws JSONException {
		clearChildList();
		JSONArray childBlockList = jsObject.getJSONArray("childBlockList");
		for (int i = 0; i < childBlockList.length(); ++i) {
			JSONObject childJs = childBlockList.getJSONObject(i);
			IStoryspacePanel child = makeChildByClassName(childJs.getString("className"));
			child.getStoryspaceScroll().reconstructFromJson(childJs.getJSONObject("scroll"));
			child.reconstructFromJson(childJs);

			// StaffPanels take ~10 mib; Articles - ~1 mib
//			Logger.logMemory("reconstructed " + child.getClass().getSimpleName() + " [" + child.getStoryspaceScroll().toString() + "]");
		}
		return this;
	}

	// private methods

	private static Class<?extends IStoryspacePanel>[] childClasses = new Class[]{Article.class, ImagePanel.class, StaffPanel.class};

	private IStoryspacePanel makeChildByClassName(String className) {
		for (int i = 0; i < childClasses.length; ++i) {
			if (childClasses[i].getSimpleName().equals(className)) {
				try {
					return childClasses[i].getDeclaredConstructor(getClass()).newInstance(this);
				} catch (Exception e) {
					childClasses[i].getSimpleName();
					System.out.println(e.getMessage());
					e.printStackTrace(); Runtime.getRuntime().halt(666);
				}
			}
		}

		Logger.fatal("Invalid className, Storyspace denies this child [" + className + "]");
		return null;
	}

	public List<Article> getArticles() {
		return modelChildList.stream()
				.filter(child -> child instanceof Article)
				.map(child -> Article.class.cast(child))
				.collect(Collectors.toList());
	}

	// event handles

	public void pushToFront(StoryspaceScroll scroll) {
		setComponentZOrder(scroll, 0);
		repaint();
	}

	public void scale(Combo combo) {
		int sign = combo.getSign();
		double factor = sign == -1 ? 0.75 : 1 / 0.75;
		for (IStoryspacePanel child: modelChildList) {
			StoryspaceScroll scroll = child.getStoryspaceScroll();
//			Scroll scroll = Scroll.class.cast(child.getParent().getParent()); // =D

			int width = (int) (scroll.getWidth() * factor);
			int height = (int) (scroll.getHeight() * factor);
			int x = (int) (scroll.getX() * factor);
			int y = (int) (scroll.getY() * factor);

			scroll.setSize(width, height);
			scroll.setLocation(x, y);
			scroll.validate();

			// TODO: child.scale(combo)
		}
	}

	public StaffPanel addMusicBlock(Combo combo) { return new StaffPanel(this); }
	public void addTextBlock(Combo combo) { new Article(this); }
	public void addImageBlock(Combo combo) { new ImagePanel(this); }
}
