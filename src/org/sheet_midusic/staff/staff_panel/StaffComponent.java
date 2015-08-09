package org.sheet_midusic.staff.staff_panel;

import org.klesun_model.AbstractHandler;
import org.klesun_model.IComponent;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.StaffHandler;
import org.sheet_midusic.staff.StaffPainter;

import java.awt.*;

// TODO: merge with AbstractPainter
public class StaffComponent extends MidianaComponent
{
	final public Staff staff;

	public StaffComponent(Staff staff) {
		super(null); /** @deprecated  */
		this.staff = staff;
	}

	@Override
	public MidianaComponent getFocusedChild()
	{
		// TODO: pointer should be here, not in Model
		return staff.getFocusedAccord();
	}

	@Override
	protected AbstractHandler makeHandler()
	{
		return new StaffHandler(this);
	}

	@Override
	public int drawOn(Graphics2D g, int x, int y)
	{
		new StaffPainter(this, g, x, y).draw(true);
		return staff.getHeightIf(-100);
	}
}
