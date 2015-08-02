package blockspace.staff.accord;

import blockspace.staff.MidianaComponent;
import blockspace.staff.Staff;
import model.AbstractHandler;
import model.AbstractModel;
import model.field.Arr;
import model.field.Field;

import java.awt.*;
import java.util.ArrayList;

// Tact is accord set from one Tact line to Another (see TactMeasurer)
public class Tact extends AbstractModel
{
	public Arr<Chord> accordList = new Arr<>("chordList", new ArrayList<>(), this, Chord.class);
	public Field<Integer> tactNumber = new Field<>("tactNumber", Integer.class, true, this);

	public Tact(int tactNumber)
	{
		this.tactNumber.set(tactNumber);
	}
}
