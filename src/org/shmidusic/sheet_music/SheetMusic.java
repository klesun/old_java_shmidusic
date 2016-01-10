package org.shmidusic.sheet_music;

import org.json.JSONObject;
import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.shmidusic.sheet_music.staff.Staff;

import java.util.ArrayList;

/** SheetMusic is container of Staff-s */
public class SheetMusic extends AbstractModel
{
	public Arr<Staff> staffList = add("staffList", new ArrayList<>(), Staff.class);

	public SheetMusic(JSONObject state) {
		this();
		reconstructFromJson(state);
	}

	public SheetMusic()
	{
		Staff staff = new Staff();
		this.staffList.add(staff);
	}

	public Staff addNewStaffAfter(Staff staff)
	{
		Staff newStaff = new Staff();
		staffList.add(newStaff, staffList.indexOf(staff) + 1);
		return newStaff;
	}
}
