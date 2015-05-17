package Storyspace.Staff.StaffConfig;


import Gui.ImageStorage;

import javax.swing.*;
import java.awt.*;

public class ConfigDialog extends JPanel {

	final private static int CHANNEL_COUNT = 10;

	StaffConfig parent = null;

	JTextField[] channelInstrumentInputList = new JTextField[CHANNEL_COUNT];
	JTextField[] channelVolumeInputList = new JTextField[CHANNEL_COUNT];
	JCheckBox[] channleMuteCheckboxList = new JCheckBox[CHANNEL_COUNT];

	JPanel channelGridPanel = new JPanel(new GridLayout(CHANNEL_COUNT + 1, 4, 20, 20));

	public ConfigDialog(StaffConfig parent) {
		super();
		this.parent = parent;

		channelGridPanel.setPreferredSize(new Dimension(200, 400));
		this.add(channelGridPanel);

		// header grid row


		// filling grid with cells
		for (int i = 0; i < 10; ++i) {
			channelGridPanel.add(new JLabel("      " + i));

			channelInstrumentInputList[i] = new JTextField();
			channelGridPanel.add(channelInstrumentInputList[i]); channelInstrumentInputList[i].setForeground(ImageStorage.getColorByChannel(i));

			channelVolumeInputList[i] = new JTextField();
			channelGridPanel.add(channelVolumeInputList[i]); channelVolumeInputList[i].setForeground(ImageStorage.getColorByChannel(i));

			channleMuteCheckboxList[i] = new JCheckBox();
			channelGridPanel.add(channleMuteCheckboxList[i]);
		}
	}

	public void showMenuDialog() {

		// TODO: use float instead of %

		for (int i = 0; i < CHANNEL_COUNT; ++i) {
			channelInstrumentInputList[i].setText(parent.getInstrumentArray()[i] + "");
			channelVolumeInputList[i].setText(parent.getVolumeArray()[i] + "");
			channleMuteCheckboxList[i].setSelected(parent.getMuteFlagArray()[i]);
		}

		int option = JOptionPane.showConfirmDialog(null, this, "Enter instruments for channels", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) { confirmChanges(); }
	}

	private void confirmChanges() {
		for (int i = 0; i < CHANNEL_COUNT; ++i) {
			int instrument = limit(Integer.parseInt(channelInstrumentInputList[i].getText()), 0, 127);
			int volume = limit(Integer.parseInt(channelVolumeInputList[i].getText()), 0, 100);

			parent.getInstrumentArray()[i] = instrument;
			parent.getVolumeArray()[i] = volume;
			parent.getMuteFlagArray()[i] = channleMuteCheckboxList[i].isSelected();
		};
		parent.syncSyntChannels();
	}

	private static int limit(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}
}
