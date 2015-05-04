package Model.Containers;

import Model.Combo;
import Model.Containers.Panels.MusicPanel;
import OverridingDefaultClasses.Scroll;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class MajesticWindow extends JFrame implements ActionListener {

	public MusicPanel musicPanel;
	public Storyspace storyspace;

	int XP_MINWIDTH = 1024;
	int XP_MINHEIGHT = 540; // my beloved netbook

	public Boolean isFullscreen = true;

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
		cards.add(new Scroll(musicPanel = new MusicPanel(storyspace).hideGracefully()), CARDS_FULLSCREEN);

		// for user-friendship there will be one initial staff
		storyspace.addMusicBlock(Combo.makeFake()).switchFullscreen(Combo.makeFake());
		musicPanel.switchFullscreen(Combo.makeFake());
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println(evt);
	}
}