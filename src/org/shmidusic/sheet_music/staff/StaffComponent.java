package org.shmidusic.sheet_music.staff;

import org.klesun_model.Explain;
import org.klesun_model.IComponent;
import org.shmidusic.sheet_music.SheetMusicComponent;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.chord.ChordComponent;
import org.shmidusic.sheet_music.staff.chord.note.NoteHandler;
import org.shmidusic.stuff.midi.DeviceEbun;
import org.shmidusic.stuff.musica.Playback;
import org.shmidusic.stuff.graphics.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

// TODO: merge with PaintHelper
public class StaffComponent extends JPanel implements IComponent
{
	final public Staff staff;
	private Set<ChordComponent> chordComponents = new HashSet<>();

	final private SheetMusicComponent parent;
	final private StaffHandler handler;
	final public JPanel chordSpace = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	final private Playback playback;

	private boolean isSelectionActive = false;
	private int selectionStart = -1;

	public StaffComponent(Staff staff, SheetMusicComponent parent)
	{
		this.parent = parent;
		this.staff = staff;
		this.handler = new StaffHandler(this);
		this.playback = new Playback(this);

		this.initLayout();

		staff.chordList.forEach(this::addComponent);
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
				return new Dimension(getLeftMargin(), 10);
			}
		};
		leftGap.setOpaque(false); // temporary solution

		JPanel rightGap = new JPanel() {
			public Dimension getPreferredSize() {
				return new Dimension(getRightMargin(), 10);
			}
		};
		rightGap.setOpaque(false); // temporary solution

		JPanel topGap = new JPanel() { // TODO: deeds are wrong with this margin y - chords drawn wrong place'
			public Dimension getPreferredSize() {
				return new Dimension(10, getTopMargin());
			}
		};
		topGap.setOpaque(false); // temporary solution

		this.add(leftGap, BorderLayout.WEST);
		this.add(rightGap, BorderLayout.EAST);
		this.add(topGap, BorderLayout.NORTH);

		this.add(chordSpace, BorderLayout.CENTER);
		chordSpace.setOpaque(false);
	}

	/** @return - pixel count */
	public static int getLeftMargin() {
		return Settings.inst().getStepWidth() * 4;
	}
	/** @return - pixel count */
	public static int getRightMargin() {
		return Settings.inst().getStepWidth() * 3;
	}
	/** @return - pixel count */
	public static int getTopMargin() {
		return Settings.inst().getStepHeight() * 3;
	}

	public ChordComponent addNewChordWithPlayback()
	{
		return addChord(new Chord());
	}

	public ChordComponent addChord(Chord chord)
	{
		staff.add(chord, staff.getFocusedIndex() + 1);
		ChordComponent chordComp = this.addComponent(chord, staff.getFocusedIndex() + 1);

		moveFocus(1);

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
		chordSpace.remove(comp);
		chordComponents.remove(comp);
		moveFocus(-1);

		revalidate();
		repaint();
	}

	public void refreshTacts() {
		staff.accordListChanged();
		repaint();
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
	public StaffHandler getHandler()	{
		return this.handler;
	}

	@Override
	public SheetMusicComponent getModelParent() {
		return this.parent;
	}

	//=============================
	// event handles
	//=============================

	public Explain moveFocusTact(int sign) {
		return new Explain(false, "Not Implemented Yet!");
	}

	public Explain moveFocusRow(int sign) {
		int n = sign * getAccordInRowCount();
		return staff.getFocusedIndex() + n < staff.chordList.size() && staff.getFocusedIndex() + n >= 0
				? moveFocusWithPlayback(n)
				: new Explain("No more rows").setImplicit(true);
	}

	public void cancelSelection()
	{
		getSelectedChords().forEach(c -> findChild(c).repaint());
		this.isSelectionActive = false;
	}

	public Explain moveFocusWithPlayback(int sign)
	{
		cancelSelection();
		Explain result = moveFocus(sign);
		if (staff.getFocusedAccord() != null && result.isSuccess()) {
			DeviceEbun.closeAllNotes();
			playback.interrupt();
			getFocusedChild().childStream().forEach(NoteHandler::play);
		}
		return result;
	}

	public Explain moveSelectionEnd(int sign) {
		selectionStart = isSelectionActive ? selectionStart : staff.getFocusedIndex();
		isSelectionActive = true;
		return moveFocus(sign).runIfSuccess(() -> {
			if (staff.getFocusedAccord() != null) {
				DeviceEbun.closeAllNotes();
				playback.interrupt();
				getFocusedChild().childStream().forEach(NoteHandler::play);
			}
		});
	}

	public Explain moveFocus(int n)
	{
		int wasIndex = staff.getFocusedIndex();
		setFocus(wasIndex + n);

		return staff.getFocusedIndex() != wasIndex
			? new Explain(true)
			: new Explain(false, "dead end").setImplicit(true);
	}

	public StaffComponent setFocus(ChordComponent comp)
	{
		cancelSelection();
		setFocus(staff.chordList.indexOf(comp.chord));

		playback.interrupt();
		getFocusedChild().childStream().forEach(NoteHandler::play);

		return this;
	}

	public StaffComponent setFocus(int index)
	{
		ChordComponent was = getFocusedChild();
		staff.setFocusedIndex(index);

		if (was != null) {
			was.setFocusedIndex(-1);
			was.paintImmediately(was.getVisibleRect()); // simple repaint() would sometimes lag for a half second, what was not likable
//			was.repaint();
		}
		if (staff.getFocusedIndex() != -1) {
//			getFocusedChild().repaint();
			getFocusedChild().paintImmediately(getFocusedChild().getVisibleRect()); // simple repaint() would sometimes lag for a half second, what was not likable
		}

		getModelParent().mainPanel.chordChanged();

		return this;
	}

	public int getAccordInRowCount() {
		int result = chordSpace.getWidth() / (dx() * 2);
		return Math.max(result, 1);
	}

	public boolean isSelectionActive() { return isSelectionActive; }
	public int getSelectionStart() { return selectionStart; }

	public List<Chord> getSelectedChords() {
		return isSelectionActive
				? staff.getChordList().subList(
						Math.min(selectionStart, staff.getFocusedIndex()) + 1,
						Math.max(selectionStart, staff.getFocusedIndex()) + 1
					)
				: new ArrayList<>();
	}

	public int calcTrueHeight() {
		return staff.getAccordRowList(getAccordInRowCount()).size() * Staff.SISDISPLACE * dy();
	}
}
