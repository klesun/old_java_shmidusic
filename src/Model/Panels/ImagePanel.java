package Model.Panels;

import Model.AbstractHandler;
import Model.AbstractModel;
import Model.Combo;
import Model.IHandlerContext;
import Tools.FileProcessor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ImagePanel extends JPanel implements IHandlerContext {

	private BufferedImage image = null;
	private JLabel imageLabel = null;

	public ImagePanel() {
		this.add(imageLabel = new JLabel("Image not loaded"));
		addKeyListener(new AbstractHandler(this) {
			@Override
			protected void init() {
				addCombo(ctrl, k.VK_O).setDo(makeOpenFileDialog(getContext()::loadImage));
			}
			@Override
			public ImagePanel getContext() { return (ImagePanel)super.getContext(); }
		});

		setFocusable(true);
	}

	@Override
	public AbstractModel getFocusedChild() { return null; }
	@Override
	public IHandlerContext getModelParent() { return null; }

	// event handles

	private void loadImage(File file) {
		// TODO: cache images with common path in ImageStorage (10 MiB images take 70 MiB of RAM)
		try { image = ImageIO.read(file); }
		catch (IOException e) { System.out.println("Failed to load image file! " + file.getAbsolutePath()); }

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
