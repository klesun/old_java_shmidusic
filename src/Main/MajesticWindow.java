package Main;

import Model.Combo;
import Storyspace.Staff.StaffPanel;
import Storyspace.Storyspace;
import Stuff.OverridingDefaultClasses.Scroll;

import javax.swing.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MajesticWindow extends JFrame {

	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public Boolean isFullscreen = false;

	public JPanel cards = new JPanel();

	public enum cardEnum {
		CARDS_FULLSCREEN,
		CARDS_STORYSPACE,
		CARDS_TERMINAL,
	}

	public StaffPanel fullscreenStaffPanel;
	public Storyspace storyspace;
	public JTextArea terminal;

	public MajesticWindow() {
		super("Да будет такая музыка!"); //Заголовок окна
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT);

		// TODO: maybe just make window have CardLayout ? why having leak container ????
		cards.setLayout(new CardLayout());
		this.add(cards);

		terminal = new JTextArea("zhopa");
		terminal.setEditable(false);
		cards.add(terminal, cardEnum.CARDS_TERMINAL.name());

		this.setVisible(true);
	}

	// this method should be called only once
	public void init() {
		cards.add(storyspace = new Storyspace(this), cardEnum.CARDS_STORYSPACE.name());
		cards.add(new Scroll(fullscreenStaffPanel = new StaffPanel(this)), cardEnum.CARDS_FULLSCREEN.name());

		// for user-friendship there will be one initial staff
		storyspace.addMusicBlock(Combo.makeFake()).switchFullscreen();
	}

	public void switchTo(cardEnum card) {
		((CardLayout)cards.getLayout()).show(cards, card.name());
	}
}