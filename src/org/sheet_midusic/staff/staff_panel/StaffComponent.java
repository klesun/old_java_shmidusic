package org.sheet_midusic.staff.staff_panel;

import org.klesun_model.AbstractHandler;
import org.klesun_model.IComponent;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.StaffHandler;
import org.sheet_midusic.staff.StaffPainter;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.staff.chord.ChordComponent;
import org.sheet_midusic.staff.chord.ChordHandler;
import org.sheet_midusic.stuff.Midi.DeviceEbun;
import org.sheet_midusic.stuff.musica.PlayMusThread;
import org.sheet_midusic.stuff.tools.Logger;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

// TODO: merge with AbstractPainter
public class StaffComponent extends MidianaComponent
{
	final public Staff staff;
	private Set<ChordComponent> chordComponents = new HashSet<>();

	public StaffComponent(Staff staff, IComponent cont) {
		super(cont);
		this.staff = staff;
		staff.chordStream().forEach(this::addComponent);
		this.revalidate();
	}

	public ChordComponent addNewChordWithPlayback()
	{
		Chord chord = staff.addNewAccord(staff.getFocusedIndex() + 1);
		ChordComponent chordComp = this.addComponent(chord);

		staff.moveFocus(1);
		if (DeviceEbun.isPlaybackSoftware()) { // i.e. when playback is not done with piano - no need to play pressed chord, user hears it anyways
			new Thread(() -> {
				try {
					Thread.sleep(ChordHandler.ACCORD_EPSILON);
					PlayMusThread.playAccord(chord);
				} catch (InterruptedException exc) {
					Logger.error("okay...");
				}
			}).start();
		}

		revalidate();
		return chordComp;
	}

	private ChordComponent addComponent(Chord chord)
	{
		ChordComponent comp = new ChordComponent(chord, this);
		chordComponents.add(comp);
		return comp;
	}

	public void removeChord(Chord chord)
	{
		staff.remove(chord);
		chordComponents.remove(findChild(chord));
		revalidate();
	}

	public void revalidate() // may conflict with java's Component
	{
		int width = getContainer().getWidth();
		getContainer().setPreferredSize(new Dimension(10, staff.getHeightIf(width)));
		getContainer().revalidate();	//	Needed to recalc the scrollBar bars
	}

	public ChordComponent findChild(Chord chord)
	{
		// TODO: change Set to Map for performance
		return chordComponents.stream().filter(p -> p.chord == chord).findAny().get();
	}

	@Override
	public Staff getModel() {
		return this.staff;
	}

	@Override
	public ChordComponent getFocusedChild()
	{
		// TODO: pointer should be here, not in Model
		return staff.getFocusedAccord() != null ? findChild(staff.getFocusedAccord()) : null;
	}

	@Override
	protected AbstractHandler makeHandler()
	{
		return new StaffHandler(this);
	}

	public int drawOn(Graphics2D g, int x, int y)
	{
		new StaffPainter(this, g, x, y).draw(true);
		return staff.getHeightIf(-100);
	}

	private Component getContainer()
	{
		return (Component)getModelParent();
	}

	public int getWidth()
	{
		return getContainer().getWidth();
	}
}
