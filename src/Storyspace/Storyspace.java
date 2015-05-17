package Storyspace;

import Main.MajesticWindow;
import Model.*;
import Storyspace.Image.ImagePanel;
import Storyspace.Staff.StaffPanel;
import Storyspace.Article.Article;
import Stuff.OverridingDefaultClasses.Scroll;
import Stuff.Tools.FileProcessor;
import Stuff.Tools.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Storyspace extends JPanel implements IModel {

	private MajesticWindow window = null;

	private ArrayList<Component> /* implements IModel */ modelChildList = new ArrayList<>();

	private AbstractHandler handler = null;

	public Storyspace(MajesticWindow window) {
		this.window = window;
		setLayout(null);
		setFocusable(true);

		handler = this.makeHandler();
		addKeyListener(handler);
		addMouseMotionListener(handler);
		addMouseListener(handler);

		this.setBackground(Color.DARK_GRAY);
	}

	public StoryspaceScroll addModelChild(Component /*IModel*/ child) {
		modelChildList.add(child);
		StoryspaceScroll scroll = new StoryspaceScroll(child);
		this.add(scroll);
		this.validate();
		child.requestFocus();
		return scroll;
	}

	// getters

	public MajesticWindow getWindow() { return this.window; }

	// overriding IModel

	@Override
	public IModel getModelParent() { return null; } // Storyspace is root parent
	@Override
	public IModel getFocusedChild() { // stupid java: JPanel is NOT focused even when one of it's children is focused
		Component focused = window.getFocusOwner();
		return modelChildList.contains(focused) ? IModel.class.cast(focused) : null;
	}
	@Override
	public AbstractHandler getHandler() { return this.handler; }

	@Override
	public void getJsonRepresentation(JSONObject dict) {
		dict.put("childBlockList", new JSONArray(modelChildList.stream().map(child -> {
			JSONObject childJs = Helper.getJsonRepresentation((IModel)child);
			childJs.put("scroll", Helper.getJsonRepresentation(IStoryspacePanel.class.cast(child).getStoryspaceScroll()));
			childJs.put("className", child.getClass().getSimpleName());
			return childJs;
		}).toArray()));
	}
	@Override
	public Storyspace reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.removeAll();
		this.modelChildList.clear();
		JSONArray childBlockList = jsObject.getJSONArray("childBlockList");
		for (int i = 0; i < childBlockList.length(); ++i) {
			JSONObject childJs = childBlockList.getJSONObject(i);
			Component /*IModel*/ child = makeChildByClassName(childJs.getString("className"));
			IStoryspacePanel.class.cast(child)
					.getStoryspaceScroll()
					.reconstructFromJson(childJs.getJSONObject("scroll"));
			IModel.class.cast(child).reconstructFromJson(childJs);
		}
		return this;
	}

	// private methods

	private static Class<?extends Component>[] childClasses = new Class[]{Article.class, ImagePanel.class, StaffPanel.class};

	private Component /* IModel */ makeChildByClassName(String className) {
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
	
	private AbstractHandler makeHandler() {
		JFileChooser jsonChooser = new JFileChooser("/home/klesun/yuzefa_git/storyspaceContent/");
		jsonChooser.setFileFilter(new FileNameExtensionFilter("Json Storyspace data", "gson"));

		return new AbstractHandler(this) {
			@Override
			protected void initActionMap() {
				addCombo(ctrl, k.VK_M).setDo((this.getContext())::addMusicBlock);
				addCombo(ctrl, k.VK_T).setDo((this.getContext())::addTextBlock);
				addCombo(ctrl, k.VK_I).setDo((this.getContext())::addImageBlock);

				addCombo(ctrl, k.VK_G).setDo(makeSaveFileDialog(FileProcessor::saveStoryspace, jsonChooser, "gson"));
				addCombo(ctrl, k.VK_R).setDo(combo -> {
					int sVal = jsonChooser.showOpenDialog(window);
					if (sVal == JFileChooser.APPROVE_OPTION) {
						FileProcessor.openStoryspace(jsonChooser.getSelectedFile(), getContext());
					}
					makeSaveFileDialog(FileProcessor::saveStoryspace, jsonChooser, "gson");
				});

				addCombo(ctrl, k.VK_EQUALS).setDo((this.getContext())::scale);
				addCombo(ctrl, k.VK_MINUS).setDo((this.getContext())::scale);
			}
			@Override
			public Boolean mousePressedFinal(ComboMouse mouse) {
				if (mouse.leftButton) {
					requestFocus();
				}
				return true;
			}
			@Override
			public Boolean mouseDraggedFinal(ComboMouse mouse) {
				Arrays.asList(getComponents()).stream().forEach(component
					-> component.setLocation(component.getX() + mouse.dx, component.getY() + mouse.dy));
				mouseLocation.move(mouse.dx, mouse.dy);
				return true;
			}
			@Override
			public Storyspace getContext() { return (Storyspace)super.getContext(); }
		};
	}

	final private Consumer<Combo> makeSaveFileDialog(BiConsumer<File, Storyspace> lambda, JFileChooser chooser, String ext) {
		return combo -> {
			int rVal = chooser.showSaveDialog(getWindow());
			if (rVal == JFileChooser.APPROVE_OPTION) {
				File fn = chooser.getSelectedFile();
				if (!chooser.getFileFilter().accept(fn)) { fn = new File(fn + "." + ext); }
				// TODO: prompt on overwrite
				lambda.accept(fn, this);
			}
		};
	}

	public void scale(Combo combo) {
		int sign = combo.getSign();
		double factor = sign == -1 ? 0.75 : 1 / 0.75;
		for (Component child: modelChildList) {
			Scroll scroll = Scroll.class.cast(child.getParent().getParent()); // =D

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
