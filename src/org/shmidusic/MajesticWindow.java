package org.shmidusic;

import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.StaffHandler;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.chord.ChordComponent;
import org.shmidusic.sheet_music.staff.chord.note.Note;
import org.shmidusic.sheet_music.staff.chord.note.NoteComponent;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.sheet_music.SheetMusicComponent;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.stuff.midi.DumpReceiver;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.klesun_model.Combo;
import org.klesun_model.ContextAction;
import org.klesun_model.Explain;
import org.klesun_model.IComponent;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMenuItem;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class MajesticWindow extends JFrame {

	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public JPanel cards = new JPanel();
	public JMenuBar menuBar;
	private Map<Class<? extends IComponent>, JMenu> menus = new HashMap<>();

	public enum cardEnum {
		CARDS_STORYSPACE,
		CARDS_TERMINAL,
		CARDS_SHEET_MIDUSIC
	}

//	public BlockSpace blockSpace;
	public MainPanel shmidusicPanel;
	public JTextArea terminal;

	public MajesticWindow()
	{
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
		cards.add(shmidusicPanel = new MainPanel(), cardEnum.CARDS_SHEET_MIDUSIC.name());
		addMenuBar();
		switchTo(cardEnum.CARDS_SHEET_MIDUSIC);
		shmidusicPanel.sheetContainer.requestFocus();
		DumpReceiver.eventHandler = (StaffHandler) shmidusicPanel.sheetContainer.getFocusedChild().getHandler();

		updateMenuBar();
	}

	private void addMenuBar() {
		this.menuBar = new JMenuBar();
		addMenuItems(new SheetMusicComponent(new SheetMusic(), new MainPanel()));
		shmidusicPanel.northPanel.add(menuBar, BorderLayout.WEST);
	}

	private void addMenuItems(IComponent fakeModelForClassMethods)
	{
		LinkedHashMap<Combo, ContextAction> actionMap = fakeModelForClassMethods.getHandler().getMyClassActionMap();

		Class<? extends IComponent> contextClass = fakeModelForClassMethods.getClass();

		if (actionMap.values().stream().anyMatch(a -> !a.omitMenuBar())) {

			JMenu modelMenu = new JMenu(fakeModelForClassMethods.getModel().getClass().getSimpleName());
			modelMenu.setToolTipText("Enabled");

			actionMap.entrySet().stream()
					.filter(e -> !e.getValue().omitMenuBar())
					.map(e -> makeActionMenuItem(e.getKey(), e.getValue(), contextClass))
					.forEach(modelMenu::add);

			menuBar.add(modelMenu);
			menus.put(contextClass, modelMenu);
		}

		for (IComponent child: makeFakePossibleChildListForClassMethods(fakeModelForClassMethods)) {
			addMenuItems(child);
		}
	}

	private JMenuItem makeActionMenuItem(Combo key, ContextAction action, Class<? extends IComponent> contextClass)
	{
		String caption = action.getCaption() != null ? action.getCaption() : "Do Action:";
		JMenuItem eMenuItem = new TruMenuItem(caption);
		eMenuItem.setToolTipText("No description");
		eMenuItem.setAccelerator(key.toKeystroke());

		eMenuItem.addActionListener(event ->
		{
			IComponent context = findFocusedByClass(contextClass);
			if (context != null) {
				Explain explain = action.redo(context);
				if (explain.isSuccess()) {
					// TODO: ctrl-z/ctrl-y probably should not be normal action at all!
					if (!key.equals(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Z)) &&
						!key.equals(new Combo(KeyEvent.CTRL_MASK, KeyEvent.VK_Y)))
					{
						shmidusicPanel.snapshotStorage.add(context.getModel().getJsonRepresentation());
					}
					updateMenuBar();
				} else {
					JOptionPane.showMessageDialog(this, explain.getExplanation());
				}
			} else {
				JOptionPane.showMessageDialog(this, "Cant perform action, " + contextClass.getSimpleName() + " class instance not focused!");
			}
		});

		return eMenuItem;
	}

	private static List<IComponent> makeFakePossibleChildListForClassMethods(IComponent parent) {
		if (parent.getClass() == SheetMusicComponent.class) {
			return Arrays.asList(new StaffComponent(new Staff(), (SheetMusicComponent)parent));
		} else if (parent.getClass() == StaffComponent.class) {
			return Arrays.asList(new ChordComponent(new Chord(), parent));
		} else if (parent.getClass() == ChordComponent.class) {
			return Arrays.asList(new NoteComponent(new Note(), (ChordComponent)parent));
		} else {
			return new ArrayList<>();
		}
	}

	private IComponent findFocusedByClass(Class<? extends IComponent> cls)
	{
		IComponent result = shmidusicPanel.sheetContainer;
		while (result != null) {
			if (result.getClass() == cls) {
				break;
			} else {
				result = result.getFocusedChild();
			}
		}
		return result;
	}

	// the Great idea behind this is to refresh menu bar each time we change focus
	// i.e. when we're pointing note we have Menus: [BlockSpace, Scroll, staff, chord, note], when Paragraph - [BlockSpace, Scroll, article, Paragraph] etc
	public void updateMenuBar()
	{
		menus.values().forEach(m -> {
			m.setEnabled(false);
			m.setToolTipText("Instance Not Focused");
		});

		IComponent model = shmidusicPanel.sheetContainer;
		while (model != null) {
			if (menus.containsKey(model.getClass())) { // will be false for StaffComponent cuz i dont like it
				menus.get(model.getClass()).setEnabled(true);
				menus.get(model.getClass()).setToolTipText(null);
			}
			model = model.getFocusedChild();
		}
	}

	public void switchTo(cardEnum card) {
		((CardLayout)cards.getLayout()).show(cards, card.name());
	}
}
