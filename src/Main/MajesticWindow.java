package Main;

import Model.Combo;
import Storyspace.Staff.StaffPanel;
import Storyspace.Storyspace;
import Stuff.OverridingDefaultClasses.Scroll;

import javax.swing.*;

import java.awt.*;

public class MajesticWindow extends JFrame {

	public StaffPanel fullscreenStaffPanel;
	public Storyspace storyspace;

	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public Boolean isFullscreen = false;

	public JPanel cards = new JPanel();

	final public static String CARDS_FULLSCREEN = "fullscreen";
	final public static String CARDS_STORYSPACE = "storyspace";

	public MajesticWindow() {
		super("Да будет такая музыка!"); //Заголовок окна
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(XP_MINWIDTH, XP_MINHEIGHT);

		cards.setLayout(new CardLayout());
		this.add(cards);

		cards.add(storyspace = new Storyspace(this), CARDS_STORYSPACE);
		cards.add(new Scroll(fullscreenStaffPanel = new StaffPanel(this)), CARDS_FULLSCREEN);

		// for user-friendship there will be one initial staff
		storyspace.addMusicBlock(Combo.makeFake()).switchFullscreen(Combo.makeFake());
	}
}