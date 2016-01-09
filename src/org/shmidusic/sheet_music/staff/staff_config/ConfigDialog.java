package org.shmidusic.sheet_music.staff.staff_config;


import org.klesun_model.IModel;
import org.klesun_model.field.IField;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;
import org.shmidusic.stuff.OverridingDefaultClasses.ModelFieldInput;
import org.shmidusic.stuff.OverridingDefaultClasses.TruLabel;
import org.shmidusic.Main;
import org.shmidusic.stuff.tools.Fp;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConfigDialog extends JPanel {

	final private static int CELL_HEIGHT = 30;
	final private static int CELL_WIDTH = 75;

	final private static int PROPERTY_CELL_HEIGHT = 40;
	final private static int PROPERTY_CELL_WIDTH = 100;

	List<ModelFieldInput> inputList = new ArrayList<>();

	StaffConfig parent = null;

	public ConfigDialog(StaffConfig parent) {
		super();
		this.parent = parent;

		this.addChannelSetupGrid();
		this.addPropertyGrid();
	}

	public void showMenuDialog(Consumer<AbstractModel> onConfirm) {
		int option = JOptionPane.showConfirmDialog(null, this, "Enter instruments for channels", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			confirmChanges();
			onConfirm.accept(parent);

			/** @hack - temporary */
			Main.window.shmidusicPanel.sheetContainer.repaint();
		}
	}

	private void addChannelSetupGrid()
	{
		// hate java for not having class methods
		Set<String> fieldList = parent.channelList.get(0).getFieldList();

		JPanel channelGridPanel = new JPanel(new GridLayout(Channel.CHANNEL_COUNT + 1, fieldList.size(), 4, 4)); // + 2 чтоб наверняка
		channelGridPanel.setPreferredSize(new Dimension(fieldList.size() * CELL_WIDTH, (Channel.CHANNEL_COUNT + 1) * CELL_HEIGHT));
		channelGridPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.DARK_GRAY));
		this.add(channelGridPanel);

		// header grid row
		for (String header: fieldList) { channelGridPanel.add(new TruLabel(Fp.splitCamelCase(header))); }

		parent.getFieldStorage().entrySet().stream().filter(e -> e.getValue() instanceof Arr).forEach(e ->
		{
			Arr arr = (Arr)e.getValue();
			for (int i = 0; i < arr.size(); ++i) {

				IModel model = arr.get(i);
				for (Map.Entry<String,IField> ec: model.getFieldStorage().entrySet()) {
					JComponent input = checkEm(new ModelFieldInput((Field)ec.getValue()));
					input.setForeground(ImageStorage.getColorByChannel(i));
					channelGridPanel.add(input);
				}
			}
		});
	}

	private void addPropertyGrid()
	{
		List<Map.Entry<String,IField>> propertyList = parent.getFieldStorage().entrySet().stream()
				.filter(e -> !(e.getValue() instanceof Arr)).collect(Collectors.toList());

		JPanel propertyGridPanel = new JPanel(new GridLayout(propertyList.size() + 1, 2, 4, 4));
		propertyGridPanel.setPreferredSize(new Dimension(2 * PROPERTY_CELL_WIDTH, (propertyList.size() + 1) * PROPERTY_CELL_HEIGHT));
		this.add(propertyGridPanel);

		String[] gridHeaders = new String[]{"Property", "Value"};
		// header grid row
		for (String header: gridHeaders) { propertyGridPanel.add(new TruLabel(header)); }

		// filling grid with cells
		for (Map.Entry<String,IField> field: propertyList) {
			propertyGridPanel.add(new TruLabel(Fp.splitCamelCase(field.getKey())));
			propertyGridPanel.add(checkEm(new ModelFieldInput((Field)field.getValue())));
		}
	}

	private void confirmChanges() {
		for (ModelFieldInput input: inputList) {
			if (!input.getOwner().isFinal()) {
				input.getOwner().setJsonValue(input.getValue());
			}
		}
	}

	private JComponent checkEm(ModelFieldInput input) {
		this.inputList.add(input);
		return input.getComponent();
	}
}
