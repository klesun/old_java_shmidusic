package Storyspace.Image;

import Gui.ImageStorage;
import Model.*;
import Storyspace.Storyspace;
import Storyspace.IStoryspacePanel;
import Storyspace.StoryspaceScroll;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

public class ImagePanel extends JPanel implements IStoryspacePanel {

	private BufferedImage image = null;
	private JLabel imageLabel = null;
	private String imagePath = "";

	private StoryspaceScroll scroll = null;
	private AbstractHandler handler = null;
	private Helper modelHelper = new Helper(this);

	public ImagePanel(Storyspace parentStoryspace) {
		setFocusable(true);
		this.add(imageLabel = new JLabel("Image not loaded"));

		handler = new AbstractHandler(this) {
			@Override
			protected void initActionMap() {
				addCombo(ctrl, k.VK_O).setDo(makeOpenFileDialog(getContext()::loadImage));
			}
			@Override
			public Boolean mousePressedFinal(ComboMouse mouse) {
				if (mouse.leftButton) {
					getContext().requestFocus();
					return true;
				} else { return false; }
			}
			@Override
			public ImagePanel getContext() {
				return (ImagePanel) super.getContext();
			}
		};
		this.addKeyListener(handler);
		this.addMouseListener(handler);
		this.addMouseMotionListener(handler);

		this.scroll = parentStoryspace.addModelChild(this);
	}

	@Override
	public StoryspaceScroll getStoryspaceScroll() { return scroll; }
	@Override
	public AbstractModel getFocusedChild() { return null; }
	@Override
	public StoryspaceScroll getModelParent() { return StoryspaceScroll.class.cast(getParent().getParent()); } // =D
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

	@Override
	public void getJsonRepresentation(JSONObject dict) { dict.put("imagePath", this.imagePath); }
	@Override
	public ImagePanel reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.loadImage(new File(jsObject.getString("imagePath")));
		return this;
	}

	// event handles

	private void loadImage(File file) {
		this.imagePath = file.getAbsolutePath();
		this.image = ImageStorage.inst().openImage(file);

		scroll.setTitle(file.getName());

		this.remove(imageLabel);
		this.add(imageLabel = new JLabel(new ImageIcon(image)));
		this.validate();
	}

	final private Consumer<Combo> makeOpenFileDialog(Consumer<File> lambda) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()));
		return combo -> {
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				lambda.accept(chooser.getSelectedFile());
			}
		};
	}
}
