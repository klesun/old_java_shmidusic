package org.sheet_midusic.staff.chord;

import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;

import java.util.ArrayList;

// Tact is chord set from one Tact line to Another (see TactMeasurer)
public class Tact extends AbstractModel
{
	public Arr<Chord> accordList = new Arr<>("chordList", new ArrayList<>(), this, Chord.class);
	public Field<Integer> tactNumber = new Field<>("tactNumber", Integer.class, true, this);

	public Tact(int tactNumber)
	{
		this.tactNumber.set(tactNumber);
	}
}
