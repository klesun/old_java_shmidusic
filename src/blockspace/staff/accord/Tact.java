package blockspace.staff.accord;

import blockspace.staff.MidianaComponent;
import blockspace.staff.Staff;
import model.AbstractHandler;
import model.field.Arr;
import model.field.Field;

import java.awt.*;
import java.util.ArrayList;

// Tact is accord set from one Tact line to Another (see TactMeasurer)
public class Tact extends MidianaComponent
{
	public Arr<Chord> accordList = new Arr<>("accordList", new ArrayList<>(), this, Chord.class);
	public Field<Integer> tactNumber = new Field<>("tactNumber", Integer.class, true, this);

	public Tact(Staff parent)
	{
		this(parent, -100);
	}

	public Tact(Staff parent, int tactNumber)
	{
		super(parent);
		this.tactNumber.set(tactNumber);
	}

	@Override
	public MidianaComponent getFocusedChild()
	{
		return null;
	}

	@Override
	protected AbstractHandler makeHandler()
	{
		return null;
	}

	@Override
	public void drawOn(Graphics2D surface, int x, int y, Boolean completeRepaint)
	{
	}
}
