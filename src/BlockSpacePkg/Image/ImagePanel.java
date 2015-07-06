package BlockSpacePkg.Image;

import BlockSpacePkg.StaffPkg.StaffPanel;
import Main.Main;
import Model.*;
import BlockSpacePkg.BlockSpace;
import BlockSpacePkg.IBlockSpacePanel;
import BlockSpacePkg.Block;
import Stuff.OverridingDefaultClasses.TruMap;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImagePanel extends JPanel implements IBlockSpacePanel {

	private BufferedImage image = null;
	private JLabel imageLabel = null;
	private String imagePath = "";

	private Block scroll = null;
	private AbstractHandler handler = null;
	private Helper modelHelper = new Helper(this);

	public ImagePanel(BlockSpace parentBlockSpace) {
		setFocusable(true);
		this.add(imageLabel = new JLabel("Image not loaded"));

		handler = new AbstractHandler(this) {

			@Override
			public LinkedHashMap<Combo, ContextAction> getStaticActionMap() {
				ContextAction<ImagePanel> openImage = new ContextAction<>();
				return new TruMap<>().p(new Combo(ctrl, k.VK_O), openImage.setRedo(ImagePanel::makeOpenFileDialog));
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

		this.scroll = parentBlockSpace.addModelChild(this);
	}

	@Override
	public Block getScroll() { return scroll; }
	@Override
	public IComponentModel getFocusedChild() { return null; }
	@Override
	public Block getModelParent() { return Block.class.cast(getParent().getParent()); } // =D
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
		this.image = getScroll().getModelParent().getImageStorage().openImage(file);

		scroll.setTitle(file.getName());

		this.remove(imageLabel);
		this.add(imageLabel = new JLabel(new ImageIcon(image)));
		this.validate();
	}

	private static Explain makeOpenFileDialog(ImagePanel ip) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()));

		if (chooser.showOpenDialog(ip) == JFileChooser.APPROVE_OPTION) {
			ip.loadImage(chooser.getSelectedFile());
		}
		return new Explain(true);
	}
}
