package blockspace.staff.accord;

import blockspace.staff.MidianaComponent;
import blockspace.staff.Staff;
import model.AbstractHandler;
import model.field.Arr;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Tact is accord set from one Tact line to Another (see TactMeasurer)
public class Tact extends MidianaComponent
{
	public Arr<Accord> accordList = new Arr<>("accordList", new ArrayList<>(), this, Accord.class);

	public Tact(Staff parent)
	{
		super(parent);
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
	public void drawOn(Graphics surface, int x, int y, Boolean completeRepaint)
	{

	}
}
