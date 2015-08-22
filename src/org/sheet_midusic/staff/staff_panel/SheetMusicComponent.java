package org.sheet_midusic.staff.staff_panel;

import org.klesun_model.*;
import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;
import org.sheet_midusic.stuff.graphics.Settings;
import org.sheet_midusic.stuff.tools.FileProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class SheetMusicComponent extends JPanel implements IComponent
{
	@Deprecated final MainPanel mainPanel;
	final public SheetMusic sheetMusic;
	final AbstractHandler handler;

	private Set<StaffComponent> staffComponentSet = new HashSet<>();

	public SheetMusicComponent(SheetMusic sheetMusic, MainPanel mainPanel)
	{
		this.mainPanel = mainPanel;
		this.sheetMusic = sheetMusic;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		sheetMusic.staffList.get().forEach(staff -> {
			StaffComponent staffComp = new StaffComponent(staff, this);
			staffComponentSet.add(staffComp);
			this.add(staffComp);
		});
		this.revalidate();

		this.handler = new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
				return new TruMap<>()
					.p(new Combo(ctrl, k.VK_L), mkFailableAction(SheetMusicComponent::splitFocusedStaff))
					.p(new Combo(ctrl, k.VK_P), mkAction(SheetMusicComponent::triggerPlayback).setCaption("Play/Stop"))
						// File
					.p(new Combo(ctrl, k.VK_S), mkFailableAction(FileProcessor::saveMusicPanel).setCaption("Save midi.json"))
					.p(new Combo(ctrl, k.VK_U), mkFailableAction(FileProcessor::saveMidi).setCaption("Save midi (alpha)"))
					.p(new Combo(ctrl, k.VK_O), mkFailableAction(FileProcessor::openSheetMusic).setCaption("Open"))
					/** @legacy */
					.p(new Combo(ctrl, k.VK_U), mkFailableAction(FileProcessor::openStaffOld).setCaption("Open Old (When Staff Coul Be Only One)"))

					.p(new Combo(ctrl, k.VK_E), mkFailableAction(FileProcessor::savePNG).setCaption("Export png"))
					;
			}
		};
	}

	public void triggerPlayback() {
		getFocusedChild().getPlayback().trigger();
	}

	/** creating two staffs from one: to pointer pos and from pointer pos */
	public Explain splitFocusedStaff()
	{
		Staff s = getFocusedChild().staff;
		return new Explain(s.getFocusedIndex() > 0, "im not plitting here").runIfSuccess(() -> {
			Staff newStaff = this.sheetMusic.addNewStaffAfter(s);

			List<Chord> staff2Chords = s.getChordList().subList(s.getFocusedIndex(), s.getChordList().size());

			staff2Chords.stream().forEach(c -> {
				s.remove(c);
				newStaff.addNewAccord().reconstructFromJson(c.getJsonRepresentation());
			});
			this.revalidate();

			newStaff.getConfig().reconstructFromJson(newStaff.getJsonRepresentation());
		});
	}

	private int getFocusedSystemY() {
		int dy = Settings.inst().getStepHeight();
		return Staff.SISDISPLACE * dy * (getFocusedChild().staff.getFocusedIndex() / getAccordInRowCount());
	}

	private int getAccordInRowCount() {
		int result = (getWidth() - StaffComponent.getLeftMargin() - StaffComponent.getRightMargin()) / (2 * dx());
		return Math.max(result, 1);
	}

	public void checkCam()
	{
		int dy = Settings.inst().getStepHeight();

		JScrollPane staffScroll = getModelParent().staffScroll;
		JScrollBar vertical = staffScroll.getVerticalScrollBar();

		if (vertical.getValue() + staffScroll.getHeight() < getFocusedSystemY() + Staff.SISDISPLACE * dy ||
				vertical.getValue() > getFocusedSystemY()) {
			vertical.setValue(getFocusedSystemY());
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		int height = getStaffPanelStream().map(c -> c.calcTrueHeight()).reduce(Math::addExact).get();
		return new Dimension(mainPanel.getWidth() - 30, height); // - 30 - love awt and horizontal scrollbars
	}

	private Stream<StaffComponent> getStaffPanelStream()
	{
		return sheetMusic.staffList.get().stream().map(this::getComponentByStaff);
	}

	private StaffComponent getComponentByStaff(Staff staff)
	{
		// TODO: change Set to Map for performance
		return staffComponentSet.stream().filter(p -> p.staff == staff).findAny().get();
	}

	@Override
	public SheetMusic getModel() {
		return this.sheetMusic;
	}

	@Override
	public MainPanel getModelParent() {
		return this.mainPanel;
	}

	@Override
	public StaffComponent getFocusedChild() {
		// TODO: make changing focus possible
		return getComponentByStaff(sheetMusic.staffList.get(0));
	}

	@Override
	public AbstractHandler getHandler() {
		return this.handler;
	}

//		context.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
//			public void mouseEntered(MouseEvent e) {
//				context.getVerticalScrollBar().setCursor(Cursor.getDefaultCursor());
//			}
//		});

//		.p(new Combo(0, KeyEvent.VK_PAGE_DOWN), mkAction(b -> b.page(1)).setCaption("Scroll Up"))
//		.p(new Combo(0, KeyEvent.VK_PAGE_UP), mkAction(b -> b.page(-1)).setCaption("Scroll Up"));

//		public void page(int sign) {
//			JScrollBar vertical = getVerticalScrollBar();
//			vertical.setValue(limit(vertical.getValue() + sign * Staff.SISDISPLACE * getModelParent().getSettings().getStepHeight(), 0, vertical.getMaximum()));
//			repaint();
//		}

	// removing stupid built-ins
//		InputMap im = context.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//		im.put(KeyStroke.getKeyStroke("UP"), "none");
//		im.put(KeyStroke.getKeyStroke("DOWN"), "none");
//		im.put(KeyStroke.getKeyStroke("PAGE_UP"), "none");
//		im.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "none");

	private static ContextAction<SheetMusicComponent> mkAction(Consumer<SheetMusicComponent> lambda) {
		ContextAction<SheetMusicComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<SheetMusicComponent> mkFailableAction(Function<SheetMusicComponent, Explain> lambda) {
		ContextAction<SheetMusicComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
