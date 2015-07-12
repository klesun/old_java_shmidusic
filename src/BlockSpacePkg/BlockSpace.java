package BlockSpacePkg;

import Gui.ImageStorage;
import Gui.Settings;
import main.MajesticWindow;
import Model.*;
import BlockSpacePkg.Image.ImagePanel;
import BlockSpacePkg.StaffPkg.StaffPanel;
import BlockSpacePkg.ArticlePkg.Article;
import Stuff.Tools.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockSpace extends JPanel implements IComponentModel {

	private MajesticWindow window = null;

	private List<Block> childScrollList = new ArrayList<>();

	final private AbstractHandler handler;
	final private Helper modelHelper = new Helper(this);
	final private Settings settings = new Settings(this);
	final private ImageStorage imageStorage = new ImageStorage(this);

	public BlockSpace(MajesticWindow window) {
		this.window = window;
		setLayout(null);
		setFocusable(true);

		handler = new BlockSpaceHandler(this);
		addKeyListener(handler);
		addMouseMotionListener(handler);
		addMouseListener(handler);

		this.setBackground(Color.DARK_GRAY);
	}

	public Block addModelChild(IBlockSpacePanel child) {
		Block scroll = new Block(child, this);
		childScrollList.add(scroll);
		this.add(scroll);

		/** @ommented for debug */
//		this.validate();
		child.requestFocus();

		return scroll;
	}

	public void removeModelChild(Block child) {
		this.remove(child);
		childScrollList.remove(child);
		repaint();
	}

	private void clearChildList() {
		this.removeAll();
		this.childScrollList.clear();
	}

	// getters

	public MajesticWindow getWindow() { return this.window; }

	// overriding IModel

	@Override
	public IComponentModel getModelParent() { return null; } // BlockSpace is root parent
	@Override
	public Block getFocusedChild() { // i think, i don't get awt philosophy...
		return getFocusedChild(window.getFocusOwner());
	}

	public Block getFocusedChild(Component awtFocus) {
		if (awtFocus instanceof IModel) {
			IModel model = (IModel)awtFocus;
			while (model != null) {
				if (childScrollList.contains(model)) {
					return (Block)model;
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

	public Settings getSettings() { return this.settings; }
	public ImageStorage getImageStorage() { return this.imageStorage; }

	@Override
	public void getJsonRepresentation(JSONObject dict) {
		dict.put("childBlockList", new JSONArray(childScrollList.stream().map(child -> {
			JSONObject childJs = Helper.getJsonRepresentation(child.content);
			childJs.put("scroll", Helper.getJsonRepresentation(child));
			childJs.put("className", child.content.getClass().getSimpleName());
			return childJs;
		}).toArray()));
	}
	@Override
	public BlockSpace reconstructFromJson(JSONObject jsObject) throws JSONException {
		clearChildList();
		JSONArray childBlockList = jsObject.getJSONArray("childBlockList");
		for (int i = 0; i < childBlockList.length(); ++i) {
			JSONObject childJs = childBlockList.getJSONObject(i);
			IBlockSpacePanel child = makeChildByClassName(childJs.getString("className"));
			child.getScroll().reconstructFromJson(childJs.getJSONObject("scroll"));
			child.reconstructFromJson(childJs);

			// StaffPanels take ~10 mib; Articles - ~1 mib
//			Logger.logMemory("reconstructed " + child.getClass().getSimpleName() + " [" + child.getScroll().toString() + "]");
		}
		return this;
	}

	// private methods

	private static Class<?extends IBlockSpacePanel>[] childClasses = new Class[]{Article.class, ImagePanel.class, StaffPanel.class};

	private IBlockSpacePanel makeChildByClassName(String className) {
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

		Logger.fatal("Invalid className, BlockSpace denies this child [" + className + "]");
		return null;
	}

	public List<Block> getChildScrollList() {
		return childScrollList;
	}

	/** @unused kinda */
	public List<Article> getArticles() {
		return getChildScrollList().stream()
				.filter(scroll -> scroll.content instanceof Article)
				.map(scroll -> Article.class.cast(scroll.content))
				.collect(Collectors.toList());
	}

	// event handles

	public void pushToFront(Block scroll) {
		setComponentZOrder(scroll, 0);
		repaint();
	}

	public void scale(int sign) {
		double factor = sign == -1 ? 0.75 : 1 / 0.75;
		if (getChildScrollList().stream().noneMatch(Block::isFullscreen)) {
			for (Block scroll: childScrollList) {

				int width = (int) (scroll.getWidth() * factor);
				int height = (int) (scroll.getHeight() * factor);
				int x = (int) (scroll.getX() * factor);
				int y = (int) (scroll.getY() * factor);

				scroll.setSize(width, height);
				scroll.setLocation(x, y);
				scroll.validate();
			}
		}
		getSettings().scale(sign);
	}

	public StaffPanel addMusicBlock() {
		StaffPanel obj = new StaffPanel(this);
		this.revalidate();
		return obj;
	}
	public Article addTextBlock() {
		Article obj = new Article(this);
		this.revalidate();
		return obj;
	}
	public ImagePanel addImageBlock() {
		ImagePanel obj = new ImagePanel(this);
		this.revalidate();
		return obj;
	}
}
