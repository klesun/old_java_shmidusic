package Main;

import BlockSpacePkg.Block;
import Model.*;
import BlockSpacePkg.BlockSpace;
import Stuff.OverridingDefaultClasses.TruMenuItem;
import Stuff.Tools.Logger;

import javax.swing.*;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.*;

public class MajesticWindow extends JFrame {

	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public JPanel cards = new JPanel();
	private JMenuBar menuBar;
	private Map<Class<? extends IComponentModel>, JMenu> menus = new HashMap<>();
	private JMenuItem fullscreenMenuItem = null;

	private Component lastFocusedBeforeMenu = null;

	public enum cardEnum {
		CARDS_STORYSPACE,
		CARDS_TERMINAL,
	}

	public BlockSpace blockSpace;
	public JTextArea terminal;

	public MajesticWindow() {
		super("Да будет такая музыка!"); //Заголовок окна
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT);
		this.addWindowListener(new MajesticListener());

		// TODO: maybe just make window have CardLayout ? why having leak container ????
		cards.setLayout(new CardLayout());
		this.add(cards);

		terminal = new JTextArea("Midiana terminal at your service!");
		terminal.setEditable(false);
		cards.add(terminal, cardEnum.CARDS_TERMINAL.name());

		this.setVisible(true);
	}

	// this method should be called only once
	public void init() {
		cards.add(blockSpace = new BlockSpace(this), cardEnum.CARDS_STORYSPACE.name());
		addMenuBar();
		switchTo(cardEnum.CARDS_STORYSPACE);
		// for user-friendship there will be one initial staff
		blockSpace.addMusicBlock().getScroll().switchFullscreen();

		updateMenuBar();
	}

	private void addMenuBar() {
		this.menuBar = new JMenuBar();
		addMenuItems(new BlockSpace(this));
		setJMenuBar(menuBar);
		getRootPane().addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				lastFocusedBeforeMenu = e.getOppositeComponent();
			}
		});
	}

	private void addMenuItems(IComponentModel fakeModelForClassMethods) {

		LinkedHashMap<Combo, ContextAction> actionMap = fakeModelForClassMethods.getHandler().getMyClassActionMap();
		if (actionMap.values().stream().anyMatch(a -> !a.omitMenuBar())) {

			JMenu modelMenu = new JMenu(fakeModelForClassMethods.getClass().getSimpleName());
			modelMenu.setToolTipText("Enabled");

			for (Combo key : actionMap.keySet()) {
				ContextAction action = actionMap.get(key);

				if (action.omitMenuBar()) {
					continue;
				}

				String caption = action.getCaption() != null ? action.getCaption() : "Do Action:";
				JMenuItem eMenuItem = new TruMenuItem(caption);
				eMenuItem.setToolTipText("No description");
				eMenuItem.setAccelerator(key.toKeystroke());

				eMenuItem.addActionListener(event -> {
					Class<? extends IComponentModel> cls = fakeModelForClassMethods.getClass();
					IComponentModel context = findeFocusedByClass(cls);
					if (context != null) {
						Explain explain = action.redo(context);
						if (explain.isSuccess()) {
							updateMenuBar();
						} else {
							JOptionPane.showMessageDialog(this, explain.getExplanation());
						}
					} else {
						Logger.warning("Cant perform action, " + cls.getSimpleName() + " class instance not focused!");
					}
				});

				modelMenu.add(eMenuItem);
				if (key.equals(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_F))) {
					this.fullscreenMenuItem = eMenuItem;
				}
			}
			menuBar.add(modelMenu);
			menus.put(fakeModelForClassMethods.getClass(), modelMenu);
		}

		for (IComponentModel child: fakeModelForClassMethods.getModelHelper().makeFakePossibleChildListForClassMethods()) {
			addMenuItems(child);
		}
	}

	private IComponentModel findeFocusedByClass(Class<? extends IComponentModel> cls) {
		if (cls == BlockSpace.class) {
			return blockSpace;
		} else {
			IComponentModel result = blockSpace.getFocusedChild(lastFocusedBeforeMenu);
			while (result != null) {
				if (result.getClass() == cls) {
					break;
				} else {
					result = result.getFocusedChild();
				}
			}
			return result;
		}
	}

	// the Great idea behind this is to refresh menu bar each time we change focus
	// i.e. when we're pointing Nota we have Menus: [BlockSpace, Scroll, StaffPkg, Accord, Nota], when Paragraph - [BlockSpace, Scroll, ArticlePkg, Paragraph] etc
	public void updateMenuBar() {

		menus.values().forEach(m -> {
			m.setEnabled(false);
			m.setToolTipText("Instance Not Focused");
		});

		IComponentModel model = blockSpace;
		while (model != null) {
			if (menus.containsKey(model.getClass())) { // will be false for StaffPanel cuz i dont like it
				menus.get(model.getClass()).setEnabled(true);
				menus.get(model.getClass()).setToolTipText(null);
			}
			model = model.getFocusedChild();
		}

		if (blockSpace.getChildScrollList().stream().anyMatch(Block::isFullscreen)) {
			menus.get(BlockSpace.class).setEnabled(false);
			menus.get(BlockSpace.class).setToolTipText("Disabled In Fullscreen Mode");

			Arrays.stream(menus.get(Block.class).getMenuComponents()).forEach(e -> {
				e.setEnabled(false);
				((JMenuItem) e).setToolTipText("Disabled In Fullscreen Mode");
			});
			fullscreenMenuItem.setEnabled(true);
			fullscreenMenuItem.setToolTipText(null);
		} else {
			Arrays.stream(menus.get(Block.class).getMenuComponents()).forEach(e -> {
				e.setEnabled(true);
				((JMenuItem) e).setToolTipText(null);
			});
		}
	}

	public void switchTo(cardEnum card) {
		((CardLayout)cards.getLayout()).show(cards, card.name());
	}
}