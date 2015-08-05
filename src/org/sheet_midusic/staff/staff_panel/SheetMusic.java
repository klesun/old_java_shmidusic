package org.sheet_midusic.staff.staff_panel;

import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.sheet_midusic.staff.Staff;

import java.util.ArrayList;

/** SheetMusic is container of Staff-s */
public class SheetMusic extends AbstractModel
{
	public Arr<Staff> staffList = new Arr<>("staffList", new ArrayList<>(), this, Staff.class);

	public Staff addNewStaffAfter(Staff staff)
	{
		Staff newStaff = new Staff(null);
		staffList.add(newStaff, staffList.indexOf(staff) + 1);
		return newStaff;
	}
}
