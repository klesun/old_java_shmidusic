package org.sheet_midusic.staff.staff_config;

import org.klesun_model.AbstractHandler;
import org.klesun_model.Combo;
import org.klesun_model.ContextAction;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.staff_panel.StaffComponent;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;

import java.awt.*;
import java.util.LinkedHashMap;

public class StaffConfigComponent extends MidianaComponent
{
	final private StaffConfig staffConfig;

	public StaffConfigComponent(StaffComponent parent, StaffConfig staffConfig) {
		super(parent);  /** @deprecated */
		this.staffConfig = staffConfig;
	}

	@Override
	public MidianaComponent getFocusedChild() { return null; }

	@Override
	protected AbstractHandler makeHandler() {
		return new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
				return new TruMap<>();
			}
		};
	}

	@Override
	public int drawOn(Graphics2D surface, int x, int y) {
		// TODO: move it here from StaffConfig
		return -100;
	}
}
