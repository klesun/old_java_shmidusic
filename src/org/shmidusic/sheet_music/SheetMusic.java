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

	/** use this constructor when restoring object from json */
	public SheetMusic(JSONObject state) {
		reconstructFromJson(state);
	}

	/** use this constructor when creating new object */
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
