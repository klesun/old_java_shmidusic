package Model.Staff.StaffConfig;


import Gui.ImageStorage;

import javax.swing.*;
import java.awt.*;

public class ConfigDialog extends JPanel {

	StaffConfig parent = null;

	JTextField[] channelInstrumentInputList = new JTextField[10];
	JTextField[] channelVolumeInputList = new JTextField[10];

	JPanel channelGridPanel = new JPanel(new GridLayout(10, 3, 20, 20));

	public ConfigDialog(StaffConfig parent) {
		super();
		this.parent = parent;

		channelGridPanel.setPreferredSize(new Dimension(150, 400));
		this.add(channelGridPanel);

		for (int i = 0; i < 10; ++i) {
			channelGridPanel.add(new JLabel("      " + i));
			channelInstrumentInputList[i] = new JTextField();
			channelGridPanel.add(channelInstrumentInputList[i]); channelInstrumentInputList[i].setForeground(ImageStorage.getColorByChannel(i));

			channelVolumeInputList[i] = new JTextField();
			channelGridPanel.add(channelVolumeInputList[i]); channelVolumeInputList[i].setForeground(ImageStorage.getColorByChannel(i));
		}
	}

	public void showMenuDialog() {

		// TODO: use float instead of %

		for (int i = 0; i < 10; ++i) {
			channelInstrumentInputList[i].setText(parent.getInstrumentArray()[i] + "");
			channelVolumeInputList[i].setText(parent.getVolumeArray()[i] + "");
		}

		int option = JOptionPane.showConfirmDialog(null, this, "Enter instruments for channels", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) { confirmChanges(); }
	}

	private void confirmChanges() {
		for (int i = 0; i < 10; ++i) {
			int instrument = limit(Integer.parseInt(channelInstrumentInputList[i].getText()), 0, 127);
			int volume = limit(Integer.parseInt(channelVolumeInputList[i].getText()), 0, 100);

			parent.getInstrumentArray()[i] = instrument;
			parent.getVolumeArray()[i] = volume;
		};
		parent.syncSyntChannels();
	}

	private static int limit(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}
}
