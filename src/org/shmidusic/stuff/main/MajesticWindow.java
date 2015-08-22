package org.shmidusic.stuff.main;

import org.shmidusic.staff.Staff;
import org.shmidusic.staff.StaffHandler;
import org.shmidusic.staff.chord.Chord;
import org.shmidusic.staff.chord.ChordComponent;
import org.shmidusic.staff.chord.nota.Nota;
import org.shmidusic.staff.chord.nota.NoteComponent;
import org.shmidusic.staff.staff_panel.MainPanel;
import org.shmidusic.staff.staff_panel.SheetMusic;
import org.shmidusic.staff.staff_panel.SheetMusicComponent;
import org.shmidusic.staff.staff_panel.StaffComponent;
import org.shmidusic.stuff.Midi.DumpReceiver;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.klesun_model.Combo;
import org.klesun_model.ContextAction;
import org.klesun_model.Explain;
import org.klesun_model.IComponent;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMenuItem;

import javax.swing.*;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class MajesticWindow extends JFrame {

	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public JPanel cards = new JPanel();
	private JMenuBar menuBar;
	private Map<Class<? extends IComponent>, JMenu> menus = new HashMap<>();
	private JMenuItem fullscreenMenuItem = null;

	private Component lastFocusedBeforeMenu = null;

	public enum cardEnum {
		CARDS_STORYSPACE,
		CARDS_TERMINAL,
		CARDS_SHEET_MIDUSIC
	}

//	public BlockSpace blockSpace;
	public MainPanel staffPanel;
	public JTextArea terminal;

	public MajesticWindow() {
		super("Да будет такая музыка!");
		this.setIconImage(ImageStorage.openImageUncached("midusic.png"));
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
		cards.add(staffPanel = new MainPanel(), cardEnum.CARDS_SHEET_MIDUSIC.name());
//		cards.add(blockSpace = new BlockSpace(this), cardEnum.CARDS_STORYSPACE.name());
		addMenuBar();
		switchTo(cardEnum.CARDS_SHEET_MIDUSIC);
		staffPanel.requestFocus();
		DumpReceiver.eventHandler = (StaffHandler)staffPanel.staffContainer.getFocusedChild().getHandler();;
		// for user-friendship there will be one initial org.shmidusic.staff
//		blockSpace.addMusicBlock().getParentBlock().switchFullscreen();

		updateMenuBar();
	}

	private void addMenuBar() {
		this.menuBar = new JMenuBar();
		addMenuItems(new MainPanel());
		setJMenuBar(menuBar);
		getRootPane().addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				lastFocusedBeforeMenu = e.getOppositeComponent();
			}
		});
	}

	private void addMenuItems(IComponent fakeModelForClassMethods)
	{
		LinkedHashMap<Combo, ContextAction> actionMap = fakeModelForClassMethods.getHandler().getMyClassActionMap();
		if (actionMap.values().stream().anyMatch(a -> !a.omitMenuBar())) {

			JMenu modelMenu = new JMenu(fakeModelForClassMethods.getModel().getClass().getSimpleName());
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
					Class<? extends IComponent> cls = fakeModelForClassMethods.getClass();
					IComponent context = findeFocusedByClass(cls);
					if (context != null) {
						Explain explain = action.redo(context);
						if (explain.isSuccess()) {
							updateMenuBar();
						} else {
							JOptionPane.showMessageDialog(this, explain.getExplanation());
						}
					} else {
						JOptionPane.showMessageDialog(this, "Cant perform action, " + cls.getSimpleName() + " class instance not focused!");
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

		for (IComponent child: makeFakePossibleChildListForClassMethods(fakeModelForClassMethods)) {
			addMenuItems(child);
		}
	}


	// retarded language does not support overridable class methods
	private static List<IComponent> makeFakePossibleChildListForClassMethods(IComponent parent) {
		/*if (this.getClass() == BlockSpace.class) {
			return Arrays.asList(new Block(new ImagePanel((BlockSpace)this), (BlockSpace)this));
		} else if (this.getClass() == Block.class) {
			Block scroll = (Block)this;
			return Arrays.asList(new MainPanel(scroll.getModelParent()), new Article(scroll.getModelParent()), new ImagePanel(scroll.getModelParent()));
		} else if (this.getClass() == Article.class) {
			return Arrays.asList(new Paragraph((Article)this));
		} else */if (parent.getClass() == MainPanel.class) {
			return Arrays.asList(new SheetMusicComponent(new SheetMusic(), (MainPanel)parent));
		} else if (parent.getClass() == SheetMusicComponent.class) {
			return Arrays.asList(new StaffComponent(new Staff(), (MainPanel)parent.getModelParent()));
		} else if (parent.getClass() == StaffComponent.class) {
			return Arrays.asList(new ChordComponent(new Chord(), parent));
		} else if (parent.getClass() == ChordComponent.class) {
			return Arrays.asList(new NoteComponent(new Nota(), (ChordComponent)parent));
		} else {
			return new ArrayList<>();
		}
	}

	private IComponent findeFocusedByClass(Class<? extends IComponent> cls) {
		if (cls == MainPanel.class) {
			return staffPanel;
		} else {
			IComponent result = staffPanel.getFocusedChild();
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
	// i.e. when we're pointing nota we have Menus: [BlockSpace, Scroll, org.shmidusic.staff, chord, nota], when Paragraph - [BlockSpace, Scroll, article, Paragraph] etc
	public void updateMenuBar() {

		menus.values().forEach(m -> {
			m.setEnabled(false);
			m.setToolTipText("Instance Not Focused");
		});

		IComponent model = staffPanel;
		while (model != null) {
			if (menus.containsKey(model.getClass())) { // will be false for StaffComponent cuz i dont like it
				menus.get(model.getClass()).setEnabled(true);
				menus.get(model.getClass()).setToolTipText(null);
			}
			model = model.getFocusedChild();
		}

//		if (blockSpace.getChildScrollList().stream().anyMatch(Block::isFullscreen)) {
//			menus.get(BlockSpace.class).setEnabled(false);
//			menus.get(BlockSpace.class).setToolTipText("Disabled In Fullscreen Mode");
//
//			Arrays.stream(menus.get(Block.class).getMenuComponents()).forEach(e -> {
//				e.setEnabled(false);
//				((JMenuItem) e).setToolTipText("Disabled In Fullscreen Mode");
//			});
//			fullscreenMenuItem.setEnabled(true);
//			fullscreenMenuItem.setToolTipText(null);
//		} else {
//			Arrays.stream(menus.get(Block.class).getMenuComponents()).forEach(e -> {
//				e.setEnabled(true);
//				((JMenuItem) e).setToolTipText(null);
//			});
//		}
	}

	public void switchTo(cardEnum card) {
		((CardLayout)cards.getLayout()).show(cards, card.name());
	}
}