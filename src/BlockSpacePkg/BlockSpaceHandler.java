package BlockSpacePkg;

import Model.*;
import Stuff.OverridingDefaultClasses.TruMap;
import Stuff.Tools.FileProcessor;
import Stuff.Tools.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class BlockSpaceHandler extends AbstractHandler {

	public BlockSpaceHandler(BlockSpace context) { super(context); }

	private static TruMap<Combo, ContextAction<BlockSpace>> actionMap = new TruMap<>();
	static {
		JFileChooser jsonChooser = new JFileChooser("/home/klesun/yuzefa_git/storyspaceContent/");
		jsonChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".bs.json") || f.isDirectory();
			}

			public String getDescription() {
				return "Json BlockSpace data";
			}
		});

		actionMap
			.p(new Combo(ctrl, k.VK_M), mkAction(BlockSpace::addMusicBlock).setCaption("Create Staff Block"))
			.p(new Combo(ctrl, k.VK_T), mkAction(BlockSpace::addTextBlock).setCaption("Create Article Block"))
			.p(new Combo(ctrl, k.VK_I), mkAction(BlockSpace::addImageBlock).setCaption("Create Image Block"))

			.p(new Combo(ctrl, k.VK_G), mkFailableAction(FileProcessor::saveStoryspace).setCaption("Save Whole Project"))
			.p(new Combo(ctrl, k.VK_R), mkFailableAction(bs -> jsonChooser.showOpenDialog(bs.getWindow()) == JFileChooser.APPROVE_OPTION
				? FileProcessor.openStoryspace(jsonChooser.getSelectedFile(), bs)
				: new Explain("You changed your mind. Why?")).setCaption("Reconstruct From a Project File"))

			.p(new Combo(ctrl, k.VK_EQUALS), mkAction(bs -> bs.scale(1)).setCaption("Scale Up"))
			.p(new Combo(ctrl, k.VK_MINUS), mkAction(bs -> bs.scale(-1)).setCaption("Scale Down"))
			;
	}

	@Override
	public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
		return actionMap;
	}

	private static ContextAction<BlockSpace> mkAction(Consumer<BlockSpace> lambda) {
		ContextAction<BlockSpace> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<BlockSpace> mkFailableAction(Function<BlockSpace, Explain> lambda) {
		ContextAction<BlockSpace> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	@Override
	public Boolean mousePressedFinal(ComboMouse mouse) {
		if (mouse.leftButton) {
			getContext().requestFocus();
		}
		return true;
	}
	@Override
	public Boolean mouseDraggedFinal(ComboMouse mouse) {
		Component eventOrigin = getFirstParentComponent(mouse.getOrigin());
		eventOrigin.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

		Arrays.asList(getContext().getComponents()).stream().forEach(component
			-> component.setLocation(component.getX() + mouse.dx, component.getY() + mouse.dy));
		mouseLocation.move(mouse.dx, mouse.dy);
		return true;
	}
	@Override
	public Boolean mouseReleasedFinal(ComboMouse mouse) {
		IComponentModel eventOrigin = (IComponentModel)getFirstParentComponent(mouse.getOrigin());
		eventOrigin.setCursor(eventOrigin.getModelHelper().getDefaultCursor());
		return true;
	}

	@Override
	public BlockSpace getContext() { return (BlockSpace)super.getContext(); }

	private static Component getFirstParentComponent(IModel model) {
		while (!(model instanceof Component)) {
			if (model == null) { Logger.fatal("orphan model detected! " + model.getClass().getSimpleName()); }
			model = model.getModelParent();
		}
		return Component.class.cast(model);
	}
}
