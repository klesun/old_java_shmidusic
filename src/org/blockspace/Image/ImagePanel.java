package org.blockspace.Image;

import org.blockspace.BlockSpace;
import org.blockspace.IBlockSpacePanel;
import org.blockspace.Block;
import org.klesun_model.*;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;
import org.sheet_midusic.stuff.tools.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;

public class ImagePanel extends JPanel implements IBlockSpacePanel {

	private BufferedImage image = null;
	private JLabel imageLabel = null;
	private String imagePath = "";

	private Block scroll = null;
	private AbstractHandler handler = null;
	private Helper modelHelper = new Helper(this);

	private static TruMap<Combo, ContextAction<ImagePanel>> actionMap = new TruMap<>();
	static {
		ContextAction<ImagePanel> openImage = new ContextAction<>();
		actionMap.p(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_O), openImage.setRedo(ImagePanel::makeOpenFileDialog));
	}

	public ImagePanel(BlockSpace parentBlockSpace) {
		setFocusable(true);
		this.add(imageLabel = new JLabel("Image not loaded"));

		handler = new AbstractHandler(this) {

			@Override
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() { return actionMap; }

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
	public Block getParentBlock() { return scroll; }
	@Override
	public IComponent getFocusedChild() { return null; }
	@Override
	public Block getModelParent() { return Block.class.cast(getParent().getParent()); } // =D
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

	@Override
	public JSONObject getJsonRepresentation() {
		return new JSONObject().put("imagePath", this.imagePath);
	}
	@Override
	public ImagePanel reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.loadImage(new File(jsObject.getString("imagePath")));
		return this;
	}

	// event handles

	private void loadImage(File file) {
		this.imagePath = file.getAbsolutePath();
		try {
			this.image = getParentBlock().getModelParent().getImageStorage().openRandomImage(file.toURI().toURL());
		} catch (MalformedURLException exc) { Logger.fatal(exc, "No WAI"); }

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
