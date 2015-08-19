package org.sheet_midusic.staff.staff_panel;

import org.klesun_model.AbstractHandler;
import org.klesun_model.Explain;
import org.klesun_model.IComponent;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.StaffHandler;
import org.sheet_midusic.staff.StaffPainter;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.staff.chord.ChordComponent;
import org.sheet_midusic.staff.chord.ChordHandler;
import org.sheet_midusic.stuff.Midi.DeviceEbun;
import org.sheet_midusic.stuff.Midi.Playback;
import org.sheet_midusic.stuff.graphics.Settings;
import org.sheet_midusic.stuff.musica.PlayMusThread;
import org.sheet_midusic.stuff.tools.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

// TODO: merge with AbstractPainter
public class StaffComponent extends JPanel implements IComponent
{
	final public Staff staff;
	private Set<ChordComponent> chordComponents = new HashSet<>();

	final private IComponent parent;
	final private StaffHandler handler;
	final private JPanel chordSpace = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	final private Playback playback;

	public StaffComponent(Staff staff, IComponent parent) {
		this.parent = parent;
		this.staff = staff;
		this.handler = new StaffHandler(this);
		this.playback = new Playback(this);

		this.initLayout();

		staff.chordStream().forEach(this::addComponent);
		this.setBackground(Color.WHITE);
		this.revalidate();
	}

	@Override
	public Dimension getPreferredSize() {
		int height = super.getPreferredSize().height; // maybe wrong
		// the problem probably should hav been solved with some constant in BoxLayout of parent...
		return new Dimension(((Component)parent).getWidth(), height);
	}

	private void initLayout()
	{
		this.setLayout(new BorderLayout());

		JPanel leftGap = new JPanel() {
			public Dimension getPreferredSize() {
				return new Dimension(staff.getMarginX() * 4, 10);
			}
		};
		leftGap.setOpaque(false); // temporary solution

		JPanel rightGap = new JPanel() {
			public Dimension getPreferredSize() {
				return new Dimension(staff.getMarginX() * 3, 10);
			}
		};
		rightGap.setOpaque(false); // temporary solution

		JPanel topGap = new JPanel() { // TODO: deeds are wrong with this margin y - chords drawn wrong place'
			public Dimension getPreferredSize() {
				return new Dimension(10, staff.getMarginY() - 12 * Settings.inst().getStepHeight());
			}
		};
		topGap.setOpaque(false); // temporary solution

		this.add(leftGap, BorderLayout.WEST);
		this.add(rightGap, BorderLayout.EAST);
		this.add(topGap, BorderLayout.NORTH);

		this.add(chordSpace, BorderLayout.CENTER);
		chordSpace.setOpaque(false);
	}

	public ChordComponent addNewChordWithPlayback()
	{
		Chord chord = staff.addNewAccord(staff.getFocusedIndex() + 1);
		ChordComponent chordComp = this.addComponent(chord, staff.getFocusedIndex() + 1);

		moveFocus(1);
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

	private ChordComponent addComponent(Chord chord) {
		return addComponent(chord, chordComponents.size());
	}

	private ChordComponent addComponent(Chord chord, int position)
	{
		ChordComponent comp = new ChordComponent(chord, this);
		chordComponents.add(comp);
		chordSpace.add(comp, position);

		return comp;
	}

	public void removeChord(Chord chord)
	{
		staff.remove(chord);
		ChordComponent comp = findChild(chord);
		chordSpace.add(comp);
		chordComponents.remove(comp);
		revalidate();
	}

	public ChordComponent findChild(Chord chord)
	{
		// TODO: change Set to Map for performance
		return chordComponents.stream().filter(p -> p.chord == chord).findAny().get();
	}

	public Playback getPlayback() { return this.playback; }

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		new StaffPainter(this, (Graphics2D)g, 0, 0).draw(true);
	}

	//====================================
	// implementing IModel methods
	//====================================

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
	public AbstractHandler getHandler()	{
		return this.handler;
	}

	@Override
	public IComponent getModelParent() {
		return this.parent;
	}

	//=============================
	// event handles
	//=============================

	public Explain moveFocusWithPlayback(int sign) {
		Explain result = moveFocus(sign);
		if (staff.getFocusedAccord() != null && result.isSuccess()) {

			PlayMusThread.shutTheFuckUp();
			playback.interrupt();
			PlayMusThread.playAccord(staff.getFocusedAccord());
		}
		return result;
	}

	public Explain moveFocusTact(int sign) {
		return new Explain(false, "Not Implemented Yet!");
	}

	public Explain moveFocusRow(int sign, int width) {
		int n = sign * staff.getAccordInRowCount(width);
		return moveFocusWithPlayback(n);
	}

	public Explain moveFocus(int n)
	{
		int wasIndex = staff.getFocusedIndex();
		staff.setFocusedIndex(wasIndex + n);

		if (staff.getFocusedIndex() != -1) { // does not repaint pointer for some reason
			findChild(staff.getFocusedAccord()).repaint();
		}
		if (wasIndex != -1) {
			findChild(staff.getChordList().get(wasIndex)).repaint(); // TODO: with Home/End not cleaned and piano grephic layout should be repainted each time
		}

		return staff.getFocusedIndex() != wasIndex
				? new Explain(true)
				: new Explain(false, "dead end").setImplicit(true);
	}
}
