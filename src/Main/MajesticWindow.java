package Main;

import Gui.Constants;
import Model.ActionFactory;
import Model.Combo;
import Model.IComponentModel;
import Model.IModel;
import Storyspace.Staff.StaffPanel;
import Storyspace.Storyspace;
import Stuff.OverridingDefaultClasses.Scroll;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class MajesticWindow extends JFrame {

	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public JPanel cards = new JPanel();
	private JMenuBar menuBar;

	public enum cardEnum {
		CARDS_STORYSPACE,
		CARDS_TERMINAL,
	}

	public Storyspace storyspace;
	public JTextArea terminal;

	public MajesticWindow() {
		super("Да будет такая музыка!"); //Заголовок окна
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT);

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
		cards.add(storyspace = new Storyspace(this), cardEnum.CARDS_STORYSPACE.name());

		addMenuBar();

		switchTo(cardEnum.CARDS_STORYSPACE);
		// for user-friendship there will be one initial staff
		storyspace.addMusicBlock(Combo.makeFake()).getStoryspaceScroll().switchFullscreen();

	}

	private void addMenuBar() {
		this.menuBar = new JMenuBar();
		addMenuItems(new Storyspace(this));
		setJMenuBar(menuBar);
	}

	private void addMenuItems(IComponentModel fakeModelForClassMethods) {

		JMenu modelMenu = new JMenu(fakeModelForClassMethods.getClass().getSimpleName());

		for (Map.Entry<Combo, ActionFactory> entry: fakeModelForClassMethods.getHandler().getActionMap().entrySet()) {

			if (entry.getValue().omitMenuBar()) {
				continue;
			}

			JMenuItem eMenuItem = new JMenuItem("Do Action: " + entry.getKey().toString());
			eMenuItem.setFont(Constants.PROJECT_FONT);
			eMenuItem.setMnemonic(entry.getKey().getKeyCode());
			eMenuItem.setToolTipText("No description");
			eMenuItem.addActionListener(event -> entry.getValue().createAction().doDo());

			modelMenu.add(eMenuItem);
		}
		menuBar.add(modelMenu);

		for (IComponentModel child: fakeModelForClassMethods.getModelHelper().makeFakePossibleChildListForClassMethods()) {
			addMenuItems(child);
		}
	}

	// the Great idea behind this is to refresh menu bar each time we change focus
	// i.e. when we're pointing Nota we have Menus: [Storyspace, Scroll, Staff, Accord, Nota], when Paragraph - [Storyspace, Scroll, Article, Paragraph] etc
	public void updateMenuBar() {

		// it was actually a bad idea to reconstruct this menu each time we move focus
		// since now i think, it should statically have all possible models, just not main route models should be greyed out

//		menuBar.removeAll();
//		int mnemonic = 1;
//
//		IComponentModel model = storyspace;
//		while (model != null) {
//
//			JMenu modelMenu = new JMenu(model.getClass().getSimpleName());
//			if (mnemonic <= 9) { modelMenu.setMnemonic(KeyEvent.VK_0 + mnemonic++); }
//
//			for (Map.Entry<Combo, ActionFactory> entry: model.getHandler().getActionMap().entrySet()) {
//
//				if (entry.getValue().omitMenuBar()) {
//					continue;
//				}
//
//				JMenuItem eMenuItem = new JMenuItem("Do Action: " + entry.getKey().toString());
//				eMenuItem.setFont(Constants.PROJECT_FONT);
//				eMenuItem.setMnemonic(entry.getKey().getKeyCode());
//				eMenuItem.setToolTipText("No description");
//				eMenuItem.addActionListener(event -> entry.getValue().createAction().doDo());
//
//				modelMenu.add(eMenuItem);
//			}
//			menuBar.add(modelMenu);
//
//			model = model.getFocusedChild();
//		}
//
//		menuBar.validate();
//		menuBar.repaint();
	}

	public void switchTo(cardEnum card) {
		((CardLayout)cards.getLayout()).show(cards, card.name());
	}
}